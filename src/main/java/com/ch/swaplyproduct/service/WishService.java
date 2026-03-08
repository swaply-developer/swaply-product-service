package com.ch.swaplyproduct.service;

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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishService {

    private final WishRepository wishRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // =====================================================
    // 찜 추가
    // =====================================================
    @Transactional
    public void addWish(Long productId, Long memberId) {
        // 중복 체크
        if (wishRepository.findByMemberIdAndProductId(memberId, productId).isPresent()) {
            log.info("이미 찜한 상품: memberId={}, productId={}", memberId, productId);
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. productId=" + productId));

        // DB 저장
        Wish wish = Wish.create(memberId, productId);
        wishRepository.save(wish);

        // Redis wishCount 증가 (UI 실시간 반영용)
        redisTemplate.opsForValue().increment("product:wish:count:" + productId);
        redisTemplate.opsForSet().add("product:wish:users:" + productId, memberId);

        // Product wishCount 동기화 (선택적 - 스케줄러로 해도 됨)
        log.info("찜 추가 완료: memberId={}, productId={}", memberId, productId);
    }

    // =====================================================
    // 찜 취소
    // =====================================================
    @Transactional
    public void removeWish(Long productId, Long memberId) {
        Wish wish = wishRepository.findByMemberIdAndProductId(memberId, productId)
                .orElse(null);

        if (wish == null) {
            log.info("찜 내역 없음(이미 취소됨): memberId={}, productId={}", memberId, productId);
            return;
        }

        wishRepository.delete(wish);

        // Redis 동기화
        redisTemplate.opsForValue().decrement("product:wish:count:" + productId);
        redisTemplate.opsForSet().remove("product:wish:users:" + productId, memberId);

        log.info("찜 취소 완료: memberId={}, productId={}", memberId, productId);
    }

    // =====================================================
    // 내 찜 목록 조회 (페이징)
    // =====================================================
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

    // =====================================================
    // 특정 상품 찜 여부 확인
    // =====================================================
    @Transactional(readOnly = true)
    public boolean isWished(Long productId, Long memberId) {
        return wishRepository.findByMemberIdAndProductId(memberId, productId).isPresent();
    }
}
