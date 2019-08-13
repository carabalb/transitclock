/**
 *
 */
package org.transitime.core.dataCache.impl;

import java.util.*;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.Policy;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.IntegerConfigValue;
import org.transitime.core.dataCache.model.ITripHistoryArrivalDeparture;
import org.transitime.core.dataCache.TripDataHistoryCache;
import org.transitime.core.dataCache.model.TripKey;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.Trip;
import org.transitime.gtfs.DbConfig;
import org.transitime.utils.Time;

/**
 * @author Sean Og Crudden
 * 		   This is a Cache to hold historical arrival departure data for trips. It
 *         is intended to look up a trips historical data when a trip starts and
 *         place in cache for use in generating predictions based on a Kalman
 *         filter. Uses Ehcache for caching rather than just using a concurrent
 *         hashmap. This approach to holding data in memory for transitime needs
 *         to be proven.
 *
 *         TODO this could do with an interface, factory class, and alternative implementations, perhaps using Infinispan.
 */
public class TripDataHistoryEhCacheImpl implements TripDataHistoryCache {
	private static boolean debug = false;

	final private static String cacheByTrip = "arrivalDeparturesByTrip";

	private static final Logger logger = LoggerFactory
			.getLogger(TripDataHistoryEhCacheImpl.class);

	private Cache cache = null;

	/**
	 * Default is 4 as we need 3 days worth for Kalman Filter implementation
	 */
	private static final IntegerConfigValue tripDataCacheMaxAgeSec = new IntegerConfigValue(
			"transitime.tripdatacache.tripDataCacheMaxAgeSec",
			15 * Time.SEC_PER_DAY,
			"How old an arrivaldeparture has to be before it is removed from the cache ");

	public TripDataHistoryEhCacheImpl() {
		CacheManager cm = CacheManager.getInstance();
		EvictionAgePolicy evictionPolicy = null;
		if(tripDataCacheMaxAgeSec!=null)
		{
			evictionPolicy = new EvictionAgePolicy(
					tripDataCacheMaxAgeSec.getValue() * Time.MS_PER_SEC);
		}else
		{
			evictionPolicy = new EvictionAgePolicy(
					15 * Time.SEC_PER_DAY *Time.MS_PER_SEC);
		}

		if (cm.getCache(cacheByTrip) == null) {
			cm.addCache(cacheByTrip);
		}
		cache = cm.getCache(cacheByTrip);

		//CacheConfiguration config = cache.getCacheConfiguration();
		/*TODO We need to refine the eviction policy. */
		cache.setMemoryStoreEvictionPolicy(evictionPolicy);
	}

	public void logCache(Logger logger)
	{
		logger.debug("Cache content log.");
		@SuppressWarnings("unchecked")
		List<TripKey> keys = cache.getKeys();

		for(TripKey key : keys)
		{
			Element result=cache.get(key);
			if(result!=null)
			{
				logger.debug("Key: "+key.toString());
				@SuppressWarnings("unchecked")

				List<ArrivalDeparture> ads=(List<ArrivalDeparture>) result.getObjectValue();

				for(ArrivalDeparture ad : ads)
				{
					logger.debug(ad.toString());
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<ITripHistoryArrivalDeparture> getTripHistory(TripKey tripKey) {

		//logger.debug(cache.toString());

		Element result = cache.get(tripKey);

		if(result!=null)
		{
			return (Set<ITripHistoryArrivalDeparture>) result.getObjectValue();
		}
		else
		{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	synchronized public TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {

		logger.debug("Putting :"+arrivalDeparture.toString() + " in TripDataHistory cache.");
		/* just put todays time in for last three days to aid development. This means it will kick in in 1 days rather than 3. Perhaps be a good way to start rather than using default transiTime method but I doubt it. */
		int days_back=1;
		if(debug)
			days_back=3;
		TripKey tripKey=null;

		for(int i=0;i < days_back;i++)
		{
			Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);

			nearestDay=DateUtils.addDays(nearestDay, i*-1);

			DbConfig dbConfig = Core.getInstance().getDbConfig();

			Trip trip=dbConfig.getTrip(arrivalDeparture.getTripId());

			tripKey = new TripKey(arrivalDeparture.getTripId(),
					nearestDay,
					trip.getStartTime());

			List<ArrivalDeparture> list = null;

			Element result = cache.get(tripKey);

			if (result != null && result.getObjectValue() != null) {
				list = (List<ArrivalDeparture>) result.getObjectValue();
				cache.remove(tripKey);
			} else {
				list = new ArrayList<ArrivalDeparture>();
			}

			list.add(arrivalDeparture);

			Element arrivalDepartures = new Element(tripKey, Collections.synchronizedList(list));

			cache.put(arrivalDepartures);


		}
		return tripKey;
	}

	@Override
	public void populateCacheFromDb(Session session, Date startDate, Date endDate)
	{
		Criteria criteria =session.createCriteria(ArrivalDeparture.class);

		@SuppressWarnings("unchecked")
		List<ArrivalDeparture> results=criteria.add(Restrictions.between("time", startDate, endDate)).list();

		for(ArrivalDeparture result : results)
		{
			putArrivalDeparture(result);
		}
	}

	@Override
	public ITripHistoryArrivalDeparture findPreviousArrivalEvent(Set<ITripHistoryArrivalDeparture> arrivalDepartures, ITripHistoryArrivalDeparture current)
	{
		for (ITripHistoryArrivalDeparture tocheck : emptyIfNull(arrivalDepartures))
		{
			if(tocheck.getStopPathIndex()==(current.getStopPathIndex()-1) && (current.isDeparture() && tocheck.isArrival()))
			{
				return tocheck;
			}
		}
		return null;
	}

	@Override
	public ITripHistoryArrivalDeparture findPreviousDepartureEvent(Set<ITripHistoryArrivalDeparture> arrivalDepartures,ITripHistoryArrivalDeparture current)
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

	@Override
	public Set<ITripHistoryArrivalDeparture> getTripHistory(String tripId, Date date, Integer starttime) {

		if(tripId!=null && date!=null && starttime!=null){
			TripKey tripKey = new TripKey(tripId, date, starttime);
			return getTripHistory(tripKey);
		}

		Set<ITripHistoryArrivalDeparture> results = new HashSet<>();

		if(tripId!=null && date!=null && starttime==null)
		{
			for(TripKey key: getKeys())
			{
				if(key.getTripId().equals(tripId) && date.compareTo(key.getTripStartDate())==0)
				{
					results.addAll(getTripHistory(key));
				}
			}
		}else if(tripId!=null && date==null && starttime==null)
		{
			for(TripKey key: getKeys())
			{
				if(key.getTripId().equals(tripId))
				{
					results.addAll(getTripHistory(key));
				}
			}
		}
		else if(tripId==null && date!=null && starttime==null)
		{
			for(TripKey key: getKeys())
			{
				if(date.compareTo(key.getTripStartDate())==0)
				{
					results.addAll(getTripHistory(key));
				}
			}
		}

		return results;
	}

	private List<TripKey> getKeys()
	{
		return cache.getKeys();
	}

	@Override
	public boolean isCacheForDateProcessed(Date startDate, Date endDate){
		logger.debug("isCacheForDateProcessed not implemented");
		return false;
	}

	@Override
	public void saveCacheHistoryRecord(Date startDate, Date endDate) {
		logger.debug("saveCacheHistory not implemented");
		return;
	}

	private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}
	/**
	 * 	This policy evicts arrival departures from the cache
	 *  when they are X (age) number of milliseconds old
	 *
	 */
	private class EvictionAgePolicy implements Policy {
		private String name = "AGE";

		private long age = 0L;

		public EvictionAgePolicy(long age) {
			super();
			this.age = age;
		}

		@Override
		public boolean compare(Element arg0, Element arg1) {
			if (arg0.getObjectKey() instanceof TripKey
					&& arg1.getObjectKey() instanceof TripKey) {
				if (((TripKey) arg0.getObjectKey()).getTripStartDate().after(
						((TripKey) arg1.getObjectKey()).getTripStartDate())) {
					return true;
				}
				if (((TripKey) arg0.getObjectKey()).getTripStartDate()
						.compareTo(
								((TripKey) arg1.getObjectKey())
										.getTripStartDate()) == 0) {
					if (((TripKey) arg0.getObjectKey()).getStartTime() > ((TripKey) arg1
							.getObjectKey()).getStartTime()) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Element selectedBasedOnPolicy(Element[] arg0, Element arg1) {

			for (int i = 0; i < arg0.length; i++) {

				if (arg0[i].getObjectKey() instanceof TripKey) {
					TripKey key = (TripKey) arg0[i].getObjectKey();

					if (Calendar.getInstance().getTimeInMillis()
							- key.getTripStartDate().getTime()
							+ (key.getStartTime().intValue() * 1000) > age) {
						return arg0[i];
					}
				}
			}
			return null;
		}
	}
}
