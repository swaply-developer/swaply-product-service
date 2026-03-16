package com.ch.swaplyproduct.controller;

import com.ch.swaplyproduct.product.dto.ProductCreateRequest;
import com.ch.swaplyproduct.product.dto.ProductResponse;
import com.ch.swaplyproduct.product.dto.ProductSummaryDto;
import com.ch.swaplyproduct.product.dto.ProductUpdatePriceRequest;
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
    // =====================================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @ModelAttribute @Valid ProductCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestHeader(value = "X-Member-Id", required = false) String memberIdHeader
    ) {
        Long memberIdFromHeader = parseMemberId(memberIdHeader, null);
        log.info("상품 등록 요청 - memberId(헤더): {}, title: {}", memberIdFromHeader, request.getTitle());
        ProductResponse response = productService.createProduct(request, images, memberIdFromHeader);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 2️⃣ 상품 상세 조회
    // =====================================================
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getDetail(productId));
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
    // 4️⃣ 상품 상태 변경 (프론트엔드 직접 호출용)
    //   PATCH /api/products/{productId}/status
    //   api.js: productAPI.changeStatus() 가 이 엔드포인트를 호출
    // =====================================================
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

    // =====================================================
    // 5️⃣ 상품 상태 변경 (내부 서비스 간 통신용)
    //   payment-service TradeExternalClient 가 호출
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
        log.info("[상태 변경 요청] 상품ID: {}, 현재상태: {}, 변경예정: {}", productId, currentStatus, newStatus);
        productService.changeStatus(productId, currentStatus, newStatus);
        log.info("[상태 변경 완료] 상품ID: {} → {}", productId, newStatus);
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // 5️⃣ 상품 일괄 조회 (Bulk, 내부 통신용)
    // =====================================================
    @PostMapping("/summary/bulk")
    public ResponseEntity<List<ProductSummaryDto>> getProductSummaries(
            @RequestBody List<Long> productIds
    ) {
        log.info("[Bulk 조회 요청] 상품 개수: {}", productIds != null ? productIds.size() : 0);
        List<ProductSummaryDto> summaries = productService.getProductSummaries(productIds);
        log.info("[Bulk 조회 완료] 조회된 상품 수: {}", summaries != null ? summaries.size() : 0);
        return ResponseEntity.ok(summaries);
    }

    // =====================================================
    // 6️⃣ 가격 변경  ← 신규
    //   - PATCH /api/products/{productId}/price
    //   - X-Member-Id: 게이트웨이가 JWT에서 주입 (판매자 본인 검증)
    //   - 변경 성공 시 찜한 사람 전원에게 RabbitMQ 알림 이벤트 발행
    // =====================================================
    @PatchMapping("/{productId}/price")
    public ResponseEntity<Void> updatePrice(
            @PathVariable Long productId,
            @RequestBody @Valid ProductUpdatePriceRequest request,
            @RequestHeader(value = "X-Member-Id", required = false) String memberIdHeader
    ) {
        Long memberId = parseMemberId(memberIdHeader, null);
        if (memberId == null) {
            return ResponseEntity.status(401).build();
        }

        log.info("[가격 변경 요청] productId={}, memberId={}, newPrice={}",
                productId, memberId, request.getPrice());

        productService.updatePrice(productId, memberId, request.getPrice());
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // 헬퍼
    // =====================================================
    private Long parseMemberId(String header, Long fallback) {
        if (header != null && !header.isBlank()) {
            try { return Long.parseLong(header); }
            catch (NumberFormatException ignored) {}
        }
        return fallback;
    }
}
