package org.transitime.core.dataCache.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.IntegerConfigValue;
import org.transitime.core.dataCache.BlockCache;
import org.transitime.core.dataCache.model.BlockCacheKey;
import org.transitime.core.dataCache.model.StopArrivalDepartureCacheKey;
import org.transitime.core.dataCache.model.TripKey;
import org.transitime.db.structs.Block;
import org.transitime.utils.Time;

import java.util.Calendar;

public class BlockCacheEhCacheImpl implements BlockCache {

    final private static String cacheName = "BlockCache";
    private static final Logger logger = LoggerFactory
            .getLogger(BlockCacheEhCacheImpl.class);

    private Cache cache = null;

    // Make this class available as a singleton
    private static BlockCacheEhCacheImpl singleton = new BlockCacheEhCacheImpl();

    public static BlockCacheEhCacheImpl getInstance() {
        if(singleton == null){
            synchronized (BlockCacheEhCacheImpl.class){
                if(singleton == null){
                    return new BlockCacheEhCacheImpl();
                }
            }
        }
        return singleton;
    }

    private BlockCacheEhCacheImpl() {
        CacheManager cm = CacheManager.getInstance();

        if (cm.getCache(cacheName) == null) {
            cm.addCache(cacheName);
        }
        cache = cm.getCache(cacheName);

        CacheConfiguration config = cache.getCacheConfiguration();

        config.setEternal(true);

        config.setMaxEntriesLocalHeap(100000);

        config.setMaxEntriesLocalDisk(100000);


    }

    @Override
    @SuppressWarnings("unchecked")
    synchronized public void put(String serviceId, String blockId, Block block) {
        BlockCacheKey key = new BlockCacheKey(serviceId, blockId);
        Element blockElement = new Element(key, block);
        cache.put(blockElement);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Block get(String serviceId, String blockId) {
        BlockCacheKey key = new BlockCacheKey(serviceId, blockId);
        Element result = cache.get(key);
        if(result==null)
            return null;
        else
            return (Block)result.getObjectValue();
    }
}
