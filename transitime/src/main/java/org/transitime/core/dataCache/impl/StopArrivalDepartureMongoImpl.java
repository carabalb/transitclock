package org.transitime.core.dataCache.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonSerializationException;
import org.bson.Document;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.LongConfigValue;
import org.transitime.core.dataCache.*;
import org.transitime.core.dataCache.model.IStopArrivalDeparture;
import org.transitime.core.dataCache.model.StopArrivalDeparture;
import org.transitime.core.dataCache.model.StopArrivalDepartureCacheKey;
import org.transitime.db.mongo.MongoDB;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.Stop;
import org.transitime.utils.Time;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StopArrivalDepartureMongoImpl implements StopArrivalDepartureCache {

    private static final Logger logger = LoggerFactory
            .getLogger(StopArrivalDepartureMongoImpl.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static boolean debug = false;

    final private static String collectionName = "arrivalDeparturesByStop";

    private MongoCollection<Document> collection = null;

    private static AtomicInteger insertCounter = new AtomicInteger(0);

    private static final LongConfigValue cacheTTL = new LongConfigValue(
            "transitime.cache.maxAgeMin", new Long(7 * Time.MIN_PER_DAY),
            "Maximum amount of time for data to live in the cache");

    public StopArrivalDepartureMongoImpl() {
        try {
            MongoDatabase db = MongoDB.getInstance().getDatabase();
            boolean collectionExists = db.listCollectionNames()
                    .into(new ArrayList<String>()).contains(collectionName);
            if(!collectionExists) {
                db.createCollection(collectionName, new CreateCollectionOptions());
                collection = db.getCollection(collectionName);
                collection.createIndex(Indexes.ascending("creationDate"),
                        new IndexOptions().expireAfter(cacheTTL.getValue(), TimeUnit.MINUTES));
            }else {
                collection = db.getCollection(collectionName);
            }
        } catch (Exception e) {
            logger.error("Error connecting to MongoDB", e);
        }
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    }

    @Override
    public List<StopArrivalDepartureCacheKey> getKeys() {
        return null;
    }

    @Override
    synchronized public Set<IStopArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key) {
        Calendar date = Calendar.getInstance();
        date.setTime(key.getDate());

        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        Document documentKey = new Document();
        documentKey.put("stopId", key.getStopid());
        documentKey.put("date", date.getTime());

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", documentKey);

        Document result = collection.find(searchQuery).first();

        if(result != null) {
            String arrivalDeparturesJson = result.get("arrivalDepartures").toString();
            try {
                Set<IStopArrivalDeparture> arrivalDepartures = objectMapper.readValue(arrivalDeparturesJson,
                        new TypeReference<LinkedHashSet<StopArrivalDeparture>>() {});

                logger.trace("Getting arrival and departure {}", key);
                return arrivalDepartures;
            } catch (Exception e){
                logger.error("Unable to get ArrivalDeparture {}", key, e);
                logger.debug(arrivalDeparturesJson);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    synchronized public StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {
        boolean trace = false;

        if(arrivalDeparture == null || arrivalDeparture.getStop() == null || arrivalDeparture.getDate() == null){
            logger.error("Invalid arrival departure {}", arrivalDeparture);
            return null;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(arrivalDeparture.getDate());

        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        StopArrivalDepartureCacheKey key = new StopArrivalDepartureCacheKey(arrivalDeparture.getStop().getId(),
                date.getTime());

        Set<IStopArrivalDeparture> set = null;

        Document documentKey = new Document();
        documentKey.put("stopId", key.getStopid());
        documentKey.put("date", key.getDate().getTime());

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", getKeyHash(key.getStopid(), key.getDate().getTime()) );

        Document result = collection.find(searchQuery).first();

        if (result != null) {
            String arrivalDeparturesAsJson = result.get("arrivalDepartures").toString();
            try {
                set = objectMapper.readValue(arrivalDeparturesAsJson, new TypeReference<TreeSet<StopArrivalDeparture>>() {});
                set.add(new StopArrivalDeparture(arrivalDeparture));
                updateData(result, set);
            } catch(JsonProcessingException jpe) {
                logger.error("Unable to convert ArrivalDeparture {} to JSON", key, jpe);
            } catch (Exception e) {
                logger.error("Unable to add ArrivalDeparture {} to StopArrivalDeparture cache", key, e);
                logger.debug(arrivalDeparturesAsJson);
                return null;
            }
        } else {
            set = new LinkedHashSet<>();
            set.add(new StopArrivalDeparture(arrivalDeparture));
            try {
                insertData(documentKey, set);
            } catch (JsonProcessingException jpe) {
                logger.error("Unable to convert ArrivalDeparture {} to JSON", key, jpe);
            }
        }
        return key;
    }

    private void insertData(Document id, Set<IStopArrivalDeparture> list) throws JsonProcessingException {
        Document document = new Document();
        String stopId = (String)id.get("stopId");
        Long date = (Long)id.get("date");
        document.put("_id", getKeyHash(stopId, date));
        document.put("creationDate",  new Date());

        String arrivalsAndDepartures = objectMapper.writeValueAsString(list);
        document.put("arrivalDepartures", arrivalsAndDepartures);

        collection.insertOne(document);
        logger.trace("Document with trip id {} inserted successfully", document.get("tripId"));

    }

    private void updateData(Document document, Set<IStopArrivalDeparture> list) throws JsonProcessingException {
        String arrivalsAndDepartures = objectMapper.writeValueAsString(list);

        Document newDocument = new Document();
        newDocument.put("arrivalDepartures", arrivalsAndDepartures);

        Document updateObject = new Document();
        updateObject.put("$set", newDocument);
        try {
            UpdateResult result = collection.updateOne(document, updateObject);
            logger.trace("Document with trip id {} updated successfully", document.get("tripId"));
        }catch(BsonSerializationException bse){
            logger.error("Document {} failed to update", document, bse);
            logger.debug("Document output: {}", newDocument);
        }
    }

    @Override
    public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
        if(isCacheForDateProcessed(startDate, endDate)){
            logger.info("StopArrivalDeparture cache for start date {} - end date {} has already been processed, skipping", startDate, endDate);
            return;
        } else {
            Date actualStartDate = HistoricalCacheService.getInstance().getStartTime(CacheType.STOP_ARRIVAL_DEPARTURE, startDate.getTime(), endDate.getTime());
            logger.info("Populating TripDataHistory cache for period {} to {}", endDate, actualStartDate);

            Criteria criteria = session.createCriteria(ArrivalDeparture.class);

            @SuppressWarnings("unchecked")
            List<ArrivalDeparture> results = criteria.add(Restrictions.between("time", actualStartDate, endDate)).list();
            int counter = 0;
            for (ArrivalDeparture result : results) {
                if (counter % 1000 == 0) {
                    logger.info("{} out of {} Stop Arrival Departure Records ({}%)", counter, results.size(), (int) ((counter * 100.0f) / results.size()));
                }
                putArrivalDeparture(result);
                counter++;
            }
            if(!startDate.equals(actualStartDate)){
                logger.info("Only populating subset of StopArrivalDeparture cache from {} to {} since the rest has already been cached", endDate, actualStartDate);
            }
            logger.info("Finished populating StopArrivalDeparture cache for period {} to {}", endDate, actualStartDate );
        }
    }

    @Override
    public boolean isCacheForDateProcessed(Date startDate, Date endDate){
        return HistoricalCacheService.getInstance().isCacheForDateProcessed(CacheType.STOP_ARRIVAL_DEPARTURE, startDate, endDate);
    }

    @Override
    public void saveCacheHistoryRecord(Date startDate, Date endDate) {
        HistoricalCacheService.getInstance().save(CacheType.STOP_ARRIVAL_DEPARTURE, startDate, endDate);
    }

    private String getKeyHash(String stopId, Long time){

        return stopId + "_" + time;
    }
}
