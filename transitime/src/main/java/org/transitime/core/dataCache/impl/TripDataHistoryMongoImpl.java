package org.transitime.core.dataCache.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.Document;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.LongConfigValue;
import org.transitime.core.dataCache.*;
import org.transitime.core.dataCache.model.ITripHistoryArrivalDeparture;
import org.transitime.core.dataCache.model.TripHistoryArrivalDeparture;
import org.transitime.core.dataCache.model.TripKey;
import org.transitime.db.mongo.MongoDB;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.Trip;
import org.transitime.gtfs.DbConfig;
import org.transitime.utils.Time;

import javax.annotation.PreDestroy;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class TripDataHistoryMongoImpl implements TripDataHistoryCache {

    private static final Logger logger = LoggerFactory
            .getLogger(TripDataHistoryMongoImpl.class);

    private static boolean debug = false;

    final private static String collectionName = "arrivalDeparturesByTrip";

    private static final Gson gson = new Gson();

    private MongoCollection<Document>  collection = null;

    private static AtomicInteger insertCounter = new AtomicInteger(0);

    private static final LongConfigValue cacheTTL = new LongConfigValue(
            "transitime.cache.maxAgeMin", new Long(7 * Time.MIN_PER_DAY),
            "Maximum amount of time for data to live in the cache");

    public TripDataHistoryMongoImpl() {
        try {
            MongoDatabase db = MongoDB.getInstance().getDatabase();
           boolean collectionExists = db.listCollectionNames()
                    .into(new ArrayList<String>()).contains(collectionName);
            if(!collectionExists) {
                db.createCollection(collectionName, new CreateCollectionOptions());
                collection = db.getCollection(collectionName);
                collection.createIndex(Indexes.ascending("creationDate"),
                        new IndexOptions().expireAfter(cacheTTL.getValue(), TimeUnit.MINUTES));
           } else {
                collection = db.getCollection(collectionName);
            }
        } catch (Exception e) {
            logger.error("Error connecting to MongoDB", e);
        }
    }

    public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
        Criteria criteria =session.createCriteria(ArrivalDeparture.class);

        @SuppressWarnings("unchecked")
        List<ArrivalDeparture> results=criteria.add(Restrictions.between("time", startDate, endDate)).list();

        int counter = 0;

        for(ArrivalDeparture result : results)
        {
            if(counter % 1000 == 0){
                logger.info("{} out of {} Trip Data History Records", counter, results.size());
            }
            putArrivalDeparture(result);
            counter++;
        }
        logger.info("TripDataHistory populateCacheFromDb finished");
    }

    synchronized public TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {
        /* just put todays time in for last three days to aid development. This means it will kick in in 1 days rather than 3. Perhaps be a good way to start rather than using default transiTime method but I doubt it. */
        int days_back=1;
        if(debug)
            days_back=3;
        TripKey tripKey=null;

        for(int i=0;i < days_back;i++)
        {
            Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);
            nearestDay=DateUtils.addDays(nearestDay, i*-1);

            DbConfig dbConfig = Core.getInstance().getDbConfig();
            Trip trip=dbConfig.getTrip(arrivalDeparture.getTripId());
            Integer startTime = trip != null ? trip.getStartTime() : null;

            tripKey = new TripKey(arrivalDeparture.getTripId(), nearestDay,startTime);

            Set<ITripHistoryArrivalDeparture> set = null;

            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("_id", getTripKeyHash(tripKey));
            searchQuery.put("tripId", tripKey.getTripId());
            searchQuery.put("startTime", tripKey.getStartTime());
            searchQuery.put("startDate", tripKey.getTripStartDate());

            Document result = collection.find(searchQuery).first();
            if (result != null) {
                String arrivalDeparturesAsJson = result.get("arrivalDepartures").toString();
                Type setType = new TypeToken<HashSet<TripHistoryArrivalDeparture>>(){}.getType();
                set = gson.fromJson(arrivalDeparturesAsJson, setType);
                set.add(new TripHistoryArrivalDeparture(arrivalDeparture));
                updateData(result, set);
            } else {
                set = new HashSet<ITripHistoryArrivalDeparture>();
                set.add(new TripHistoryArrivalDeparture(arrivalDeparture));
                insertData(tripKey,set);
            }
        }
        return tripKey;
    }


    private void insertData(TripKey tripKey, Set<ITripHistoryArrivalDeparture> list){
        Document document = new Document();

        document.put("_id", getTripKeyHash(tripKey));
        document.put("tripId", tripKey.getTripId());
        document.put("startTime", tripKey.getStartTime());
        document.put("startDate", tripKey.getTripStartDate());
        document.put("creationDate",  new Date());

        String arrivalsAndDepartures = gson.toJson(list);
        document.put("arrivalDepartures", arrivalsAndDepartures);

        collection.insertOne(document);

        logger.trace("Document with trip id {} inserted successfully", document.get("tripId"));
    }

    private void updateData(Document document, Set<ITripHistoryArrivalDeparture> list){
        String arrivalsAndDepartures = gson.toJson(list);

        Document newDocument = new Document();
        newDocument.put("arrivalDepartures", arrivalsAndDepartures);

        Document updateObject = new Document();
        updateObject.put("$set", newDocument);

        UpdateResult result = collection.updateOne(document, updateObject);
        if(!result.wasAcknowledged()){
            logger.error("Document {} failed to update", document);
        } else {
            logger.trace("Document with trip id {} updated successfully", document.get("tripId"));
        }
    }

    synchronized public Set<ITripHistoryArrivalDeparture> getTripHistory(TripKey tripKey) {

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("tripId", tripKey.getTripId());
        searchQuery.put("startTime", tripKey.getStartTime());
        searchQuery.put("startDate", tripKey.getTripStartDate());
        Document result = collection.find(searchQuery).first();

        if(result != null) {
            String arrivalDeparturesJson = result.get("arrivalDepartures").toString();
            Type listType = new TypeToken<HashSet<TripHistoryArrivalDeparture>>() {}.getType();
            Set<ITripHistoryArrivalDeparture> arrivalDepartures = gson.fromJson(arrivalDeparturesJson, listType);
            logger.trace("Get arrival departures");
            return arrivalDepartures;
        } else {
            return null;
        }
    }

    public Set<ITripHistoryArrivalDeparture> getTripHistory(final String tripId, final Date date, final Integer starttime) {
        if(tripId!=null && date!=null && starttime!=null){
            return getTripHistory(new TripKey(tripId, date, starttime));
        }

        Set<ITripHistoryArrivalDeparture> results = new HashSet<>();
        MongoCursor<Document> cursor = null;

        if(tripId!=null && date!=null && starttime==null)
        {
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("tripId", tripId);
            searchQuery.put("date", date);
            cursor = collection.find(searchQuery).iterator();

        }else if(tripId!=null && date==null && starttime==null)
        {
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("tripId", tripId);
            cursor = collection.find(searchQuery).iterator();
        }
        else if(tripId==null && date!=null && starttime==null)
        {
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("date", date);
            cursor = collection.find(searchQuery).iterator();
        }

        if(cursor != null) {
            Type listType = new TypeToken<HashSet<TripHistoryArrivalDeparture>>() {}.getType();
            while (cursor.hasNext()) {
                Set<ITripHistoryArrivalDeparture> arrivalDepartures = gson.fromJson(cursor.next().get("arrivalDepartures").toString(), listType);
                results.addAll(arrivalDepartures);
            }
        }
        logger.trace("Get arrival departures");
        return results;
    }

    public ITripHistoryArrivalDeparture findPreviousDepartureEvent(Set<ITripHistoryArrivalDeparture> arrivalDepartures, ITripHistoryArrivalDeparture current) {
        return CacheUtil.findPreviousDepartureEvent(arrivalDepartures, current);
    }

    public ITripHistoryArrivalDeparture findPreviousArrivalEvent(Set<ITripHistoryArrivalDeparture> arrivalDepartures, ITripHistoryArrivalDeparture current) {
        return CacheUtil.findPreviousArrivalEvent(arrivalDepartures, current);
    }

    private String getTripKeyHash(TripKey tripKey){
        return tripKey.getTripId() + "_" + tripKey.getStartTime() + "_" + tripKey.getTripStartDate().getTime();
    }

    @Override
    public boolean isCacheForDateProcessed(Date startDate, Date endDate){
        return HistoricalCacheService.getInstance().isCacheForDateProcessed(CacheType.TRIP_DATA_HISTORY, startDate, endDate);
    }

    @Override
    public void saveCacheHistoryRecord(Date startDate, Date endDate){
        HistoricalCacheService.getInstance().save(CacheType.TRIP_DATA_HISTORY, startDate, endDate);
    }

}
