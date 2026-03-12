package com.ch.swaplyproduct.controller;

import com.ch.swaplyproduct.product.dto.BrandResponse;
import com.ch.swaplyproduct.product.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandRepository brandRepository;

    /**
     * GET /api/brands              → 전체 브랜드 목록
     * GET /api/brands?categoryId=1 → 특정 카테고리 브랜드 목록
     */
    @GetMapping
    public List<BrandResponse> getBrands(
            @RequestParam(required = false) Integer categoryId
    ) {
        if (categoryId != null) {
            return brandRepository.findByCategoryCategoryId(categoryId)
                    .stream()
                    .map(BrandResponse::from)
                    .collect(Collectors.toList());
        }
        return brandRepository.findAll()
                .stream()
                .map(BrandResponse::from)
                .collect(Collectors.toList());
    }
}
