package org.transitime.core.dataCache;

import org.transitime.db.structs.Block;

public interface BlockCache {
    @SuppressWarnings("unchecked")
    void put(String serviceId, String blockId, Block block);

    @SuppressWarnings("unchecked")
    Block get(String serviceId, String blockId);
}
