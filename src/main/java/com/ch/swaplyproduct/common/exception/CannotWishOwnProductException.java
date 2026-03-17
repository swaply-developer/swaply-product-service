package com.ch.swaplyproduct.common.exception;

public class CannotWishOwnProductException extends RuntimeException {
    public CannotWishOwnProductException() {
        super("자신의 상품은 찜할 수 없습니다.");
    }
}