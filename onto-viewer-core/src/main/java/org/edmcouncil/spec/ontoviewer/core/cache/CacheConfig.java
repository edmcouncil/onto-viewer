package org.edmcouncil.spec.ontoviewer.core.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Michał Daniel (michal.daniel@makolab.com)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CacheNames.DEPRECATED.label);
    }

    public enum CacheNames {
        DEPRECATED("deprecated");

        public final String label;

        private CacheNames(String label) {
            this.label = label;
        }

    }
}
