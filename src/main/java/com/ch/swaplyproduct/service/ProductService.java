// 파일: src/main/java/com/ch/swaplyproduct/service/ProductService.java
package com.ch.swaplyproduct.service;

import com.ch.swaplyproduct.product.dto.ProductCreateRequest;
import com.ch.swaplyproduct.product.dto.ProductResponse;
import com.ch.swaplyproduct.product.entity.Brand;
import com.ch.swaplyproduct.product.entity.Category;
import com.ch.swaplyproduct.product.entity.Product;
import com.ch.swaplyproduct.product.entity.ProductStatus;
import com.ch.swaplyproduct.product.repository.BrandRepository;
import com.ch.swaplyproduct.product.repository.CategoryRepository;
import com.ch.swaplyproduct.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /* =====================================================
       1️⃣ 상품 등록 (카테고리/브랜드 연동)
       ===================================================== */
// ProductService.java

    @Transactional
    public ProductResponse create(ProductCreateRequest request, List<MultipartFile> images) {
        // 1. 카테고리 조회
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음: " + request.getCategoryId()));

        // 2. 브랜드 조회 (선택)
        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId()).orElse(null);
        }

        // 3. Product 엔티티 생성 (tradeType 추가 전달)
        Product product = Product.create(
                request.getSellerId(),
                category,
                brand,
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getTradeType() // 👈 파라미터 추가
        );

        Product savedProduct = productRepository.save(product);

        // 4. 이미지 정보 저장 (테이블이 비어있지 않게 하려면 필요)
        if (images != null && !images.isEmpty()) {
            // 실제 파일 저장 로직은 생략하더라도 DB에 경로는 남겨야 함
            // images.forEach(img -> { ... productImageRepository.save(...) });
        }

        return ProductResponse.from(savedProduct);
    }

    /* =====================================================
       2️⃣ 상품 상세 조회 (조회수 Redis 증가)
       ===================================================== */
    @Transactional(readOnly = true)
    public ProductResponse getDetail(Long productId) {

        // Redis 조회수 증가
        redisTemplate.opsForValue().increment("product:view:" + productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        return ProductResponse.from(product);
    }

    /* =====================================================
       3️⃣ 좋아요 추가
       ===================================================== */
    @Transactional
    public void addWish(Long productId, Long userId) {

        String userKey = "product:wish:users:" + productId;
        String countKey = "product:wish:count:" + productId;

        Long added = redisTemplate.opsForSet().add(userKey, userId);
        if (added != null && added == 1) {
            redisTemplate.opsForValue().increment(countKey);
        }
    }

    /* =====================================================
       4️⃣ 좋아요 취소
       ===================================================== */
    @Transactional
    public void removeWish(Long productId, Long userId) {

        String userKey = "product:wish:users:" + productId;
        String countKey = "product:wish:count:" + productId;

        Long removed = redisTemplate.opsForSet().remove(userKey, userId);
        if (removed != null && removed == 1) {
            redisTemplate.opsForValue().decrement(countKey);
        }
    }

    /* =====================================================
       5️⃣ 상품 상태 변경 (동시성 안전)
       ===================================================== */
    @Transactional
    public void changeStatus(Long productId,
                             ProductStatus currentStatus,
                             ProductStatus newStatus) {

        int updated = productRepository.updateStatusIfMatch(productId, currentStatus, newStatus);
        if (updated == 0) {
            throw new IllegalStateException("상태 변경 실패 (동시성 충돌)");
        }
    }
    /* =====================================================
   6️⃣ 상품 목록 조회 (페이징 + 필터링)
   ===================================================== */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getList(
            Long sellerId,       // null이면 전체
            Integer categoryId,  // null이면 전체
            String keyword,      // null이면 전체
            ProductStatus status,// null이면 전체 상태
            Pageable pageable
    ) {
        Page<Product> products;

        if (sellerId != null) {
            // 판매자별 조회
            products = productRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
        } else if (categoryId != null && status != null) {
            // 카테고리별 + 상태
            products = productRepository.findByCategoryIdAndStatus(categoryId, status, pageable);
        } else if (keyword != null && status != null) {
            // 제목 검색 + 상태
            products = productRepository.findByTitleContainingAndStatus(keyword, status, pageable);
        } else if (status != null) {
            // 상태별 조회
            products = productRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            // 전체 조회 (최신순)
            products = productRepository.findAll(pageable);
        }

        // DTO 변환
        return products.map(ProductResponse::from);
    }
}