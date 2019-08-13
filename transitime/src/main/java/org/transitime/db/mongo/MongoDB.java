package org.transitime.db.mongo;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            String password = MongoDbSetupConfig.getMongoPassword();
            String host = MongoDbSetupConfig.getMongoHost();
            String databaseName = MongoDbSetupConfig.getMongoDbName();
            Integer port = MongoDbSetupConfig.getMongoPort();
            boolean isSSLEnabled = MongoDbSetupConfig.getMongoSslEnabled();
            boolean isSSLInvalidHostNameAllowed = MongoDbSetupConfig.getInvalidHostnameAllowed();

            MongoClientOptions options = MongoClientOptions.builder()
                                            .sslEnabled(isSSLEnabled)
                                            .sslInvalidHostNameAllowed(isSSLInvalidHostNameAllowed)
                                            .build();

            ServerAddress mongoServerAddress = new ServerAddress(host, port);
            MongoClient mongoClient = null;

            if(username == null || password == null){
                mongoClient = new MongoClient(mongoServerAddress, options);
            } else {
                MongoCredential credential = MongoCredential.createCredential(username,databaseName, password.toCharArray());
                mongoClient = new MongoClient(mongoServerAddress, Arrays.asList(credential), options);
            }

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
