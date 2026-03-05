//package com.ch.swaplyproduct.product.scheduler;
//
//import com.ch.swaplyproduct.product.repository.ProductRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.data.redis.core.RedisTemplate;
//
//import java.util.Set;
//
//@Component
//@RequiredArgsConstructor
//@EnableScheduling
//public class ViewCountScheduler {
//
//    private final RedisTemplate<String, Long> redisTemplate;
//    private final ProductRepository productRepository;
//
//    /* ==============================
//       조회수 DB 반영
//       ============================== */
//    @Scheduled(fixedRate = 60000)
//    public void flushViewCount() {
//        Set<String> keys = redisTemplate.keys("product:view:*");
//        if (keys == null || keys.isEmpty()) return;
//
//        for (String key : keys) {
//            String[] parts = key.split(":");
//            if (parts.length < 3) continue;
//
//            Long productId = Long.parseLong(parts[2]);
//            Long count = redisTemplate.opsForValue().get(key);
//
//            if (count != null && count > 0) {
//                productRepository.bulkIncreaseViewCount(productId, count);
//                redisTemplate.delete(key);
//            }
//        }
//    }
//
//    /* ==============================
//       좋아요 DB 반영
//       ============================== */
//    @Scheduled(fixedRate = 60000)
//    public void flushWishCount() {
//        Set<String> keys = redisTemplate.keys("product:wish:count:*");
//        if (keys == null || keys.isEmpty()) return;
//
//        for (String key : keys) {
//            String[] parts = key.split(":");
//            if (parts.length < 4) continue;
//
//            Long productId = Long.parseLong(parts[3]);
//            Long count = redisTemplate.opsForValue().get(key);
//
//            if (count != null && count != 0) {
//                productRepository.bulkIncreaseWishCount(productId, count);
//                redisTemplate.delete(key);
//            }
//        }
//    }
//}