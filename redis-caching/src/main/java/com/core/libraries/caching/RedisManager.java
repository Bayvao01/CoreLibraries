package com.core.libraries.caching;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.core.libraries.caching.RedisConfig.CacheConfig;

/**
 * @author Bayvao Verma
 *
 */
@Component("redisCacheManager")
public class RedisManager {
	private static RedisManager INSTANCE; //NOSONAR

	private RedisConfig redisConfig;
	private CacheManager cacheManager;
	private CacheManager transactionAwareCacheManager;
	private RedisTemplate<String, Object> redisTemplate;
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	public RedisManager(RedisConfig redisConfig) {
		Assert.notNull(redisConfig, "Redis Config can't be null");
		this.redisConfig = redisConfig;
		init();
	}

	protected RedisConfig getRedisConfig() {
		return redisConfig;
	}

	/**
	 * Initializing Redis connection
	 */
	protected void init() {
		this.cacheManager = createCacheManager(false);
		this.transactionAwareCacheManager = createCacheManager(true);
		this.redisTemplate = createRedisTemplate();
		this.stringRedisTemplate = new StringRedisTemplate(getRedisConfig().getRedisConnectionFactory());

		/**
		 * Initialize as Singleton. Replace with last initialized copy.
		 */

		synchronized (RedisManager.class) {
			INSTANCE = this;
		}
	}

	protected RedisTemplate<String, Object> createRedisTemplate() {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(getRedisConfig().getRedisConnectionFactory());
		template.afterPropertiesSet();
		return template;

	}

	private CacheManager createCacheManager(boolean transactionAware) {

		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

		String cacheKeyPrefix = getRedisConfig().getCacheKeyPrefix();
		String keyPrefix = null;
		RedisCacheConfiguration redisCacheConfiguration = null;
		String cacheName = null;
		CacheConfig cacheConfig = null;

		for (Entry<String, CacheConfig> cacheEntry : getRedisConfig().getCahceNames().entrySet()) {
			cacheConfig = cacheEntry.getValue();
			if (cacheConfig.isTransactionAwarePut() != transactionAware) {
				continue;
			}

			cacheName = cacheEntry.getKey();
			if (StringUtils.hasText(cacheKeyPrefix)) {
				keyPrefix = cacheKeyPrefix + "::" + cacheName + "::";
			} else {
				keyPrefix = cacheName + "::";
			}
			
			redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().disableCachingNullValues().prefixCacheNameWith(keyPrefix);

			if (cacheConfig.isStringValueOnly()) {
				redisCacheConfiguration = redisCacheConfiguration
						.serializeValuesWith(SerializationPair.fromSerializer(new StringRedisSerializer()));
			}
			if (cacheConfig.getExpiresInSeconds() > 0) {
				redisCacheConfiguration = redisCacheConfiguration
						.entryTtl(Duration.ofSeconds(cacheConfig.getExpiresInSeconds()));
			}
			cacheConfigurations.put(cacheName, redisCacheConfiguration);

		}

		RedisCacheManagerBuilder builder = RedisCacheManager.builder(getRedisConfig().getRedisConnectionFactory());
		builder = builder.withInitialCacheConfigurations(cacheConfigurations).disableCreateOnMissingCache();

		if (transactionAware) {
			builder = builder.transactionAware();
		}

		RedisCacheManager redisCacheManager = builder.build();
		redisCacheManager.afterPropertiesSet();

		return redisCacheManager;
	}

	/**
	 * Returns singleton instance of this class initialized earlier.
	 * 
	 * @return an initialized instance of RedisManager.
	 */
	protected static RedisManager getInstance() {
		Assert.notNull(INSTANCE, "RedisManager not initialized. Instantiate at least once before using it");
		return INSTANCE;
	}

	/**
	 * Returns cache associated with input cache name from appropriate CacheManager.
	 * 
	 * @param cacheName cache name
	 * @return an instance of Cache associated with input cache name
	 */
	public static Cache getCache(String cacheName) {
		Assert.hasText(cacheName, "cacheName can't be null or blank");
		CacheManager cacheManager;
		CacheConfig cacheConfig = getInstance().getRedisConfig().getCahceNames().get(cacheName);

		if (cacheConfig != null && cacheConfig.isTransactionAwarePut()) {
			cacheManager = getInstance().transactionAwareCacheManager;
		} else {
			cacheManager = getInstance().cacheManager;
		}

		Assert.notNull(cacheManager, "cacheManager can't be null, init() incomplete.");

		return cacheManager.getCache(cacheName);
	}

	/**
	 * Returns singleton instance of StringRedisTemplate initialized earlier.
	 * 
	 * @return an instance of StringRedisTemplate.
	 */
	public static StringRedisTemplate getStringRedisTemplate() {
		return getInstance().stringRedisTemplate;
	}

	/**
	 * Returns singleton instance of RedisTemplate initialized earlier.
	 * 
	 * @return an instance of RedisTemplate.
	 */
	public static RedisTemplate<String, Object> getRedisTemplate() {
		return getInstance().redisTemplate;
	}

	/**
	 * Returns input key prefixed with configured environment name.
	 * 
	 * @param cacheKey
	 * @return an input key prefixed with configured environment name.
	 */
	public static String getPrefixKey(String cacheKey) {
		return getInstance().getRedisConfig().getPrefixKey(cacheKey);
	}
}
