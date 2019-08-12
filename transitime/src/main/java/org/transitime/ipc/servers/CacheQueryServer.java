/**
 * 
 */
package org.transitime.ipc.servers;

import java.rmi.RemoteException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.core.dataCache.*;
import org.transitime.core.dataCache.factory.TripDataHistoryCacheFactory;
import org.transitime.core.dataCache.factory.KalmanErrorCacheFactory;
import org.transitime.core.dataCache.factory.StopArrivalDepartureCacheFactory;
import org.transitime.core.dataCache.impl.HistoricalAverageCacheImpl;
import org.transitime.ipc.data.*;
import org.transitime.ipc.interfaces.CacheQueryInterface;
import org.transitime.ipc.rmi.AbstractServer;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 * @author Sean Og Crudden Server to allow cache content to be queried.
 */
public class CacheQueryServer extends AbstractServer implements CacheQueryInterface {
	// Should only be accessed as singleton class
	private static CacheQueryServer singleton;

	private static final Logger logger = LoggerFactory.getLogger(CacheQueryServer.class);

	protected CacheQueryServer(String agencyId) {
		super(agencyId, CacheQueryInterface.class.getSimpleName());

	}

	/**
	 * Starts up the CacheQueryServer so that RMI calls can be used to query
	 * cache. This will automatically cause the object to continue to run and
	 * serve requests.
	 * 
	 * @param agencyId
	 * @return the singleton CacheQueryServer object. Usually does not need to
	 *         used since the server will be fully running.
	 */
	public static CacheQueryServer start(String agencyId) {
		if (singleton == null) {
			singleton = new CacheQueryServer(agencyId);
		}

		if (!singleton.getAgencyId().equals(agencyId)) {
			logger.error(
					"Tried calling CacheQueryServer.start() for "
							+ "agencyId={} but the singleton was created for agencyId={}",
					agencyId, singleton.getAgencyId());
			return null;
		}

		return singleton;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.transitime.ipc.interfaces.CacheQueryInterface#
	 * getStopArrivalDepartures(java.lang.String)
	 */
	@Override
	public List<IpcArrivalDeparture> getStopArrivalDepartures(String stopId) throws RemoteException {

		try {
			StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(stopId,
					Calendar.getInstance().getTime());

			Set<IStopArrivalDeparture> result = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(nextStopKey);

			List<IpcArrivalDeparture> ipcResultList = new ArrayList<IpcArrivalDeparture>();

			for (IStopArrivalDeparture arrivalDeparture : result) {
				ipcResultList.add(new IpcArrivalDeparture(arrivalDeparture));
			}
			return ipcResultList;			
		} catch (Exception e) {

			throw new RemoteException(e.toString(),e);
		}
	}

	@Override
	public Integer entriesInCache(String cacheName) throws RemoteException {

		CacheManager cm = CacheManager.getInstance();
		Cache cache = cm.getCache(cacheName);
		if (cache != null)
			return cache.getSize();
		else
			return null;

	}

	@Override
	public IpcHistoricalAverage getHistoricalAverage(String tripId, Integer stopPathIndex) throws RemoteException {
		StopPathCacheKey key = new StopPathCacheKey(tripId, stopPathIndex);

		HistoricalAverage average = HistoricalAverageCacheImpl.getInstance().getAverage(key);
		return new IpcHistoricalAverage(average);
	}

	@Override
	public List<IpcArrivalDeparture> getTripArrivalDepartures(String tripId, Date date, Integer starttime)
			throws RemoteException {
		
		try {
			List<ITripHistoryArrivalDeparture> result = new ArrayList<>(TripDataHistoryCacheFactory.getInstance().getTripHistory(tripId, date, starttime));
			
			List<IpcArrivalDeparture> ipcResultList = new ArrayList<IpcArrivalDeparture>();
			
			Collections.sort(result, new CachedArrivalDepartureComparator());
			
			for (ITripHistoryArrivalDeparture arrivalDeparture : result) {
				ipcResultList.add(new IpcArrivalDeparture(arrivalDeparture));
			}

			return ipcResultList;

		} catch (Exception e) {

			throw new RemoteException(e.toString(), e);
		}		
	}

	@Override
	public List<IpcHistoricalAverageCacheKey> getHistoricalAverageCacheKeys() throws RemoteException {
		
		List<StopPathCacheKey> keys = HistoricalAverageCacheImpl.getInstance().getKeys();
		List<IpcHistoricalAverageCacheKey> ipcResultList = new ArrayList<IpcHistoricalAverageCacheKey>();
				
		for(StopPathCacheKey key:keys)
		{
			ipcResultList.add(new IpcHistoricalAverageCacheKey(key));
		}
		return ipcResultList;
	}

	@Override
	public Double getKalmanErrorValue(String tripId, Integer stopPathIndex) throws RemoteException {
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(tripId, stopPathIndex);
		Double result = KalmanErrorCacheFactory.getInstance().getErrorValue(key);
		return result;
	}

	@Override
	public List<IpcKalmanErrorCacheKey> getKalmanErrorCacheKeys() throws RemoteException {
		List<KalmanErrorCacheKey> keys = KalmanErrorCacheFactory.getInstance().getKeys();
		List<IpcKalmanErrorCacheKey> ipcResultList = new ArrayList<IpcKalmanErrorCacheKey>();
				
		for(KalmanErrorCacheKey key:keys)
		{
			ipcResultList.add(new IpcKalmanErrorCacheKey(key));
		}
		return ipcResultList;
	}

}
