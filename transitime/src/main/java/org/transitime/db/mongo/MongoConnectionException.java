package org.transitime.db.mongo;

public class MongoConnectionException extends Exception{
    public MongoConnectionException(){
        super("Unable to connect to mongo database");
    }
}
