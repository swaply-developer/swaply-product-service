package com.ch.swaplyproduct.controller;

import com.ch.swaplyproduct.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 서비스 내부 전용 API — 외부(Gateway)에 노출하지 않습니다.
 *
 * report-service 의 Feign Client(ProductInternalClient)가 호출합니다.
 * 신고 접수 시 상품 판매자가 신고자 본인인지 확인하기 위해 sellerId를 반환합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductRepository productRepository;

    /**
     * 상품의 판매자(sellerId) 반환.
     * 상품이 없으면 404를 반환하며, report-service 에서 신고를 그대로 허용합니다.
     */
    @GetMapping("/products/{productId}/seller-id")
    public ResponseEntity<Long> getSellerIdByProductId(@PathVariable Long productId) {
        return productRepository.findById(productId)
                .map(product -> ResponseEntity.ok(product.getSellerId()))
                .orElse(ResponseEntity.notFound().build());
    }
}
