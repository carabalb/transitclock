package org.transitime.core.dataCache;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.CoreConfig;
import org.transitime.db.mongo.MongoDB;
import java.util.*;

public class HistoricalCacheService {

    private static final Logger logger = LoggerFactory
            .getLogger(HistoricalCacheService.class);

    private final static String collectionName = "historicalCacheRecords";

    private final boolean trackHistoricalCaches;

    private MongoCollection<Document> collection = null;

    // Make this class available as a singleton
    private static HistoricalCacheService singleton = new HistoricalCacheService();

    public static HistoricalCacheService getInstance() {
        if(singleton == null){
            synchronized (HistoricalCacheService.class){
                if(singleton == null){
                    return new HistoricalCacheService();
                }
            }
        }
        return singleton;
    }

    private HistoricalCacheService() {
        trackHistoricalCaches = CoreConfig.getTrackHistoricalCache();
        try {
            MongoDatabase db = MongoDB.getInstance().getDatabase();
            boolean collectionExists = db.listCollectionNames()
                    .into(new ArrayList<String>()).contains(collectionName);
            if(!collectionExists) {
                db.createCollection(collectionName, new CreateCollectionOptions());
                collection = db.getCollection(collectionName);
            } else {
                collection = db.getCollection(collectionName);
            }
        } catch (Exception e) {
            logger.error("Error connecting to MongoDB", e);
        }
    }

    public void save(CacheType cacheType, Date startDate, Date endDate){
        if(!trackHistoricalCaches || collection == null) {
            return;
        }

        Document query = new Document();
        query.append("_id", cacheType.getValue());
        Document result = collection.find(query).first();

        try {

            if (result != null) {
                Document updateDocument = new Document();
                updateDocument.put("startTime", startDate.getTime());
                updateDocument.put("endTime", endDate.getTime());
                updateDocument.put("lastUpdated", new Date());

                Document setterDocument = new Document();
                setterDocument.put("$set", updateDocument);

                collection.updateOne(result, setterDocument);
            } else {
                Document document = new Document();
                document.put("_id", cacheType.getValue());
                document.put("startTime", startDate.getTime());
                document.put("endTime", endDate.getTime());
                document.put("lastUpdated", new Date());

                collection.insertOne(document);
            }
            logger.debug("saved cache history record for cache type {} with start date {} and end date {}", cacheType.getValue(), startDate, endDate);
        } catch (Exception e){
            logger.error("unable to save cache record", e);
        }
    }

    public boolean isCacheForDateProcessed(CacheType cacheType, Date startDate, Date endDate){
        if(!trackHistoricalCaches || collection == null) {
            return false;
        }

        Document cacheRecord = new Document();
        cacheRecord.append("_id", cacheType.getValue());
        cacheRecord.append("startTime", new Document("$lte", startDate.getTime()));
        cacheRecord.append("endTime", new Document("$gte", endDate.getTime()));

        Document result = collection.find(cacheRecord).first();

        if(result != null) {
           return true;
        } else {
            return false;
        }
    }
}
