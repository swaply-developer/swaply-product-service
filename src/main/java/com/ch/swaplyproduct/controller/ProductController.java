// 파일: src/main/java/com/ch/swaplyproduct/controller/ProductController.java
package com.ch.swaplyproduct.controller;

import com.ch.swaplyproduct.product.dto.ProductCreateRequest;
import com.ch.swaplyproduct.product.dto.ProductResponse;
import com.ch.swaplyproduct.product.dto.ProductSummaryDto;
import com.ch.swaplyproduct.product.entity.ProductStatus;
import com.ch.swaplyproduct.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // =====================================================
    // 1️⃣ 상품 등록
    //   - X-Member-Id: 게이트웨이가 JWT에서 추출해서 주입
    //   - sellerId를 프론트에서 받지 않고 헤더값을 사용 (보안)
    // =====================================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @ModelAttribute @Valid ProductCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestHeader(value = "X-Member-Id", required = false) String memberIdHeader
    ) {
        // 헤더에서 온 memberId를 Long으로 변환
        Long memberIdFromHeader = null;
        if (memberIdHeader != null && !memberIdHeader.isBlank()) {
            try {
                memberIdFromHeader = Long.parseLong(memberIdHeader);
            } catch (NumberFormatException e) {
                log.warn("X-Member-Id 헤더 파싱 실패: {}", memberIdHeader);
            }
        }

        log.info("상품 등록 요청 - memberId(헤더): {}, title: {}", memberIdFromHeader, request.getTitle());

        ProductResponse response = productService.createProduct(request, images, memberIdFromHeader);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 2️⃣ 상품 상세 조회
    //   - 더미 데이터 블록 완전 제거
    //   - 실제 DB + 이미지 반환
    // =====================================================
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long productId) {
        ProductResponse response = productService.getDetail(productId);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 3️⃣ 상품 목록 조회 (페이징 + 필터)
    // =====================================================
    @GetMapping
    public Page<ProductResponse> listProducts(
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProductStatus status,
            Pageable pageable
    ) {
        return productService.getList(sellerId, categoryId, keyword, status, pageable);
    }

    // =====================================================
    // 6️⃣ 상품 상태 변경
    // =====================================================
    // 프론트엔드 직접 호출용 PATCH 엔드포인트
    @PatchMapping("/{productId}/status")
    public ResponseEntity<Void> changeStatusPatch(
            @PathVariable Long productId,
            @RequestParam ProductStatus currentStatus,
            @RequestParam ProductStatus newStatus
    ) {
        log.info("[상태 변경 PATCH] 상품ID: {}, {} → {}", productId, currentStatus, newStatus);
        productService.changeStatus(productId, currentStatus, newStatus);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/{productId}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long productId,
            @RequestParam ProductStatus currentStatus,
            @RequestParam ProductStatus newStatus
    ) {
        // [로그 추가] 상태 변경 요청 정보 확인
        log.info("[상태 변경 요청] 상품ID: {}, 현재상태: {}, 변경예정: {}", productId, currentStatus, newStatus);

        productService.changeStatus(productId, currentStatus, newStatus);

        log.info("[상태 변경 완료] 상품ID: {} 상태가 {}로 변경되었습니다.", productId, newStatus);
        return ResponseEntity.ok().build();
    }
    // =====================================================
    // 헬퍼: 헤더 우선, 없으면 파라미터 fallback
    // =====================================================
    private Long parseMemberId(String header, Long fallback) {
        if (header != null && !header.isBlank()) {
            try { return Long.parseLong(header); } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }

    /**
     * [두 번째 요청] 여러 상품 정보 일괄 조회 (Bulk)
     * POST /api/products/summary/bulk
     */
    @PostMapping("/summary/bulk")
    public ResponseEntity<List<ProductSummaryDto>> getProductSummaries(
            @RequestBody List<Long> productIds
    ) {
        // [로그 추가] 벌크 조회 요청 시 받은 ID 리스트 확인
        log.info("[Bulk 조회 요청] 요청 상품 개수: {}, 상품ID 목록: {}",
                (productIds != null ? productIds.size() : 0), productIds);

        if (productIds == null || productIds.isEmpty()) {
            log.warn("[Bulk 조회] 요청된 상품 ID 목록이 비어있습니다.");
        }

        List<ProductSummaryDto> summaries = productService.getProductSummaries(productIds);

        // [로그 추가] 조회 결과 개수 확인 (데이터 매칭 실패 여부 파악용)
        log.info("[Bulk 조회 완료] 조회된 요약 정보 개수: {}", (summaries != null ? summaries.size() : 0));

        return ResponseEntity.ok(summaries);
    }
    /**-----------------------------------------------------------------------------------------------------
     * 상품 상태 변경 요청 종료
     -----------------------------------------------------------------------------------------------------*/


}
