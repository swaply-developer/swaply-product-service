package com.ch.swaplyproduct.controller;

import com.ch.swaplyproduct.product.dto.AdminProductResponse;
import com.ch.swaplyproduct.product.dto.AdminProductStatusRequest;
import com.ch.swaplyproduct.service.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자 전용 상품 API.
 * gateway → X-Member-Role: ADMIN 이 주입된 요청만 허용.
 *
 * ┌─────────────────────────────────────────────────────┐
 * │ GET  /api/admin/products              전체 목록      │
 * │ PATCH /api/admin/products/{id}/status 상태 변경      │
 * └─────────────────────────────────────────────────────┘
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    // =====================================================
    // 전체 상품 목록 조회
    // GET /api/admin/products
    // =====================================================
    @GetMapping
    public ResponseEntity<List<AdminProductResponse>> getAllProducts() {
        log.info("[Admin] 전체 상품 목록 조회 요청");
        return ResponseEntity.ok(adminProductService.getAllProducts());
    }

    // =====================================================
    // 상품 상태 변경
    // PATCH /api/admin/products/{productId}/status
    // body: { "status": "숨김" }
    // =====================================================
    @PatchMapping("/{productId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long productId,
            @RequestBody @Valid AdminProductStatusRequest request
    ) {
        log.info("[Admin] 상품 상태 변경 요청: productId={}, status={}", productId, request.getStatus());
        adminProductService.updateStatus(productId, request.getStatus());
        return ResponseEntity.ok().build();
    }
}
