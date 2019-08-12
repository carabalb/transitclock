/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitime.core.predictiongenerator;

import java.util.*;

import org.apache.commons.lang3.time.DateUtils;
import org.transitime.applications.Core;
import org.transitime.config.IntegerConfigValue;
import org.transitime.core.Indices;
import org.transitime.core.VehicleState;
import org.transitime.core.dataCache.*;
import org.transitime.core.dataCache.factory.TripDataHistoryCacheFactory;
import org.transitime.core.dataCache.factory.StopArrivalDepartureCacheFactory;
import org.transitime.core.dataCache.model.IStopArrivalDeparture;
import org.transitime.core.dataCache.model.ITripHistoryArrivalDeparture;
import org.transitime.core.dataCache.model.StopArrivalDepartureCacheKey;
import org.transitime.core.dataCache.model.TripKey;
import org.transitime.db.structs.Block;
import org.transitime.gtfs.DbConfig;

/**
 * Commonly-used methods for PredictionGenerators that use historical cached data.
 */
public class HistoricalPredictionLibrary {

	private static final IntegerConfigValue closestVehicleStopsAhead = new IntegerConfigValue(
			"transitime.prediction.closestvehiclestopsahead", new Integer(2),
			"Num stops ahead a vehicle must be to be considers in the closest vehicle calculation");
	
	public static long getLastVehicleTravelTime(VehicleState currentVehicleState, Indices indices) {

		StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(
				indices.getStopPath().getStopId(),
				new Date(currentVehicleState.getMatch().getAvlTime()));
										
		/* TODO how do we handle the the first stop path. Where do we get the first stop id. */ 		 
		if(!indices.atBeginningOfTrip())
		{						
			String currentStopId = indices.getPreviousStopPath().getStopId();
			
			StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(currentStopId,
					new Date(currentVehicleState.getMatch().getAvlTime()));
	
			Set<IStopArrivalDeparture> currentStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(currentStopKey);
	
			Set<IStopArrivalDeparture> nextStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(nextStopKey);
	
			if (currentStopList != null && nextStopList != null) {
				// lists are already sorted when put into cache.
				for (IStopArrivalDeparture currentArrivalDeparture : currentStopList) {
					
					if(currentArrivalDeparture.isDeparture() && currentArrivalDeparture.getVehicleId() != currentVehicleState.getVehicleId())
					{
						IStopArrivalDeparture found;
											
						if ((found = findMatchInList(nextStopList, currentArrivalDeparture)) != null) {
							if(found.getDate().getTime() - currentArrivalDeparture.getDate().getTime()>0)
							{																
								return found.getDate().getTime() - currentArrivalDeparture.getDate().getTime();
							}else
							{
								// must be going backwards
								return -1;
							}
						}else
						{
							return -1;
						}
					}
				}
			}
		}
		return -1;
	}
	public static Indices getLastVehicleIndices(VehicleState currentVehicleState, Indices indices) {

		StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(
				indices.getStopPath().getStopId(),
				new Date(currentVehicleState.getMatch().getAvlTime()));
										
		/* TODO how do we handle the the first stop path. Where do we get the first stop id. */ 		 
		if(!indices.atBeginningOfTrip())
		{						
			String currentStopId = indices.getPreviousStopPath().getStopId();
			
			StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(currentStopId,
					new Date(currentVehicleState.getMatch().getAvlTime()));
	
			Set<IStopArrivalDeparture> currentStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(currentStopKey);
	
			Set<IStopArrivalDeparture> nextStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(nextStopKey);
	
			if (currentStopList != null && nextStopList != null) {
				// lists are already sorted when put into cache.
				for (IStopArrivalDeparture currentArrivalDeparture : currentStopList) {
					
					if(currentArrivalDeparture.isDeparture() && currentArrivalDeparture.getVehicleId() != currentVehicleState.getVehicleId())
					{
						IStopArrivalDeparture found;
											
						if ((found = findMatchInList(nextStopList, currentArrivalDeparture)) != null) {
							if(found.getDate().getTime() - currentArrivalDeparture.getDate().getTime()>0)
							{	
								Block currentBlock=null;
								/* block is transient in arrival departure so when read from database need to get from dbconfig. */ 
								if(currentArrivalDeparture.getBlock()==null&&currentArrivalDeparture.getServiceId()!=null && currentArrivalDeparture.getBlockId()!=null)
								{																																			
									DbConfig dbConfig = Core.getInstance().getDbConfig();
									
									currentBlock=dbConfig.getBlock(currentArrivalDeparture.getServiceId(), currentArrivalDeparture.getBlockId());
								}else
								{
									currentBlock=currentArrivalDeparture.getBlock();
								}				
								if(currentBlock!=null)
									return new Indices(currentBlock, currentArrivalDeparture.getTripIndex(), found.getStopPathIndex(), 0);
							}else
							{
								// must be going backwards
								return null;
							}
						}else
						{
							return null;
						}
					}
				}
			}
		}
		return null;
	}
	/* TODO could also make it a requirement that it is on the same route as the one we are generating prediction for */
	private static IStopArrivalDeparture findMatchInList(Set<IStopArrivalDeparture> nextStopList,
														 IStopArrivalDeparture currentArrivalDeparture) {
		for (IStopArrivalDeparture nextStopArrivalDeparture : nextStopList) {
			if (currentArrivalDeparture.getVehicleId() == nextStopArrivalDeparture.getVehicleId()
					&& currentArrivalDeparture.getTripId() == nextStopArrivalDeparture.getTripId()
					&&  currentArrivalDeparture.isDeparture() && nextStopArrivalDeparture.isArrival() ) {
				return nextStopArrivalDeparture;
			}
		}
		return null;
	}

	private static VehicleState getClosestVehicle(List<VehicleState> vehiclesOnRoute, Indices indices,
			VehicleState currentVehicleState) {

		Map<String, List<String>> stopsByDirection = currentVehicleState.getTrip().getRoute()
				.getOrderedStopsByDirection();

		List<String> routeStops = stopsByDirection.get(currentVehicleState.getTrip().getDirectionId());

		Integer closest = 100;

		VehicleState result = null;

		for (VehicleState vehicle : vehiclesOnRoute) {

			Integer numAfter = numAfter(routeStops, vehicle.getMatch().getStopPath().getStopId(),
					currentVehicleState.getMatch().getStopPath().getStopId());
			if (numAfter != null && numAfter > closestVehicleStopsAhead.getValue() && numAfter < closest) {
				closest = numAfter;
				result = vehicle;
			}
		}
		return result;
	}

	private static boolean isAfter(List<String> stops, String stop1, String stop2) {
		if (stops != null && stop1 != null && stop2 != null) {
			if (stops.contains(stop1) && stops.contains(stop2)) {
				if (stops.indexOf(stop1) > stops.indexOf(stop2))
					return true;
				else
					return false;
			}
		}
		return false;
	}

	private static Integer numAfter(List<String> stops, String stop1, String stop2) {
		if (stops != null && stop1 != null && stop2 != null)
			if (stops.contains(stop1) && stops.contains(stop2))
				return stops.indexOf(stop1) - stops.indexOf(stop2);

		return null;
	}

	public static List<Integer> lastDaysTimes(TripDataHistoryCache cache, String tripId, int stopPathIndex, Date startDate,
                                              Integer startTime, int num_days_look_back, int num_days) {

		List<Integer> times = new ArrayList<Integer>();
		Set<ITripHistoryArrivalDeparture> results = null;
		int num_found = 0;
		/*
		 * TODO This could be smarter about the dates it looks at by looking at
		 * which services use this trip and only 1ook on day service is
		 * running
		 */

		for (int i = 0; i < num_days_look_back && num_found < num_days; i++) {

			Date nearestDay = DateUtils.truncate(DateUtils.addDays(startDate, (i + 1) * -1), Calendar.DAY_OF_MONTH);

			TripKey tripKey = new TripKey(tripId, nearestDay, startTime);

			results = cache.getTripHistory(tripKey);

			if (results != null) {

                ITripHistoryArrivalDeparture arrival = getArrival(stopPathIndex, results);

                ITripHistoryArrivalDeparture departure = TripDataHistoryCacheFactory.getInstance().findPreviousDepartureEvent(results, arrival);
														
				if (arrival != null && departure != null) {

					times.add(new Integer((int) (timeBetweenStops(departure, arrival))));
						num_found++;
				}			
			}
		}
		return times;		
	}
	private static ITripHistoryArrivalDeparture getArrival(int stopPathIndex, Set<ITripHistoryArrivalDeparture> results)
	{
		for(ITripHistoryArrivalDeparture result:results)
		{
			if(result.isArrival()&&result.getStopPathIndex()==stopPathIndex)
			{
				return result;
			}
		}
		return null;	
	}
	private static long timeBetweenStops(ITripHistoryArrivalDeparture ad1, ITripHistoryArrivalDeparture ad2) {
		
		return Math.abs(ad2.getDate().getTime() - ad1.getDate().getTime());
	}

	private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}

}
