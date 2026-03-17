package com.ch.swaplyproduct.service;

import com.ch.swaplyproduct.client.MemberInternalClient;
import com.ch.swaplyproduct.common.exception.CannotWishOwnProductException;
import com.ch.swaplyproduct.message.WishAddedMessage;
import com.ch.swaplyproduct.message.WishNotificationPublisher;
import com.ch.swaplyproduct.product.dto.WishResponse;
import com.ch.swaplyproduct.product.entity.Product;
import com.ch.swaplyproduct.product.entity.ProductImage;
import com.ch.swaplyproduct.product.entity.Wish;
import com.ch.swaplyproduct.product.repository.ProductImageRepository;
import com.ch.swaplyproduct.product.repository.ProductRepository;
import com.ch.swaplyproduct.product.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishService {

    private static final Duration WISH_NOTIFICATION_TTL = Duration.ofHours(24);

    private final WishRepository wishRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WishNotificationPublisher wishNotificationPublisher;
    private final MemberInternalClient memberInternalClient;

    @Transactional
    public void addWish(Long productId, Long memberId) {
        if (wishRepository.findByMemberIdAndProductId(memberId, productId).isPresent()) {
            log.info("이미 찜한 상품: memberId={}, productId={}", memberId, productId);
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. productId=" + productId));

        if (product.getSellerId().equals(memberId)) {
            throw new CannotWishOwnProductException();
        }

        Wish wish = Wish.create(memberId, productId);
        wishRepository.save(wish);

        redisTemplate.opsForValue().increment("product:wish:count:" + productId);
        redisTemplate.opsForSet().add("product:wish:users:" + productId, memberId);

        log.info("찜 추가 완료: memberId={}, productId={}", memberId, productId);

        boolean shouldNotify = shouldSendWishNotification(productId, memberId);
        log.info("알림 발송 여부: memberId={}, productId={}, shouldNotify={}", memberId, productId, shouldNotify);

        if (shouldNotify) {
            publishWishAddedNotification(product, memberId);
        } else {
            log.info("찜 알림 스킵(중복 방지): memberId={}, productId={}", memberId, productId);
        }
    }

    @Transactional
    public void removeWish(Long productId, Long memberId) {
        Wish wish = wishRepository.findByMemberIdAndProductId(memberId, productId)
                .orElse(null);

        if (wish == null) {
            log.info("찜 내역 없음(이미 취소됨): memberId={}, productId={}", memberId, productId);
            return;
        }

        wishRepository.delete(wish);

        redisTemplate.opsForValue().decrement("product:wish:count:" + productId);
        redisTemplate.opsForSet().remove("product:wish:users:" + productId, memberId);

        log.info("찜 취소 완료: memberId={}, productId={}", memberId, productId);
    }

    @Transactional(readOnly = true)
    public Page<WishResponse> getWishList(Long memberId, Pageable pageable) {
        Page<Wish> wishes = wishRepository.findByMemberId(memberId, pageable);

        List<WishResponse> responses = wishes.getContent().stream()
                .map(wish -> {
                    Product product = productRepository.findById(wish.getProductId()).orElse(null);
                    if (product == null) return null;

                    ProductImage thumbnail = productImageRepository
                            .findByProductAndIsThumbnailTrue(product)
                            .orElse(null);

                    return WishResponse.of(wish, product, thumbnail);
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, wishes.getTotalElements());
    }

    @Transactional(readOnly = true)
    public boolean isWished(Long productId, Long memberId) {
        return wishRepository.findByMemberIdAndProductId(memberId, productId).isPresent();
    }

    private boolean shouldSendWishNotification(Long productId, Long memberId) {
        String key = "wish:notify:" + productId + ":" + memberId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", WISH_NOTIFICATION_TTL);

        log.info("wish notify key={}, success={}", key, success);

        return Boolean.TRUE.equals(success);
    }

    private void publishWishAddedNotification(Product product, Long wishMemberId) {
        try {
            String thumbnailUrl = productImageRepository
                    .findByProductAndIsThumbnailTrue(product)
                    .map(ProductImage::getImageUrl)
                    .orElse("");

            String wishMemberNickname = memberInternalClient.getNickname(wishMemberId);

            WishAddedMessage message = new WishAddedMessage(
                    product.getProductId(),
                    product.getTitle(),
                    thumbnailUrl,
                    wishMemberId,
                    wishMemberNickname,
                    product.getSellerId()
            );

            wishNotificationPublisher.publishWishAdded(message);

        } catch (Exception e) {
            log.error("[WishService] 찜 추가 알림 발행 실패: productId={}, error={}",
                    product.getProductId(), e.getMessage(), e);
        }
    }
}