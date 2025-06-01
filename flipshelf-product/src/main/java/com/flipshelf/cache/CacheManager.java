package com.flipshelf.cache;

import com.flipshelf.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Component
public class CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
    private static final String CACHE_PREFIX = "products:";
    private static final long CACHE_TTL_MINUTES = 30;
    @Autowired
    private RedisTemplate<String, List<Product>> redisTemplate;

    public Page<Product> getCachedProducts(String cacheKey, Pageable pageable) {
        try {
            List<Product> cachedList = redisTemplate.opsForValue().get(cacheKey);
            if (cachedList != null) {
                Page<Product> cachedPage = new PageImpl<>(cachedList, pageable, cachedList.size());
                logger.info("Cache hit for key: {}, products: {} ", cacheKey, cachedPage.getTotalElements());
                return cachedPage;
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve cached data for key: {}", cacheKey, e);
        }
        logger.info("Cache miss for key: {}", cacheKey);
        return null;
    }

    public void cacheProducts(String cacheKey, Page<Product> productsPage) {
        try {
            redisTemplate.opsForValue().set(cacheKey, productsPage.getContent(), Duration.ofMinutes(CACHE_TTL_MINUTES));
            logger.info("Cached products for key: {}, products: {}", cacheKey, productsPage.getTotalElements());
        } catch (Exception e) {
            logger.error("Failed to cache products for key: {}", cacheKey, e);
        }
    }

    public void invalidateCache(String sellerEmail) {
        // Invalidate admin/user cache (all products)
        String adminUserPattern = CACHE_PREFIX + "all:*";
        deleteKeysByPattern(adminUserPattern);

        // Invalidate seller-specific cache
        String sellerPattern = CACHE_PREFIX + "seller:" + sellerEmail + ":*";
        deleteKeysByPattern(sellerPattern);

        logger.info("Invalidated cache for seller: {} and admin/user", sellerEmail);
    }

    private void deleteKeysByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.debug("Deleted {} keys matching pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            logger.error("Failed to delete keys for pattern: {}", pattern, e);
        }
    }

    public String generateCacheKey(String role, String email, int page, int size) {
        return switch (role) {
            case "ROLE_SELLER" -> String.format("products:seller:%s:page:%d:size:%d", email, page, size);
            case "ROLE_ADMIN", "ROLE_USER" -> String.format("products:all:page:%d:size:%d", page, size);
            default -> String.format("products:unknown:page:%d:size:%d", page, size);
        };
    }
}
