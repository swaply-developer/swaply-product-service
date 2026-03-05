package com.ch.swaplyproduct.product.scheduler;

import com.ch.swaplyproduct.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class ProductMetricScheduler {

    private final RedisTemplate<String, Long> redisTemplate;
    private final ProductRepository productRepository;

    /* ==============================
       조회수 및 좋아요 DB 반영
       ============================== */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void flushMetrics() {

        // ===== 조회수 반영 =====
        Set<String> viewKeys = redisTemplate.keys("product:view:*");
        if (viewKeys != null) {
            for (String key : viewKeys) {
                Long productId = Long.parseLong(key.split(":")[2]);
                Long count = redisTemplate.opsForValue().get(key);

                if (count != null && count > 0) {
                    productRepository.bulkIncreaseViewCount(productId, count);
                    redisTemplate.delete(key);
                }
            }
        }

        // ===== 좋아요 반영 =====
        Set<String> wishKeys = redisTemplate.keys("product:wish:count:*");
        if (wishKeys != null) {
            for (String key : wishKeys) {
                Long productId = Long.parseLong(key.split(":")[3]);
                Long count = redisTemplate.opsForValue().get(key);

                if (count != null && count != 0) {
                    productRepository.bulkIncreaseWishCount(productId, count);
                    redisTemplate.delete(key);
                }
            }
        }
    }
}