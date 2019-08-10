package org.transitime.core.dataCache.factory;

import org.transitime.config.StringConfigValue;
import org.transitime.core.dataCache.KalmanErrorCache;
import org.transitime.core.dataCache.StopPathPredictionsCache;
import org.transitime.utils.ClassInstantiator;

public class StopPathPredictionCacheFactory {
    // The name of the class to instantiate
    private static StringConfigValue className =
            new StringConfigValue("transitime.cache.stopPathCacheClass",
                    "org.transitime.core.dataCache.impl.StopPathPredictionEhCacheImpl",
                    "Specifies the name of the class used for stop path prediction cache.");

    private static StopPathPredictionsCache singleton = null;

    /********************** Member Functions **************************/

    public static StopPathPredictionsCache getInstance() {
        // If the cache hasn't been created yet then do so now
        if (singleton == null) {
            synchronized (StopPathPredictionCacheFactory.class){
                if(singleton ==null){
                    singleton = ClassInstantiator.instantiate(className.getValue(),
                            StopPathPredictionsCache.class);
                }
            }
        }
        return singleton;
    }
}
