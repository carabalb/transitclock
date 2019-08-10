package org.transitime.core.dataCache.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.LongConfigValue;
import org.transitime.core.dataCache.*;
import org.transitime.db.mongo.MongoDB;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.utils.Time;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StopArrivalDepartureMongoImpl implements StopArrivalDepartureCache {

    private static final Logger logger = LoggerFactory
            .getLogger(StopArrivalDepartureMongoImpl.class);

    private static boolean debug = false;

    final private static String collectionName = "arrivalDeparturesByStop";

    private static final Gson gson = new Gson();

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
                Type listType = new TypeToken<HashSet<StopArrivalDeparture>>() {}.getType();
                Set<IStopArrivalDeparture> arrivalDepartures = gson.fromJson(arrivalDeparturesJson, listType);
                return arrivalDepartures;
            } catch (Exception e){
                logger.error("problem",e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    synchronized public StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {
        synchronized(insertCounter) {
            if (insertCounter.get() % 1000 == 0) {
                logger.debug("{} stop arrival departures added", insertCounter.get());
            }
            insertCounter.getAndIncrement();
        }

        Calendar date = Calendar.getInstance();
        date.setTime(arrivalDeparture.getDate());

        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        StopArrivalDepartureCacheKey key = new StopArrivalDepartureCacheKey(arrivalDeparture.getStop().getId(),
                date.getTime());

        Set<IStopArrivalDeparture> list = null;

        Document documentKey = new Document();
        documentKey.put("stopId", key.getStopid());
        documentKey.put("date", key.getDate());

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", documentKey);

        Document result = collection.find(searchQuery).first();
        if (result != null) {
            String arrivalDeparturesAsJson = result.get("arrivalDepartures").toString();
            Type setType = new TypeToken<HashSet<StopArrivalDeparture>>(){}.getType();
            list = gson.fromJson(arrivalDeparturesAsJson, setType);
            list.add(new StopArrivalDeparture(arrivalDeparture));
            updateData(result, list);
        } else {
            list = new HashSet<>();
            list.add(new StopArrivalDeparture(arrivalDeparture));
            insertData(documentKey,list);
        }

        return key;
    }

    private void insertData(Document id, Set<IStopArrivalDeparture> list){
        Document document = new Document();
        document.put("_id", id);
        document.put("creationDate",  new Date());

        String arrivalsAndDepartures = gson.toJson(list);
        document.put("arrivalDepartures", arrivalsAndDepartures);

        collection.insertOne(document);

        //logger.debug("Document with trip id {} inserted successfully", document.get("tripId"));

    }

    private void updateData(Document document, Set<IStopArrivalDeparture> list){
        String arrivalsAndDepartures = gson.toJson(list);

        Document newDocument = new Document();
        newDocument.put("arrivalDepartures", arrivalsAndDepartures);

        Document updateObject = new Document();
        updateObject.put("$set", newDocument);

        UpdateResult result = collection.updateOne(document, updateObject);
        if(!result.wasAcknowledged()){
            logger.error("Document {} failed to update", document);
        } else {
            //logger.debug("Document with trip id {} updated successfully", document.get("tripId"));
        }
    }

    @Override
    public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
        Criteria criteria =session.createCriteria(ArrivalDeparture.class);

        @SuppressWarnings("unchecked")
        List<ArrivalDeparture> results=criteria.add(Restrictions.between("time", startDate, endDate)).list();

        for(ArrivalDeparture result : results)
        {
            putArrivalDeparture(result);
        }
    }
}
