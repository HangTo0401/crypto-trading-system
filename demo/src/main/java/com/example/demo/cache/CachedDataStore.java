package com.example.demo.cache;

import com.example.demo.dto.CacheableDTO;
import com.example.demo.utils.Constants;
import com.google.common.base.Stopwatch;
import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.google.common.base.Stopwatch.createStarted;

@Log4j2
public class CachedDataStore {

    @Getter
    private boolean initialized = false;

    private CacheManager cacheManager;

    private CachedDataStore() {
        init();
    }

    private void init() {
        cacheManager = CacheManager.newInstance(getClass().getResource("/ehcache.xml"));
    }

    public Cache getPricingCache() {
        return cacheManager.getCache(CacheName.PRICING.getName());
    }

    private <T extends Serializable, V extends CacheableDTO<T>>void reloadCache(final Cache cache, @NotNull final Supplier<List<V>> supplier) {
        final String cacheName = cache.getName();
        Stopwatch stopwatch = createStarted();

        try {
            log.info("Loading data for {} cache to refresh cache.", cacheName);
            final List<? extends CacheableDTO<T>> dtos = supplier.get();

            log.info("Adding items to {} cache.", cacheName);
            List<T> oldCacheKeys = cache.getKeys();
            List<T> updatedCacheKeys = new ArrayList<>();

            if (dtos != null) {
                dtos.forEach(dto -> {
                    cache.put(new Element(dto.getCachingId(), dto));
                    updatedCacheKeys.add(dto.getCachingId());
                });
            }

            oldCacheKeys = (List<T>) CollectionUtils.subtract(oldCacheKeys, updatedCacheKeys);
            log.info("Added {} items to {} cache.", updatedCacheKeys.size(), cacheName);

            cache.removeAll(oldCacheKeys);

            log.info("{} Old items were removed from {} cache.", oldCacheKeys.size(), cacheName);

        } catch (Exception t) {
            log.error(cacheName + " check refresh failed!", t);
        } finally {
            stopwatch.stop();
            long executionTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);

            log.info(Constants.EXEC_TIME_LOG_MS, cacheName + " cache refresh", executionTime);
        }
    }

//    private void reloadPricingCache(final Cache cache, @NotNull final Supplier<List<V>> supplier) {
//        List<? extends CacheEntry<T>> deltaCacheEntries = deltaLoadFunc.apply(
//                getLastCacheUpdatedTimestamp(cacheName).orElse(null));
//
//        reloadCache(getPricingCache(), () -> templateItemFolders);
//    }

    public boolean reloadCaches() {
        if (initialized) {
            return false;
        }

//        reloadPricingCache();

        initialized = true;

        return true;
    }
}
