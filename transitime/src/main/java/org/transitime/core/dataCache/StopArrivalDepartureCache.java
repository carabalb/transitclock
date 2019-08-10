package org.transitime.core.dataCache;

import org.hibernate.Session;
import org.transitime.db.structs.ArrivalDeparture;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface StopArrivalDepartureCache {
    @SuppressWarnings("unchecked")
    List<StopArrivalDepartureCacheKey> getKeys();

    @SuppressWarnings("unchecked")
    Set<IStopArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key);

    @SuppressWarnings("unchecked")
    StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture);

    void populateCacheFromDb(Session session, Date startDate, Date endDate);
}
