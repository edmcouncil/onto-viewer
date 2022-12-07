package org.edmcouncil.spec.ontoviewer.core.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Component;

/**
 * Created by Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class CoreCache {

    private final CacheManager cacheManager;

    public CoreCache(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public CacheManager getCacheManager() {
//        cacheManager.getCache("deprecated").put("key", true);
//        cacheManager.getCache("deprecated").put("key", false);
//        cacheManager.getCache("label").put("l1", "label");
//        System.out.println( "cache: " +  cacheManager.getCache("deprecated").get("key", Boolean.class));
//        System.out.println( "cache: " +  cacheManager.getCache("label").get("l1", String.class));
        return cacheManager;
    }


}
