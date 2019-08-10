package org.transitime.core.dataCache;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CacheUtil {

    public static ITripHistoryArrivalDeparture findPreviousDepartureEvent(Set<ITripHistoryArrivalDeparture> arrivalDepartures, ITripHistoryArrivalDeparture current)
    {
        for (ITripHistoryArrivalDeparture tocheck : emptyIfNull(arrivalDepartures))
        {
            if(tocheck.getStopPathIndex()==(current.getStopPathIndex()-1) && (current.isArrival() && tocheck.isDeparture()))
            {
                return tocheck;
            }
        }
        return null;
    }

    public static ITripHistoryArrivalDeparture findPreviousArrivalEvent(Set<ITripHistoryArrivalDeparture> arrivalDepartures, ITripHistoryArrivalDeparture current) {
        for (ITripHistoryArrivalDeparture tocheck : emptyIfNull(arrivalDepartures))
        {
            if(tocheck.getStopPathIndex()==(current.getStopPathIndex()-1) && (current.isDeparture() && tocheck.isArrival()))
            {
                return tocheck;
            }
        }
        return null;
    }

    private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
        return iterable == null ? Collections.<T> emptyList() : iterable;
    }
}
