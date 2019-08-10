package org.transitime.core.dataCache.factory;

import org.transitime.config.StringConfigValue;
import org.transitime.core.dataCache.KalmanErrorCache;
import org.transitime.core.dataCache.StopArrivalDepartureCache;
import org.transitime.utils.ClassInstantiator;

public class StopArrivalDepartureCacheFactory {
    // The name of the class to instantiate
    private static StringConfigValue className =
            new StringConfigValue("transitime.cache.stopArrivalDepartureCacheClass",
                    "org.transitime.core.dataCache.impl.StopArrivalDepartureEhCacheImpl",
                    "Specifies the name of the class used for stop arrival-departure cache.");

    private static StopArrivalDepartureCache singleton = null;

    /********************** Member Functions **************************/

    public static StopArrivalDepartureCache getInstance() {
        // If the cache hasn't been created yet then do so now
        if (singleton == null) {
            synchronized (StopArrivalDepartureCacheFactory.class){
                if(singleton ==null){
                    singleton = ClassInstantiator.instantiate(className.getValue(),
                            StopArrivalDepartureCache.class);
                }
            }
        }
        return singleton;
    }


}
