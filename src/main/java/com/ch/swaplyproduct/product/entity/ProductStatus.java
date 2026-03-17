package com.ch.swaplyproduct.product.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {

    SALE("판매중"),
    RESERVED("예약중"),
    SOLD_OUT("판매완료"),
    HIDDEN("숨김"),       // 관리자 숨김 처리
    DELETED("삭제");     // 관리자 강제 삭제

    private final String description;

    /** 한글 description으로 enum 역조회 (관리자 API 요청 파싱용) */
    public static ProductStatus fromDescription(String description) {
        for (ProductStatus s : values()) {
            if (s.description.equals(description)) return s;
        }
        // 영문 name으로도 fallback 처리
        try {
            return ProductStatus.valueOf(description.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("알 수 없는 상품 상태: " + description);
        }
    }
}
