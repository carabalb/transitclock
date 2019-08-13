package org.transitime.core.dataCache.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.LongConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.core.Indices;
import org.transitime.core.dataCache.KalmanErrorCache;
import org.transitime.core.dataCache.KalmanErrorCacheKey;
import org.transitime.db.mongo.MongoDB;
import org.transitime.utils.Time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

public class KalmanErrorMongoImpl implements KalmanErrorCache {

    private static final Logger logger = LoggerFactory
            .getLogger(KalmanErrorMongoImpl.class);

    final private static String collectionName = "kalmanError";

    private MongoCollection<Document> collection = null;

    private static AtomicInteger insertCounter = new AtomicInteger(0);

    private static final LongConfigValue cacheTTL = new LongConfigValue(
            "transitime.cache.maxAgeMin", new Long(7 * Time.MIN_PER_DAY),
            "Maximum amount of time for data to live in the cache");


    public KalmanErrorMongoImpl() {
        try {
            MongoDatabase db = MongoDB.getInstance().getDatabase();
            boolean collectionExists = db.listCollectionNames()
                    .into(new ArrayList<String>()).contains(collectionName);
            if(!collectionExists) {
                db.createCollection(collectionName, new CreateCollectionOptions().maxDocuments(1000000));
                collection = db.getCollection(collectionName);

            }else {
                collection = db.getCollection(collectionName);
            }
        } catch (Exception e) {
            logger.error("Error connecting to MongoDB", e);
        }
    }

    @Override
    public Double getErrorValue(KalmanErrorCacheKey key) {
        Document documentKey = new Document();
        documentKey.put("tripId", key.getTripId());
        documentKey.put("stopPathIndex", key.getStopPathIndex());

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", documentKey);

        Document result = collection.find(searchQuery).first();
        if (result != null) {
            return (Double) result.get("errorValue");
        } else {
            return null;
        }
    }

    @Override
    public Double getErrorValue(Indices indices) {
        KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);

        Document documentKey = new Document();
        documentKey.put("tripId", key.getTripId());
        documentKey.put("stopPathIndex", key.getStopPathIndex());

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", documentKey);

        Document result = collection.find(searchQuery).first();
        if (result != null) {
            return (Double) result.get("errorValue");
        } else {
            return null;
        }
    }

    @Override
    public void putErrorValue(Indices indices, Double value) {
        synchronized(insertCounter) {
            if (insertCounter.get() % 1000 == 0) {
                logger.debug("{} kalman error values added", insertCounter.get());
            }
            insertCounter.getAndIncrement();
        }

        KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);

        Document documentKey = new Document();
        documentKey.put("tripId", key.getTripId());
        documentKey.put("stopPathIndex", key.getStopPathIndex());

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", documentKey);

        Document result = collection.find(searchQuery).first();
        if (result != null) {
            updateData(result, value);
        } else {
            insertData(documentKey,value);
        }
    }

    private void insertData(Document id, Double errorValue){
        Document document = new Document();
        document.put("_id", id);
        document.put("errorValue", errorValue);
        document.put("creationDate",  new Date());

        collection.insertOne(document);
        //logger.debug("Document with trip id {} inserted successfully", document.get("tripId"));
    }

    private void updateData(Document document, Double errorValue){
        Document newDocument = new Document();
        newDocument.put("errorValue", errorValue);

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
    public List<KalmanErrorCacheKey> getKeys() {
        List<KalmanErrorCacheKey> keys = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find().projection(fields(include("_id"))).iterator();
        try {
            while (cursor.hasNext()) {
                String tripId = (String) cursor.next().get("tripId");
                Integer stopPathIndex = (Integer) cursor.next().get("stopPathIndex");
                keys.add(new KalmanErrorCacheKey(tripId, stopPathIndex));
            }
        } finally {
            if(cursor != null)
                cursor.close();
        }
        return keys;
    }
}
