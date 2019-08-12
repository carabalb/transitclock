package org.transitime.core.dataCache.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.Block;

import javax.persistence.Transient;
import java.util.Date;
import java.util.Objects;

@JsonDeserialize(builder = StopArrivalDeparture.Builder.class)
public class StopArrivalDeparture implements IStopArrivalDeparture, Comparable<StopArrivalDeparture> {

    private final String vehicleId;

    private final Date date;

    private final String stopId;

    private final boolean isArrival;

    private final boolean isDeparture;

    private final String tripId;

    private final String blockId;

    private final String routeId;

    private final int stopPathIndex;

    private final Date scheduledDate;

    @Transient
    @JsonIgnore
    private final Block block;

    private final String serviceId;

    private final int tripIndex;

    private StopArrivalDeparture(String vehicleId,
                                Date time,
                                String stopId,
                                String tripId,
                                String blockId,
                                String routeId,
                                int stopPathIndex,
                                boolean isArrival,
                                boolean isDeparture,
                                Date scheduledDate,
                                Block block,
                                String serviceId,
                                int tripIndex) {
        this.vehicleId = vehicleId;
        this.date = time;
        this.stopId = stopId;
        this.tripId = tripId;
        this.blockId = blockId;
        this.routeId = routeId;
        this.stopPathIndex = stopPathIndex;
        this.isArrival = isArrival;
        this.isDeparture = isDeparture;
        this.scheduledDate = scheduledDate;
        this.block = block;
        this.serviceId = serviceId;
        this.tripIndex = tripIndex;
    }

    public StopArrivalDeparture(ArrivalDeparture arrivalDeparture) {
        this.vehicleId = arrivalDeparture.getVehicleId();
        this.date = arrivalDeparture.getDate();
        this.stopId = arrivalDeparture.getStopId();
        this.tripId = arrivalDeparture.getTripId();
        this.blockId = arrivalDeparture.getBlockId();
        this.routeId = arrivalDeparture.getRouteId();
        this.stopPathIndex = arrivalDeparture.getStopPathIndex();
        this.isArrival = arrivalDeparture.isArrival();
        this.isDeparture = arrivalDeparture.isDeparture();
        this.scheduledDate = arrivalDeparture.getScheduledDate();
        this.block = arrivalDeparture.getBlock();
        this.serviceId = arrivalDeparture.getServiceId();
        this.tripIndex = arrivalDeparture.getTripIndex();
    }

    @Override
    public String getVehicleId() {
        return vehicleId;
    }

    @Override
    public Date getDate() {
        return date;
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
        return scheduledDate;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public int getTripIndex() {
        return tripIndex;
    }

    @JsonPOJOBuilder
    static class Builder {
        String vehicleId;
        Date date;
        String stopId;
        boolean isArrival;
        boolean isDeparture;
        String tripId = null;
        String blockId = null;
        String routeId = null;
        int stopPathIndex;
        Date scheduledDate = null;
        String serviceId = null;
        int tripIndex;

        Builder withVehicleId(String vehicleId) {
            this.vehicleId = vehicleId;
            return this;
        }

        Builder withDate(Date date) {
            this.date = date;
            return this;
        }

        Builder withStopId(String stopId) {
            this.stopId = stopId;
            return this;
        }

        Builder withArrival(boolean isArrival) {
            this.isArrival = isArrival;
            return this;
        }

        Builder withDeparture(boolean isDeparture) {
            this.isDeparture = isDeparture;
            return this;
        }

        Builder withTripId(String tripId) {
            this.tripId = tripId;
            return this;
        }
        Builder withBlockId(String blockId) {
            this.blockId = blockId;
            return this;
        }

        Builder withRouteId(String routeId) {
            this.routeId = routeId;
            return this;
        }

        Builder withStopPathIndex(int stopPathIndex) {
            this.stopPathIndex = stopPathIndex;
            return this;
        }

        Builder withScheduledDate(Date scheduledDate) {
            this.scheduledDate = scheduledDate;
            return this;
        }

        Builder withServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        Builder withTripIndex(int tripIndex) {
            this.tripIndex = tripIndex;
            return this;
        }


        public StopArrivalDeparture build() {
            return new StopArrivalDeparture(vehicleId, date, stopId, tripId, blockId, routeId, stopPathIndex, isArrival, isDeparture, scheduledDate, null, serviceId, tripIndex);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopArrivalDeparture that = (StopArrivalDeparture) o;
        return isArrival == that.isArrival &&
                isDeparture == that.isDeparture &&
                stopPathIndex == that.stopPathIndex &&
                tripIndex == that.tripIndex &&
                Objects.equals(vehicleId, that.vehicleId) &&
                Objects.equals(date, that.date) &&
                Objects.equals(stopId, that.stopId) &&
                Objects.equals(tripId, that.tripId) &&
                Objects.equals(blockId, that.blockId) &&
                Objects.equals(routeId, that.routeId) &&
                Objects.equals(scheduledDate, that.scheduledDate) &&
                Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, date, stopId, isArrival, isDeparture, tripId, blockId, routeId, stopPathIndex, scheduledDate, serviceId, tripIndex);
    }

    @Override
    public int compareTo(StopArrivalDeparture stopArrivalDeparture) {
        if(this.getDate().getTime()<stopArrivalDeparture.getDate().getTime())
        {
            return -1;
        }else if(this.getDate().getTime()> stopArrivalDeparture.getDate().getTime())
        {
            return 1;
        }
        else if(!this.equals(stopArrivalDeparture)){
            return 1;
        }
        return 0;
    }
}
