package org.transitime.core.dataCache.model;

import org.transitime.db.structs.IArrivalDeparture;

import java.util.Date;

public interface ITripHistoryArrivalDeparture extends IArrivalDeparture {
    String getVehicleId();

    Date getDate();

    String getStopId();

    boolean isArrival();

    boolean isDeparture();

    String getTripId();

    String getBlockId();

    String getRouteId();

    int getStopPathIndex();

    Date getScheduledDate();
}
