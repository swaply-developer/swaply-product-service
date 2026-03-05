package com.ch.swaplyproduct.service;

import com.ch.swaplyproduct.product.dto.BrandCreateRequest;
import com.ch.swaplyproduct.product.dto.BrandResponse;
import com.ch.swaplyproduct.product.entity.Brand;
import com.ch.swaplyproduct.product.entity.Category;
import com.ch.swaplyproduct.product.repository.BrandRepository;
import com.ch.swaplyproduct.product.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public BrandResponse create(BrandCreateRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));

        Brand brand = Brand.create(
                request.getName(),
                request.getNameEn(),
                request.getLogoUrl(),
                category
        );

        Brand saved = brandRepository.save(brand);

        return BrandResponse.from(saved);
    }
}