package org.transitime.core.dataCache;

import org.transitime.db.structs.PredictionForStopPath;

import java.util.List;

public interface StopPathPredictionsCache {
    @SuppressWarnings("unchecked")
    List<PredictionForStopPath> getPredictions(StopPathCacheKey key);

    void putPrediction(PredictionForStopPath prediction);

    @SuppressWarnings("unchecked")
    void putPrediction(StopPathCacheKey key, PredictionForStopPath prediction);

    List<StopPathCacheKey> getKeys();
}
