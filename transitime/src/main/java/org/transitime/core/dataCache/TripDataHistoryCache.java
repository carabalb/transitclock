package org.transitime.core.dataCache;

import org.hibernate.Session;
import org.transitime.core.dataCache.model.ITripHistoryArrivalDeparture;
import org.transitime.core.dataCache.model.TripKey;
import org.transitime.db.structs.ArrivalDeparture;

import java.util.Date;
import java.util.Set;

public interface TripDataHistoryCache {

    ITripHistoryArrivalDeparture findPreviousArrivalEvent(Set<ITripHistoryArrivalDeparture> arrivalDepartures, ITripHistoryArrivalDeparture current);

    @SuppressWarnings("unchecked")
    Set<ITripHistoryArrivalDeparture> getTripHistory(TripKey tripKey);

    @SuppressWarnings("unchecked")
    TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture);

    void populateCacheFromDb(Session session, Date startDate, Date endDate);

    ITripHistoryArrivalDeparture findPreviousDepartureEvent(Set<ITripHistoryArrivalDeparture> arrivalDepartures, ITripHistoryArrivalDeparture current);

    Set<ITripHistoryArrivalDeparture> getTripHistory(String tripId, Date date, Integer starttime);

    boolean isCacheForDateProcessed(Date startDate, Date endDate);

    void saveCacheHistoryRecord(Date startDate, Date endDate);
}
