package org.transitclock.feed.zmq.oba;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nyc.transit_data.model.NycQueuedInferredLocationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.avl.ZeroMQAvlModule;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.feed.zmq.ZmqQueueBeanReader;
import org.transitclock.utils.MathUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains method to convert JSON into NycQueueInferredLocationBean and
 * to convert NycQueueInferredLocationBean into an AvlReport.
 *
 * @author carabalb
 *
 */
public class NycQueueInferredLocationBeanReader implements ZmqQueueBeanReader {

    private static final Logger logger = LoggerFactory
            .getLogger(ZeroMQAvlModule.class);
    private static final ObjectMapper _mapper = new ObjectMapper();
    private static final ObjectReader _reader =  _mapper.readerFor(NycQueuedInferredLocationBean.class);

    Date markTimestamp = new Date();
    private int processedCount = 0;
    private int acceptableProcessedCount = 0;
    private int avlReportProcessedCount = 0;
    private static final int COUNT_INTERVAL = 10000;

    public static StringConfigValue zeromqRouteFilter =
            new StringConfigValue("transitclock.avl.zeromq.allowedRoutes",
                    "*",
                    "List of acceptable routes for incoming avl data. Defaults to * which allows all.");

    private static Set<String> routeFilterList = new HashSet<>();

    public NycQueueInferredLocationBeanReader(){
        initializeRouteFilter();
    }

    /********************** Member Functions **************************/

    private void initializeRouteFilter(){
        try {
            String[] routesToFilter = zeromqRouteFilter.getValue().split("(;|,| +)");
            for(String routeToFilter: routesToFilter){
                routeFilterList.add(routeToFilter.toUpperCase());
            }
        }catch (Exception e){
            routeFilterList.clear();
            routeFilterList.add("*");
        }
    }


    @Override
    public AvlReport getAvlReport(String topic, String contents) throws Exception {
        // Get NycQueuedInferredLocationBean from ZMQ
        NycQueuedInferredLocationBean inferredLocationBean = processMessage(contents);
        processedCount++;

        // Check for data issues and/or filter out data
        if(!acceptableResult(inferredLocationBean)){
            logCounts(topic);
            return null;
        } else {
            acceptableProcessedCount++;
        }

        AvlReport avlReport = convertInferredLocationBeanToAvlReport(inferredLocationBean);
        avlReportProcessedCount++;

        return avlReport;

    }

    private NycQueuedInferredLocationBean processMessage(String contents) throws Exception {
        try {
            NycQueuedInferredLocationBean inferredResult = _reader.readValue(contents);
            return inferredResult;
        } catch (Exception e) {
            logger.warn("Received corrupted message from queue; discarding: " + e.getMessage(), e);
            logger.warn("Contents=" + contents);
            throw e;
        }
    }

    private boolean acceptableResult(NycQueuedInferredLocationBean inferredLocationBean){
        String routeId = inferredLocationBean.getInferredRouteId();

        if(routeId == null || routeFilterList.isEmpty() || (routeFilterList.size() > 0 && routeFilterList.contains("*")))
            return true;
        try {
            String route = AgencyAndId.convertFromString(routeId).getId().toUpperCase();
            if (routeFilterList.contains(route)) {
                return true;
            } else {
                logger.debug("NycQueuedInferredLocationBean {} not filtered", inferredLocationBean);
                return false;
            }
        } catch (Exception e){
            logger.error("Error processing NycQueuedInferredLocationBean {}", inferredLocationBean, e);
            return false;
        }
    }

    private AvlReport convertInferredLocationBeanToAvlReport(NycQueuedInferredLocationBean inferredLocationBean){

        String vehicleId = inferredLocationBean.getVehicleId();
        long gpsTime = inferredLocationBean.getRecordTimestamp();
        Double lat = inferredLocationBean.getInferredLatitude();
        Double lon = inferredLocationBean.getInferredLongitude();
        float speed = Float.NaN;
        float heading = (float) inferredLocationBean.getBearing();

        // AvlReport is expecting time in ms while the proto provides it in
        // seconds
        AvlReport avlReport = new AvlReport(
                vehicleId,
                gpsTime,
                MathUtils.round(lat, 5),
                MathUtils.round(lon, 5),
                speed,
                heading,
                "ZMQ",
                null, // leadingVehicleId,
                null, // driverId
                null,
                null, // passengerCount
                Float.NaN // passengerFullness
        );

        if(inferredLocationBean.getInferredBlockId() != null) {
            avlReport.setAssignment(AgencyAndId.convertFromString(inferredLocationBean.getInferredBlockId()).getId(), AvlReport.AssignmentType.BLOCK_ID);
        } else if(inferredLocationBean.getInferredRouteId() != null){
            avlReport.setAssignment(AgencyAndId.convertFromString(inferredLocationBean.getInferredRouteId()).getId(), AvlReport.AssignmentType.ROUTE_ID);
        }  else if(inferredLocationBean.getInferredTripId() != null){
            avlReport.setAssignment(AgencyAndId.convertFromString(inferredLocationBean.getInferredTripId()).getId(),
                    AvlReport.AssignmentType.TRIP_ID);
        }
        return avlReport;
    }

    private void logCounts(String topic){
        if (processedCount > COUNT_INTERVAL) {
            long timeInterval = (new Date().getTime() - markTimestamp.getTime());
            int timeIntervalSec = (int)(timeInterval/1000);

            logger.info("{} input queue: processed {} messages in {} seconds. ({}) records/second",
                    topic, COUNT_INTERVAL, timeIntervalSec, (int)(1000.0 * processedCount/timeInterval));

            logger.info("{} input queue: processed {} accepted messages in {} seconds. ({}) records/second",
                    topic, acceptableProcessedCount, timeIntervalSec, (int)(1000.0 * acceptableProcessedCount/timeInterval));

            logger.info("processed {} avl report records in {} seconds. ({}) records/second",
                    avlReportProcessedCount, timeIntervalSec, (int)(1000.0 * avlReportProcessedCount/timeInterval));


            markTimestamp = new Date();
            processedCount = 0;
            acceptableProcessedCount = 0;
            avlReportProcessedCount = 0;
        }
    }
}
