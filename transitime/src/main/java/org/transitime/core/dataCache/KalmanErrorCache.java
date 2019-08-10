package org.transitime.core.dataCache;

import org.slf4j.Logger;
import org.transitime.core.Indices;
import org.transitime.core.dataCache.KalmanErrorCacheKey;

import java.util.List;

public interface KalmanErrorCache {

    @SuppressWarnings("unchecked")
    Double getErrorValue(Indices indices);


    @SuppressWarnings("unchecked")
    Double getErrorValue(KalmanErrorCacheKey key);

    @SuppressWarnings("unchecked")
    void putErrorValue(Indices indices, Double value);

    List<KalmanErrorCacheKey> getKeys();
}
