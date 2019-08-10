package org.transitime.core.dataCache.factory;

import org.transitime.config.StringConfigValue;
import org.transitime.core.dataCache.KalmanErrorCache;
import org.transitime.core.dataCache.StopArrivalDepartureCache;
import org.transitime.utils.ClassInstantiator;

public class KalmanErrorCacheFactory {
    // The name of the class to instantiate
    private static StringConfigValue className =
            new StringConfigValue("transitime.cache.kalmanErrorCacheClass",
                    "org.transitime.core.dataCache.impl.KalmanErrorEhCacheImpl",
                    "Specifies the name of the class used for kalman error cache.");

    private static KalmanErrorCache singleton = null;

    /********************** Member Functions **************************/

    public static KalmanErrorCache getInstance() {
        // If the cache hasn't been created yet then do so now
        if (singleton == null) {
            synchronized (KalmanErrorCacheFactory.class){
                if(singleton ==null){
                    singleton = ClassInstantiator.instantiate(className.getValue(),
                            KalmanErrorCache.class);
                }
            }
        }
        return singleton;
    }
}
