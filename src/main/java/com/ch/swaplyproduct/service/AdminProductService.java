package com.ch.swaplyproduct.service;

import com.ch.swaplyproduct.client.MemberInternalClient;
import com.ch.swaplyproduct.product.dto.AdminProductResponse;
import com.ch.swaplyproduct.product.dto.MemberSummaryDto;
import com.ch.swaplyproduct.product.entity.Product;
import com.ch.swaplyproduct.product.entity.ProductImage;
import com.ch.swaplyproduct.product.entity.ProductStatus;
import com.ch.swaplyproduct.product.repository.ProductImageRepository;
import com.ch.swaplyproduct.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final MemberInternalClient memberInternalClient;

    // =====================================================
    // 전체 상품 목록 조회 (관리자)
    // =====================================================
    @Transactional(readOnly = true)
    public List<AdminProductResponse> getAllProducts() {

        // 1. 전체 상품 조회 (최신순)
        List<Product> products = productRepository.findAllByOrderByCreatedAtDesc();

        if (products.isEmpty()) return List.of();

        // 2. 판매자 ID 수집 → member-service bulk 조회 (N+1 방지)
        List<Long> sellerIds = products.stream()
                .map(Product::getSellerId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, MemberSummaryDto> memberMap = memberInternalClient.getMemberSummaries(sellerIds);

        // 3. 이미지 조회 후 응답 DTO 조립
        return products.stream()
                .map(product -> {
                    List<ProductImage> images = productImageRepository
                            .findByProduct_ProductIdOrderBySortOrderAsc(product.getProductId());

                    MemberSummaryDto seller = memberMap.get(product.getSellerId());
                    String nickname = seller != null ? seller.getNickname() : "-";

                    return AdminProductResponse.from(product, images, nickname);
                })
                .collect(Collectors.toList());
    }

    // =====================================================
    // 상품 상태 강제 변경 (관리자 — currentStatus 체크 없음)
    // =====================================================
    @Transactional
    public void updateStatus(Long productId, String statusDescription) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. productId=" + productId));

        // 한글 description → enum 변환 (예: "숨김" → HIDDEN)
        ProductStatus newStatus = ProductStatus.fromDescription(statusDescription);

        product.updateStatus(newStatus);

        log.info("[Admin] 상품 상태 변경: productId={}, {} → {}",
                productId, product.getStatus().getDescription(), newStatus.getDescription());
    }
}
