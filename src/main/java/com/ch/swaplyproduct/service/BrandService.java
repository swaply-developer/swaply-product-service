package com.ch.swaplyproduct.service;

import com.ch.swaplyproduct.product.dto.BrandCreateRequest;
import com.ch.swaplyproduct.product.dto.BrandResponse;
import com.ch.swaplyproduct.product.entity.Brand;
import com.ch.swaplyproduct.product.entity.BrandFollow;
import com.ch.swaplyproduct.product.entity.Category;
import com.ch.swaplyproduct.product.repository.BrandFollowRepository;
import com.ch.swaplyproduct.product.repository.BrandRepository;
import com.ch.swaplyproduct.product.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository       brandRepository;
    private final CategoryRepository    categoryRepository;
    private final BrandFollowRepository brandFollowRepository;

    // =====================================================
    // 브랜드 생성
    // =====================================================
    @Transactional
    public BrandResponse create(BrandCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));

        Brand brand = Brand.create(request.getName(), request.getNameEn(),
                request.getLogoUrl(), category);
        Brand saved = brandRepository.save(brand);
        return BrandResponse.from(saved);
    }

    // =====================================================
    // 전체 브랜드 목록 (memberId 선택적 — 로그인 시 followed 포함)
    // =====================================================
    public List<BrandResponse> getAll(Long memberId) {
        List<Brand> brands = brandRepository.findAll();
        return toResponseList(brands, memberId);
    }

    // =====================================================
    // 카테고리별 브랜드 목록
    // =====================================================
    public List<BrandResponse> getByCategory(Integer categoryId, Long memberId) {
        List<Brand> brands = brandRepository.findByCategoryCategoryId(categoryId);
        return toResponseList(brands, memberId);
    }

    // =====================================================
    // 팔로우한 브랜드 목록
    // =====================================================
    public List<BrandResponse> getFollowed(Long memberId) {
        List<Integer> brandIds = brandFollowRepository.findBrandIdsByMemberId(memberId);
        if (brandIds.isEmpty()) return List.of();

        List<Brand> brands = brandRepository.findAllById(brandIds);
        return toResponseList(brands, memberId);
    }

    // =====================================================
    // 브랜드 팔로우
    // =====================================================
    @Transactional
    public void follow(Long memberId, Integer brandId) {
        if (brandFollowRepository.existsByMemberIdAndBrandId(memberId, brandId)) {
            log.info("[BrandFollow] 이미 팔로우: memberId={}, brandId={}", memberId, brandId);
            return;
        }
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("브랜드가 존재하지 않습니다."));

        brandFollowRepository.save(BrandFollow.create(memberId, brandId));
        log.info("[BrandFollow] 팔로우: memberId={}, brandId={}", memberId, brandId);
    }

    // =====================================================
    // 브랜드 팔로우 취소
    // =====================================================
    @Transactional
    public void unfollow(Long memberId, Integer brandId) {
        brandFollowRepository.deleteByMemberIdAndBrandId(memberId, brandId);
        log.info("[BrandFollow] 언팔로우: memberId={}, brandId={}", memberId, brandId);
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────────────────

    private List<BrandResponse> toResponseList(List<Brand> brands, Long memberId) {
        // 현재 유저가 팔로우한 brandId 집합 (비로그인이면 빈 Set)
        Set<Integer> followedIds = memberId == null
                ? Set.of()
                : Set.copyOf(brandFollowRepository.findBrandIdsByMemberId(memberId));

        return brands.stream()
                .map(brand -> {
                    long followCount = brandFollowRepository.countByBrandId(brand.getBrandId());
                    boolean followed = followedIds.contains(brand.getBrandId());
                    return BrandResponse.from(brand, followCount, followed);
                })
                .collect(Collectors.toList());
    }
}
