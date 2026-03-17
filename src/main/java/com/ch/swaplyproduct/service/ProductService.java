package com.ch.swaplyproduct.service;

import com.ch.swaplyproduct.message.WishNotificationPublisher;
import com.ch.swaplyproduct.message.WishPriceMessage;
import com.ch.swaplyproduct.product.dto.ProductCreateRequest;
import com.ch.swaplyproduct.product.dto.ProductResponse;
import com.ch.swaplyproduct.product.dto.ProductSummaryDto;
import com.ch.swaplyproduct.product.entity.*;
import com.ch.swaplyproduct.product.repository.BrandRepository;
import com.ch.swaplyproduct.product.repository.CategoryRepository;
import com.ch.swaplyproduct.product.repository.ProductImageRepository;
import com.ch.swaplyproduct.product.repository.ProductRepository;
import com.ch.swaplyproduct.product.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final BrandRepository brandRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // 신규 의존성
    private final WishRepository wishRepository;
    private final WishNotificationPublisher wishNotificationPublisher;

    /* =====================================================
       1️⃣ 상품 등록 (카테고리/브랜드 연동)
       ===================================================== */
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, List<MultipartFile> images, Long memberId) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        Brand brand = (request.getBrandId() != null)
                ? brandRepository.findById(request.getBrandId()).orElse(null)
                : null;

        Product product = Product.create(
                memberId, category, brand,
                request.getTitle(), request.getDescription(),
                request.getPrice(), request.getTradeType()
        );
        Product savedProduct = productRepository.save(product);

        if (images != null && !images.isEmpty()) {
            String uploadDir = "uploads/";
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            System.out.println("==== [이미지 저장 시작] ====");
            System.out.println("절대 경로: " + dir.getAbsolutePath());

            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                String filename = savedProduct.getProductId() + "_" + i + ".jpg";
                java.io.File dest = new java.io.File(dir.getAbsolutePath() + java.io.File.separator + filename);
                try {
                    file.transferTo(dest);
                    System.out.println("파일 저장 성공: " + dest.getAbsolutePath());
                    ProductImage pi = ProductImage.create(savedProduct, "/uploads/" + filename, (i == 0), i);
                    productImageRepository.save(pi);
                } catch (Exception e) {
                    System.err.println("파일 저장 실패: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("이미지 저장 중 오류 발생", e);
                }
            }
            System.out.println("==== [이미지 저장 종료] ====");
        }

        return ProductResponse.from(savedProduct);
    }

    /* =====================================================
       2️⃣ 상품 상세 조회
       ===================================================== */
    @Transactional(readOnly = true)
    public ProductResponse getDetail(Long productId) {
        redisTemplate.opsForValue().increment("product:view:" + productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        List<ProductImage> imgs =
                productImageRepository.findByProduct_ProductIdOrderBySortOrderAsc(productId);

        return ProductResponse.fromWithImages(product, imgs);
    }

    /* =====================================================
       3️⃣ 좋아요 추가 (ProductService 내부 Redis 처리)
       ===================================================== */
    @Transactional
    public void addWish(Long productId, Long userId) {
        String userKey  = "product:wish:users:" + productId;
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
        String userKey  = "product:wish:users:" + productId;
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
    public void changeStatus(Long productId, ProductStatus currentStatus, ProductStatus newStatus) {
        int updated = productRepository.updateStatusIfMatch(productId, currentStatus, newStatus);
        if (updated == 0) {
            throw new IllegalStateException("상태 변경 실패 (동시성 충돌)");
        }
    }

    /* =====================================================
       6️⃣ 상품 목록 조회 (수정된 버전)
       ===================================================== */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getList(
            Long sellerId, Integer categoryId, String keyword,
            ProductStatus status, Pageable pageable) {

        Page<Product> products;

        // 1. 키워드가 있으면 (null이 아니고 비어있지 않으면) 무조건 검색 우선
        if (keyword != null && !keyword.trim().isEmpty()) {
            log.info("검색어로 조회 수행: {}", keyword);
            products = productRepository.findByTitleContainingAndStatus(keyword, status, pageable);
        }
        // 2. 판매자 아이디가 있으면
        else if (sellerId != null) {
            products = productRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
        }
        // 3. 카테고리가 있으면
        else if (categoryId != null) {
            products = productRepository.findByCategoryIdAndStatus(categoryId, status, pageable);
        }
        // 4. 그 외에는 상태별 전체 조회
        else if (status != null) {
            products = productRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
        else {
            products = productRepository.findAll(pageable);
        }

        return products.map(product -> {
            List<ProductImage> imgs =
                    productImageRepository.findByProduct_ProductIdOrderBySortOrderAsc(product.getProductId());
            return ProductResponse.fromWithImages(product, imgs);
        });
    }

    @Transactional(readOnly = true)
    public List<String> getSuggestions(String keyword) {
        if (keyword == null) {
            return List.of();
        }

        String normalized = keyword.trim();
        if (normalized.length() < 2) {
            return List.of();
        }

        return productRepository.findTitlesForSuggestion(
                normalized,
                ProductStatus.SALE,
                org.springframework.data.domain.PageRequest.of(0, 3)
        ).stream().distinct().limit(3).toList();
    }
    /* =====================================================
       7️⃣ 상품 상태 변경 (문자열 버전)
       ===================================================== */
    @Transactional
    public void updateProductStatus(Long productId, String currentStatus, String newStatus) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + productId));
        product.updateStatus(ProductStatus.valueOf(newStatus));
    }

    /* =====================================================
       8️⃣ 여러 ID로 상품 리스트 조회 후 DTO 변환 (Bulk)
       ===================================================== */
    public List<ProductSummaryDto> getProductSummaries(List<Long> productIds) {
        return productRepository.findAllById(productIds).stream()
                .map(p -> {
                    String thumb = p.getImages().stream()
                            .filter(ProductImage::isThumbnail)
                            .map(ProductImage::getImageUrl)
                            .findFirst()
                            .orElse("");
                    return ProductSummaryDto.builder()
                            .productId(p.getProductId())
                            .sellerId(p.getSellerId())
                            .title(p.getTitle())
                            .price(p.getPrice().longValue())
                            .thumbnailUrl(thumb)
                            .status(p.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /* =====================================================
       9️⃣ 가격 변경 + 찜한 사람들에게 알림 발행  ← 신규
       ===================================================== */
    /**
     * 판매자가 상품 가격을 수정할 때 호출한다.
     *
     * 동작 순서:
     * 1. 요청자가 해당 상품의 판매자인지 검증한다.
     * 2. 기존 가격을 메모리에 보관한다.
     * 3. 새 가격으로 엔티티를 수정한다.
     * 4. 이 상품을 찜한 전체 회원 ID 를 조회한다.
     * 5. 가격 변동 이벤트 메시지를 RabbitMQ 로 발행한다.
     *    → notification-service WishPriceNotificationConsumer 가 소비해
     *      찜한 모든 회원에게 WebSocket 알림을 전송한다.
     *
     * @param productId  수정 대상 상품 ID
     * @param requesterId 요청자 (게이트웨이가 주입한 X-Member-Id, 판매자여야 함)
     * @param newPrice    변경할 가격
     */
    @Transactional
    public void updatePrice(Long productId, Long requesterId, BigDecimal newPrice) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. productId=" + productId));

        // 판매자 본인 여부 검증
        if (!product.getSellerId().equals(requesterId)) {
            throw new IllegalStateException("본인의 상품만 가격을 수정할 수 있습니다.");
        }

        // 기존 가격 캡처 (알림 메시지에 담을 용도)
        BigDecimal oldPrice = product.getPrice();

        // 가격이 동일하면 불필요한 이벤트 방지
        if (oldPrice.compareTo(newPrice) == 0) {
            log.info("[updatePrice] 가격 동일, 변경 없음: productId={}, price={}", productId, oldPrice);
            return;
        }

        // 가격 변경
        product.updatePrice(newPrice);

        log.info("[updatePrice] 가격 변경: productId={}, {}→{}", productId, oldPrice, newPrice);

        // 찜한 전체 회원 조회
        List<Long> wishedMemberIds = wishRepository.findAllMemberIdsByProductId(productId);

        if (wishedMemberIds.isEmpty()) {
            log.debug("[updatePrice] 찜한 회원 없음, 알림 생략: productId={}", productId);
            return;
        }

        // 썸네일 조회
        String thumbnailUrl = productImageRepository
                .findByProduct_ProductIdOrderBySortOrderAsc(productId)
                .stream()
                .filter(ProductImage::isThumbnail)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse("");

        // 가격 변동 알림 이벤트 발행
        try {
            WishPriceMessage message = new WishPriceMessage(
                    product.getProductId(),
                    product.getTitle(),
                    thumbnailUrl,
                    oldPrice,
                    newPrice,
                    wishedMemberIds
            );
            wishNotificationPublisher.publishWishPrice(message);
        } catch (Exception e) {
            // 알림 발행 실패가 가격 변경 트랜잭션을 롤백해서는 안 된다.
            log.error("[updatePrice] 가격 변동 알림 발행 실패: productId={}, error={}",
                    productId, e.getMessage(), e);
        }
    }
}
