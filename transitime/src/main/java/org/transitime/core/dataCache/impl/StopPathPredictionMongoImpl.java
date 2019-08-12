package org.transitime.core.dataCache.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.core.dataCache.model.StopPathCacheKey;
import org.transitime.core.dataCache.StopPathPredictionsCache;
import org.transitime.db.mongo.MongoDB;
import org.transitime.db.structs.PredictionForStopPath;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

public class StopPathPredictionMongoImpl implements StopPathPredictionsCache {
    private static final Logger logger = LoggerFactory
            .getLogger(StopPathPredictionMongoImpl.class);

    private static final Gson gson = new Gson();

    final private static String collectionName = "stopPathPredictions";

    private MongoCollection<Document> collection = null;

    private static AtomicInteger insertCounter = new AtomicInteger(0);

    public StopPathPredictionMongoImpl() {
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
    public List<PredictionForStopPath> getPredictions(StopPathCacheKey key) {
        Document documentKey = new Document();
        documentKey.put("tripId", key.getTripId());
        documentKey.put("stopPathIndex", key.getStopPathIndex());
        documentKey.put("travelTime", key.isTravelTime());

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", documentKey);

        Document result = collection.find(searchQuery).first();
        if (result != null) {
            String predictionsForStopPathJson = result.get("predictionsForStopPath").toString();
            Type listType = new TypeToken<ArrayList<PredictionForStopPath>>() {}.getType();
            List<PredictionForStopPath> predictionsForStopPath = gson.fromJson(predictionsForStopPathJson, listType);
            return predictionsForStopPath;
        } else {
            return null;
        }
    }

    @Override
    public void putPrediction(PredictionForStopPath prediction) {
        synchronized(insertCounter) {
            if (insertCounter.get() % 1000 == 0) {
                logger.debug("{} stop path predictions added", insertCounter.get());
            }
            insertCounter.getAndIncrement();
        }

        StopPathCacheKey key=new StopPathCacheKey(prediction.getTripId(), prediction.getStopPathIndex());

        Document documentKey = new Document();
        documentKey.put("tripId", key.getTripId());
        documentKey.put("stopPathIndex", key.getStopPathIndex());
        documentKey.put("travelTime", key.isTravelTime());

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", documentKey);

        List<PredictionForStopPath> list = null;

        Document result = collection.find(searchQuery).first();
        if (result != null) {
            String predictionsForStopPathJson = result.get("predictionsForStopPath").toString();
            Type listType = new TypeToken<ArrayList<PredictionForStopPath>>(){}.getType();
            list = gson.fromJson(predictionsForStopPathJson, listType);
            list.add(prediction);
            updateData(result, list);
        } else {
            list = new ArrayList<PredictionForStopPath>();
            list.add(prediction);
            insertData(documentKey,list);
        }
    }

    @Override
    public void putPrediction(StopPathCacheKey key, PredictionForStopPath prediction) {

    }

    private void insertData(Document id, List<PredictionForStopPath> list){
        Document document = new Document();
        document.put("_id", id);
        document.put("creationDate",  new Date());

        String predictionsForStopPath = gson.toJson(list);
        document.put("predictionsForStopPath", predictionsForStopPath);

        collection.insertOne(document);

        //logger.debug("Document with trip id {} inserted successfully", document.get("tripId"));
    }

    private void updateData(Document document, List<PredictionForStopPath> list){
        String predictionsForStopPath = gson.toJson(list);

        Document newDocument = new Document();
        newDocument.put("predictionsForStopPath", predictionsForStopPath);

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
    public List<StopPathCacheKey> getKeys() {
        List<StopPathCacheKey> keys = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find().projection(fields(include("_id"))).iterator();

        while(cursor.hasNext()){
            String tripId = (String) cursor.next().get("tripId");
            Integer stopPathIndex = (Integer) cursor.next().get("stopPathIndex");
            boolean travelTime = (Boolean) cursor.next().get("travelTime");
            keys.add(new StopPathCacheKey(tripId, stopPathIndex, travelTime));
        }
        return keys;
    }
}
