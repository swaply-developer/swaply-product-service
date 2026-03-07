package com.ch.swaplyproduct.product.repository;

import com.ch.swaplyproduct.product.entity.Product;
import com.ch.swaplyproduct.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /**
     * [WishService 연동용]
     * 특정 Product 엔티티와 대표 이미지 여부로 조회
     */
    Optional<ProductImage> findByProductAndIsThumbnailTrue(Product product);

    /**
     * 특정 상품 ID와 대표 이미지 여부로 조회 (ID만 있을 때 유용)
     */
    Optional<ProductImage> findByProduct_ProductIdAndIsThumbnailTrue(Long productId);

    /**
     * 특정 상품에 속한 모든 이미지 목록을 정렬 순서대로 조회
     */
    List<ProductImage> findByProduct_ProductIdOrderBySortOrderAsc(Long productId);

    /**
     * 상품 삭제 시 해당 상품의 모든 이미지 데이터를 삭제
     */
    void deleteByProduct_ProductId(Long productId);
}