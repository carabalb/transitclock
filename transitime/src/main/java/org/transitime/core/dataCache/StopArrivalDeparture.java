package org.transitime.core.dataCache;

import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.Block;

import java.util.Date;

public class StopArrivalDeparture implements IStopArrivalDeparture {

    private final String vehicleId;

    private final Date date;

    private final String stopId;

    private final boolean isArrival;

    private final boolean isDeparture;

    private final String tripId;

    private final String blockId;

    private final String routeId;

    private final int stopPathIndex;

    private final Date scheduledTime;

    private final Block block;

    private final String serviceId;

    private final int tripIndex;


    public StopArrivalDeparture(String vehicleId, Date time, String stopId, String tripId, String blockId,
                                String routeId, int stopPathIndex, boolean isArrival, boolean isDeparture, Date scheduledTime,
                                Block block, String serviceId, int tripIndex) {
        this.vehicleId = vehicleId;
        this.date = time;
        this.stopId = stopId;
        this.tripId = tripId;
        this.blockId = blockId;
        this.routeId = routeId;
        this.stopPathIndex = stopPathIndex;
        this.isArrival = isArrival;
        this.isDeparture = isDeparture;
        this.scheduledTime = scheduledTime;
        this.block = block;
        this.serviceId = serviceId;
        this.tripIndex = tripIndex;
    }

    public StopArrivalDeparture(ArrivalDeparture arrivalDeparture) {
        this.vehicleId = arrivalDeparture.getVehicleId();
        this.date = arrivalDeparture.getDate();
        this.stopId = arrivalDeparture.getStopId();
        this.tripId = arrivalDeparture.getTripId();
        this.blockId = arrivalDeparture.getBlockId();
        this.routeId = arrivalDeparture.getRouteId();
        this.stopPathIndex = arrivalDeparture.getStopPathIndex();
        this.isArrival = arrivalDeparture.isArrival();
        this.isDeparture = arrivalDeparture.isDeparture();
        this.scheduledTime = arrivalDeparture.getScheduledDate();
        this.block = arrivalDeparture.getBlock();
        this.serviceId = arrivalDeparture.getServiceId();
        this.tripIndex = arrivalDeparture.getTripIndex();
    }

    @Override
    public String getVehicleId() {
        return vehicleId;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public String getStopId() {
        return stopId;
    }

    @Override
    public boolean isArrival() {
        return isArrival;
    }

    @Override
    public boolean isDeparture() {
        return isDeparture;
    }

    @Override
    public String getTripId() {
        return tripId;
    }

    @Override
    public String getBlockId() {
        return blockId;
    }

    @Override
    public String getRouteId() {
        return routeId;
    }

    @Override
    public int getStopPathIndex() {
        return stopPathIndex;
    }

    @Override
    public Date getScheduledDate() {
        return scheduledTime;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public int getTripIndex() {
        return tripIndex;
    }
}
