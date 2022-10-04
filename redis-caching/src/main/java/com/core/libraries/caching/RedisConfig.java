package com.core.libraries.caching;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Configuration
@ConfigurationProperties(prefix = "redis")
@ImportResource("redis-cache-config.xml")
public class RedisConfig {

	public static class CacheConfig {
		private long expiresInSeconds;
		private boolean stringValueOnly;
		private boolean transactionAwarePut;

		public long getExpiresInSeconds() {
			return expiresInSeconds;
		}

		public void setExpiresInSeconds(long expiresInSeconds) {
			this.expiresInSeconds = expiresInSeconds;
		}

		public boolean isStringValueOnly() {
			return stringValueOnly;
		}

		public void setStringValueOnly(boolean stringValueOnly) {
			this.stringValueOnly = stringValueOnly;
		}

		public boolean isTransactionAwarePut() {
			return transactionAwarePut;
		}

		public void setTransactionAwarePut(boolean transactionAwarePut) {
			this.transactionAwarePut = transactionAwarePut;
		}

	}

	private String cacheKeyPrefix;
	private Map<String, CacheConfig> cacheNames = new HashMap<>();
	private RedisConnectionFactory redisConnectionFactory;

	@Autowired
	public RedisConfig(RedisConnectionFactory redisConnectionFactory) {
		Assert.notNull(redisConnectionFactory, "Redis Connection Factory cannot be null");
	}

	public RedisConnectionFactory getRedisConnectionFactory() {
		return redisConnectionFactory;
	}

	public Map<String, CacheConfig> getCahceNames() {
		return cacheNames;
	}

	public String getCacheKeyPrefix() {
		return cacheKeyPrefix;
	}
	
	public void setCacheKeyPrefix(String cacheKeyPrefix) {
		this.cacheKeyPrefix = cacheKeyPrefix;
	}
	
	
	public String getPrefixKey(String cacheKey) {
		return (StringUtils.hasText(getCacheKeyPrefix())) ? (getCacheKeyPrefix() + "::" + cacheKey) : cacheKey;
	}
}
