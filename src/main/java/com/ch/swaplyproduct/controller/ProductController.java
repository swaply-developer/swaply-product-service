// 파일: src/main/java/com/ch/swaplyproduct/controller/ProductController.java
package com.ch.swaplyproduct.controller;

import com.ch.swaplyproduct.product.dto.ProductCreateRequest;
import com.ch.swaplyproduct.product.dto.ProductResponse;
import com.ch.swaplyproduct.product.entity.ProductStatus;
import com.ch.swaplyproduct.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 1️⃣ 상품 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse createProduct(
            @ModelAttribute @Valid ProductCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        // 💡 로그를 찍어서 데이터가 들어오는지 확인하세요!
        log.info("수신 데이터: {}", request);
        return productService.create(request, images);
    }

    // 2️⃣ 상품 상세 조회
    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable Long productId) {
        return productService.getDetail(productId);
    }

    // 3️⃣ 상품 목록 조회 (페이징 + 필터)
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

    // 4️⃣ 좋아요 추가
    @PostMapping("/{productId}/wish")
    public void addWish(@PathVariable Long productId,
                        @RequestParam Long userId) {
        productService.addWish(productId, userId);
    }

    // 5️⃣ 좋아요 취소
    @DeleteMapping("/{productId}/wish")
    public void removeWish(@PathVariable Long productId,
                           @RequestParam Long userId) {
        productService.removeWish(productId, userId);
    }

    // 6️⃣ 상품 상태 변경
    @PatchMapping("/{productId}/status")
    public void changeStatus(@PathVariable Long productId,
                             @RequestParam ProductStatus currentStatus,
                             @RequestParam ProductStatus newStatus) {
        productService.changeStatus(productId, currentStatus, newStatus);
    }
}