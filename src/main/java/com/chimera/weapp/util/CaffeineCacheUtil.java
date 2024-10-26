package com.chimera.weapp.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CaffeineCacheUtil {

    // 创建缓存实例，使用自定义的Expiry策略
    private static final Cache<String, CacheValue> CACHE = Caffeine.newBuilder()
            .expireAfter(new Expiry<String, CacheValue>() {
                @Override
                public long expireAfterCreate(String key, CacheValue value, long currentTime) {
                    // 根据写入缓存时的指定时间返回过期时间
                    return TimeUnit.MILLISECONDS.toNanos(value.getExpireAfterWrite());
                }

                @Override
                public long expireAfterUpdate(String key, CacheValue value, long currentTime, long currentDuration) {
                    return currentDuration;  // 更新时不改变过期时间
                }

                @Override
                public long expireAfterRead(String key, CacheValue value, long currentTime, long currentDuration) {
                    return currentDuration;  // 读取时不改变过期时间
                }
            })
            .maximumSize(100) // 最大缓存容量
            .build();

    /**
     * 添加缓存项并设置过期时间
     *
     * @param key              缓存键
     * @param value            缓存值
     * @param expireAfterWrite 过期时间，单位毫秒
     */
    public static void put(String key, Object value, long expireAfterWrite) {
        CACHE.put(key, new CacheValue(value, expireAfterWrite));
    }

    /**
     * 获取缓存项
     *
     * @param key 缓存键
     * @return 缓存值
     */
    public static Optional<Object> get(String key) {
        CacheValue cacheValue = CACHE.getIfPresent(key);
        return Optional.ofNullable(cacheValue).map(CacheValue::getValue);
    }

    /**
     * 删除缓存项
     *
     * @param key 缓存键
     */
    public static void remove(String key) {
        CACHE.invalidate(key);
    }

    /**
     * 清空所有缓存
     */
    public static void clear() {
        CACHE.invalidateAll();
    }

    /**
     * 缓存值封装类，包含缓存项和其独立的过期时间
     */
    private static class CacheValue {
        private final Object value;
        private final long expireAfterWrite;

        public CacheValue(Object value, long expireAfterWrite) {
            this.value = value;
            this.expireAfterWrite = expireAfterWrite;
        }

        public Object getValue() {
            return value;
        }

        public long getExpireAfterWrite() {
            return expireAfterWrite;
        }
    }
}
