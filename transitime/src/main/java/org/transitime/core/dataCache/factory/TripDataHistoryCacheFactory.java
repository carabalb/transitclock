package org.transitime.core.dataCache.factory;

import org.transitime.config.StringConfigValue;
import org.transitime.core.dataCache.TripDataHistoryCache;
import org.transitime.utils.ClassInstantiator;

public class TripDataHistoryCacheFactory {

    // The name of the class to instantiate
    private static StringConfigValue className =
            new StringConfigValue("transitime.cache.tripDataHistoryCacheClass",
                    "org.transitime.core.dataCache.impl.TripDataHistoryEhCacheImpl",
                    "Specifies the name of the class used for trip data history cache.");

    private static TripDataHistoryCache singleton = null;

    /********************** Member Functions **************************/

    public static TripDataHistoryCache getInstance() {
        // If the cache hasn't been created yet then do so now
        if (singleton == null) {
            synchronized (TripDataHistoryCacheFactory.class){
                if(singleton ==null){
                    singleton = ClassInstantiator.instantiate(className.getValue(),
                            TripDataHistoryCache.class);
                }
            }
        }
        return singleton;
    }
}
