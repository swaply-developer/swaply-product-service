package com.ch.swaplyproduct.controller;

import com.ch.swaplyproduct.product.dto.BrandResponse;
import com.ch.swaplyproduct.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 브랜드 API
 *
 * GET  /api/brands                     전체 브랜드 목록 (비로그인 가능, 로그인 시 followed 포함)
 * GET  /api/brands?categoryId={id}     카테고리별 브랜드 목록
 * GET  /api/brands/followed            내가 팔로우한 브랜드 목록 (로그인 필요)
 * POST /api/brands/{brandId}/follow    브랜드 팔로우
 * DELETE /api/brands/{brandId}/follow  브랜드 팔로우 취소
 */
@Slf4j
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    // ── 브랜드 목록 ──────────────────────────────────────────────────────────

    @GetMapping
    public List<BrandResponse> getBrands(
            @RequestParam(required = false) Integer categoryId,
            @RequestHeader(value = "X-Member-Id", required = false) String memberIdHeader
    ) {
        Long memberId = parseMemberId(memberIdHeader);

        if (categoryId != null) {
            return brandService.getByCategory(categoryId, memberId);
        }
        return brandService.getAll(memberId);
    }

    // ── 내가 팔로우한 브랜드 ────────────────────────────────────────────────

    @GetMapping("/followed")
    public ResponseEntity<List<BrandResponse>> getFollowedBrands(
            @RequestHeader(value = "X-Member-Id", required = false) String memberIdHeader
    ) {
        Long memberId = parseMemberId(memberIdHeader);
        if (memberId == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(brandService.getFollowed(memberId));
    }

    // ── 팔로우 ───────────────────────────────────────────────────────────────

    @PostMapping("/{brandId}/follow")
    public ResponseEntity<Void> follow(
            @PathVariable Integer brandId,
            @RequestHeader(value = "X-Member-Id", required = false) String memberIdHeader
    ) {
        Long memberId = parseMemberId(memberIdHeader);
        if (memberId == null) return ResponseEntity.status(401).build();

        brandService.follow(memberId, brandId);
        return ResponseEntity.ok().build();
    }

    // ── 팔로우 취소 ──────────────────────────────────────────────────────────

    @DeleteMapping("/{brandId}/follow")
    public ResponseEntity<Void> unfollow(
            @PathVariable Integer brandId,
            @RequestHeader(value = "X-Member-Id", required = false) String memberIdHeader
    ) {
        Long memberId = parseMemberId(memberIdHeader);
        if (memberId == null) return ResponseEntity.status(401).build();

        brandService.unfollow(memberId, brandId);
        return ResponseEntity.ok().build();
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private Long parseMemberId(String header) {
        if (header == null || header.isBlank()) return null;
        try { return Long.parseLong(header); }
        catch (NumberFormatException e) { return null; }
    }
}
