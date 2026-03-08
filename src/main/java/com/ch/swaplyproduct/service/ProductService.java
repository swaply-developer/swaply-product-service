// 파일: src/main/java/com/ch/swaplyproduct/service/ProductService.java
package com.ch.swaplyproduct.service;

import com.ch.swaplyproduct.product.dto.ProductCreateRequest;
import com.ch.swaplyproduct.product.dto.ProductResponse;
import com.ch.swaplyproduct.product.entity.*;
import com.ch.swaplyproduct.product.repository.BrandRepository;
import com.ch.swaplyproduct.product.repository.CategoryRepository;
import com.ch.swaplyproduct.product.repository.ProductImageRepository;
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
    private final ProductImageRepository productImageRepository;
    private final BrandRepository brandRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /* =====================================================
       1️⃣ 상품 등록 (카테고리/브랜드 연동)
       ===================================================== */
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, List<MultipartFile> images, Long memberId) {
        // 1. 카테고리 및 브랜드 조회
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        Brand brand = (request.getBrandId() != null)
                ? brandRepository.findById(request.getBrandId()).orElse(null)
                : null;

        // 2. 상품 저장
        // 중요: request.getSellerId() 대신 컨트롤러에서 넘겨받은 검증된 memberId를 사용합니다.
        Product product = Product.create(
                memberId,
                category,
                brand,
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getTradeType()
        );
        Product savedProduct = productRepository.save(product);

        // 3. 이미지 저장 및 로그 출력
        if (images != null && !images.isEmpty()) {
            // 상대 경로 설정 (프로젝트 루트의 uploads 폴더)
            String uploadDir = "uploads/";
            java.io.File dir = new java.io.File(uploadDir);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            // [로그] 실제 서버의 어디에 저장되는지 확인
            System.out.println("==== [이미지 저장 시작] ====");
            System.out.println("절대 경로: " + dir.getAbsolutePath());

            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                // 파일명 중복 방지를 위해 상품ID와 인덱스 조합
                String filename = savedProduct.getProductId() + "_" + i + ".jpg";
                java.io.File dest = new java.io.File(dir.getAbsolutePath() + java.io.File.separator + filename);

                try {
                    file.transferTo(dest);
                    // [로그] 파일 저장 성공 여부 확인
                    System.out.println("파일 저장 성공: " + dest.getAbsolutePath());

                    // 이미지 엔티티 생성 및 저장
                    ProductImage pi = ProductImage.create(
                            savedProduct,
                            "/uploads/" + filename,
                            (i == 0),
                            i
                    );
                    productImageRepository.save(pi);
                } catch (Exception e) {
                    System.err.println("파일 저장 실패: " + e.getMessage());
                    e.printStackTrace();
                    // 이미지 저장 실패 시 롤백을 위해 런타임 예외 발생
                    throw new RuntimeException("이미지 저장 중 오류 발생", e);
                }
            }
            System.out.println("==== [이미지 저장 종료] ====");
        }

        // 4. 저장된 정보를 기반으로 응답 DTO 반환
        return ProductResponse.from(savedProduct);
    }


    /* =====================================================
       2️⃣ 상품 상세 조회 (수정본)
       ===================================================== */
    @Transactional(readOnly = true)
    public ProductResponse getDetail(Long productId) {

        // 1. Redis 조회수 증가
        redisTemplate.opsForValue().increment("product:view:" + productId);

        // 2. 상품 기본 정보 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        // 3. ✅ 해당 상품의 이미지 목록을 별도로 조회 (중요!)
        List<ProductImage> imgs =
                productImageRepository.findByProduct_ProductIdOrderBySortOrderAsc(productId);

        // 4. ✅ 이미지 정보를 포함하여 응답 DTO 생성
        return ProductResponse.fromWithImages(product, imgs);
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
        // ✅ 목록 조회 시 썸네일 URL 포함 (N+1 없이 product 단위로 조회)
        return products.map(product -> {
            List<ProductImage> imgs =
                    productImageRepository.findByProduct_ProductIdOrderBySortOrderAsc(product.getProductId()); // 메서드명 수정 확인 필요


            String thumbnail = imgs.stream()
                    .filter(com.ch.swaplyproduct.product.entity.ProductImage::isThumbnail)
                    .map(com.ch.swaplyproduct.product.entity.ProductImage::getImageUrl)
                    .findFirst()
                    .orElse(imgs.isEmpty() ? null : imgs.get(0).getImageUrl());

            return ProductResponse.fromWithImages(product, imgs);
        });
    }
}