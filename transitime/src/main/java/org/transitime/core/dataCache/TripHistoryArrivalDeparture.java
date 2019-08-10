package org.transitime.core.dataCache;

import org.transitime.db.structs.*;
import java.util.Date;
import java.util.Objects;

public class TripHistoryArrivalDeparture implements ITripHistoryArrivalDeparture {

    final private String vehicleId;

    final private Date time;

    final private String stopId;

    final private boolean isArrival;

    final private boolean isDeparture;

    final private String tripId;

    final private String blockId;

    final private String routeId;

    final private int stopPathIndex;

    final private Date scheduledTime;


    public TripHistoryArrivalDeparture(String vehicleId, Date time, String stopId, String tripId, String blockId,
                                       String routeId, int stopPathIndex, boolean isArrival, boolean isDeparture, Date scheduledTime) {
        this.vehicleId = vehicleId;
        this.time = time;
        this.stopId = stopId;
        this.tripId = tripId;
        this.blockId = blockId;
        this.routeId = routeId;
        this.stopPathIndex = stopPathIndex;
        this.isArrival = isArrival;
        this.isDeparture = isDeparture;
        this.scheduledTime = scheduledTime;
    }

    public TripHistoryArrivalDeparture(ArrivalDeparture arrivalDeparture) {
        this.vehicleId = arrivalDeparture.getVehicleId();
        this.time = arrivalDeparture.getDate();
        this.stopId = arrivalDeparture.getStopId();
        this.tripId = arrivalDeparture.getTripId();
        this.blockId = arrivalDeparture.getBlockId();
        this.routeId = arrivalDeparture.getRouteId();
        this.stopPathIndex = arrivalDeparture.getStopPathIndex();
        this.isArrival = arrivalDeparture.isArrival();
        this.isDeparture = arrivalDeparture.isDeparture();
        this.scheduledTime = arrivalDeparture.getScheduledDate();
    }

    @Override
    public String getVehicleId() {
        return vehicleId;
    }

    @Override
    public Date getDate() {
        return time;
    }

    @Override
    public String getStopId() {
        return stopId;
    }

    @Override
    public boolean isArrival() {
        return isArrival;
    }

    @Override
    public boolean isDeparture() {
        return isDeparture;
    }

    @Override
    public String getTripId() {
        return tripId;
    }

    @Override
    public String getBlockId() {
        return blockId;
    }

    @Override
    public String getRouteId() {
        return routeId;
    }

    @Override
    public int getStopPathIndex() {
        return stopPathIndex;
    }

    @Override
    public Date getScheduledDate() {
        return scheduledTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripHistoryArrivalDeparture that = (TripHistoryArrivalDeparture) o;
        return isArrival == that.isArrival &&
                isDeparture == that.isDeparture &&
                stopPathIndex == that.stopPathIndex &&
                vehicleId.equals(that.vehicleId) &&
                time.equals(that.time) &&
                stopId.equals(that.stopId) &&
                tripId.equals(that.tripId) &&
                Objects.equals(blockId, that.blockId) &&
                Objects.equals(routeId, that.routeId) &&
                Objects.equals(scheduledTime, that.scheduledTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, time, stopId, isArrival, isDeparture, tripId, blockId, routeId, stopPathIndex, scheduledTime);
    }
}
