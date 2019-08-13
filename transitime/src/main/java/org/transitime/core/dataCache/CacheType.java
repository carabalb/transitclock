package org.transitime.core.dataCache;

public enum CacheType {

    TRIP_DATA_HISTORY("tripDataHistory"),
    KALMAN_ERROR("kalmanError"),
    STOP_ARRIVAL_DEPARTURE("stopArrivalDeparture"),
    STOP_PATH_PREDICTIONS("stopPathPredictions");

    private String value;

    CacheType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CacheType fromValue(String value) {
        for (CacheType cacheType :CacheType.values()){
            if (cacheType.getValue().equals(value)){
                return cacheType;
            }
        }
        throw new UnsupportedOperationException(
                "The value " + value + " is not supported!");
    }
}