package com.ch.swaplyproduct.controller;

import com.ch.swaplyproduct.common.exception.CannotWishOwnProductException;
import com.ch.swaplyproduct.product.dto.WishResponse;
import com.ch.swaplyproduct.service.WishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WishController {

    private final WishService wishService;

    // =====================================================
    // GET /api/wishes  — 내 찜 목록 조회
    //   X-Member-Id 헤더: 게이트웨이가 JWT에서 주입
    //   비로그인: 빈 목록 반환 (게이트웨이에서 이미 통과시킴)
    // =====================================================
    @GetMapping("/api/wishes")
    public ResponseEntity<Page<WishResponse>> getWishList(
            @RequestHeader(value = "X-Member-Id", required = false) String memberIdHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long memberId = parseMemberId(memberIdHeader);

        // ✅ 비로그인 시 빈 목록 반환 (401 대신) — 프론트에서 비로그인도 홈 접근 가능
        if (memberId == null) {
            Page<WishResponse> empty = new PageImpl<>(
                    Collections.emptyList(),
                    PageRequest.of(page, size),
                    0
            );
            return ResponseEntity.ok(empty);
        }

        Page<WishResponse> result = wishService.getWishList(
                memberId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // POST /api/products/{productId}/wish  — 찜 추가
    // =====================================================
    @PostMapping("/api/products/{productId}/wish")
    public ResponseEntity<?> addWish(
            @PathVariable Long productId,
            @RequestHeader(value = "X-Member-Id", required = false) String memberIdHeader
        ) {
            Long memberId = parseMemberId(memberIdHeader);
            if (memberId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("message", "로그인이 필요한 서비스 입니다."));
            }

            wishService.addWish(productId, memberId);
            return ResponseEntity.ok().build();
        }

    // =====================================================
    // DELETE /api/products/{productId}/wish  — 찜 취소
    // =====================================================
    @DeleteMapping("/api/products/{productId}/wish")
    public ResponseEntity<Void> removeWish(
            @PathVariable Long productId,
            @RequestHeader(value = "X-Member-Id", required = false) String memberIdHeader
    ) {
        Long memberId = parseMemberId(memberIdHeader);
        if (memberId == null) return ResponseEntity.status(401).build();

        wishService.removeWish(productId, memberId);
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET /api/products/{productId}/wish/check  — 찜 여부 확인
    // =====================================================
    @GetMapping("/api/products/{productId}/wish/check")
    public ResponseEntity<Map<String, Boolean>> checkWish(
            @PathVariable Long productId,
            @RequestHeader(value = "X-Member-Id", required = false) String memberIdHeader
    ) {
        Long memberId = parseMemberId(memberIdHeader);
        boolean wished = (memberId != null) && wishService.isWished(productId, memberId);
        return ResponseEntity.ok(Map.of("wished", wished));
    }

    // =====================================================
    // 헬퍼
    // =====================================================
    private Long parseMemberId(String header) {
        if (header == null || header.isBlank()) return null;
        try { return Long.parseLong(header); }
        catch (NumberFormatException e) { return null; }
    }
}
