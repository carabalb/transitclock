package org.transitime.core.dataCache.impl;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.core.Indices;
import org.transitime.core.dataCache.KalmanErrorCache;
import org.transitime.core.dataCache.KalmanErrorCacheKey;

/**
 * @author Sean Og Crudden
 *
 */
public class KalmanErrorEhCacheImpl implements KalmanErrorCache {
	final private static String cacheName = "KalmanErrorCache";
	private static final Logger logger = LoggerFactory
			.getLogger(KalmanErrorEhCacheImpl.class);

	private Cache cache = null;

	public KalmanErrorEhCacheImpl() {
		CacheManager cm = CacheManager.getInstance();

		if (cm.getCache(cacheName) == null) {
			cm.addCache(cacheName);
		}
		cache = cm.getCache(cacheName);

		CacheConfiguration config = cache.getCacheConfiguration();

		config.setEternal(true);

		config.setMaxEntriesLocalHeap(1000000);

		config.setMaxEntriesLocalDisk(1000000);
	}

    public void logCache(Logger logger)
	{
		logger.debug("Cache content log.");
		@SuppressWarnings("unchecked")
		List<KalmanErrorCacheKey> keys = cache.getKeys();

		for(KalmanErrorCacheKey key : keys)
		{
			Element result=cache.get(key);
			if(result!=null)
			{
				logger.debug("Key: "+key.toString());

				Double value=(Double) result.getObjectValue();

				logger.debug("Error value: "+value);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	synchronized public Double getErrorValue(Indices indices) {

		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);

		Element result = cache.get(key);

		if(result==null)
			return null;
		else
			return (Double)result.getObjectValue();
	}

	@Override
	@SuppressWarnings("unchecked")
	synchronized public Double getErrorValue(KalmanErrorCacheKey key) {

		Element result = cache.get(key);

		if(result==null)
			return null;
		else
			return (Double)result.getObjectValue();
	}

    @Override
    @SuppressWarnings("unchecked")
	synchronized public void putErrorValue(Indices indices, Double value) {

		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		Element errorElement = new Element(key, value);

		cache.put(errorElement);
	}

    @Override
    public List<KalmanErrorCacheKey> getKeys()
	{
		@SuppressWarnings("unchecked")
		List<KalmanErrorCacheKey> keys = cache.getKeys();
		return keys;
	}
}
