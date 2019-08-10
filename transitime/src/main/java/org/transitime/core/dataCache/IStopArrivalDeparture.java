package org.transitime.core.dataCache;

import org.transitime.db.structs.Block;
import org.transitime.db.structs.IArrivalDeparture;

import java.util.Date;

public interface IStopArrivalDeparture extends IArrivalDeparture {
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

    Block getBlock();

    String getServiceId();

    int getTripIndex();
}
