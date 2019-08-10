package org.transitime.db.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.configData.MongoDbSetupConfig;

import javax.annotation.PreDestroy;
import java.util.Arrays;

public class MongoDB {

    private static final Logger logger = LoggerFactory
            .getLogger(MongoDB.class);

    private static MongoDB singleton = new MongoDB();



    private MongoClient mongoClient = null;

    private MongoDatabase database = null;


    /**
     * Gets the singleton instance of this class.
     *
     * @return
     */
    public static MongoDB getInstance() {
        return singleton;
    }

    private MongoDB() {
        try {
            String username = MongoDbSetupConfig.getMongoUsername();
            char[] password = MongoDbSetupConfig.getMongoPassword().toCharArray();

            String databaseName = MongoDbSetupConfig.getMongoDbName();
            String host = MongoDbSetupConfig.getMongoHost();
            Integer port = MongoDbSetupConfig.getMongoPort();

            MongoCredential credential = MongoCredential.createCredential(username,databaseName, password);
            ServerAddress mongoServerAddress = new ServerAddress(host, port);
            MongoClient mongoClient = new MongoClient(mongoServerAddress);
            database = mongoClient.getDatabase(databaseName);

            if(database != null){
                logger.info("Succesfully connected to mongo db database {}", databaseName);
            }

        } catch (Exception e) {
            logger.error("Error connecting to MongoDB", e);
        }
    }

    public MongoDatabase getDatabase() throws MongoConnectionException{
        if(database == null){
            throw new MongoConnectionException();
        }
        return database;
    }

    @PreDestroy
    public void destroy(){
        if(mongoClient != null)
        mongoClient.close();
    }
}
