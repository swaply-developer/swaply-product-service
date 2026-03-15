package com.ch.swaplyproduct.message;

import java.math.BigDecimal;
import java.util.List;

/**
 * product-service → RabbitMQ → notification-service
 *
 * 상품 가격이 변경됐을 때 발행하는 메시지.
 * notification-service 의 WishPriceNotificationMessage 와 필드가 완전히 일치해야 한다.
 *
 * Exchange : wish.price.notification.exchange (DirectExchange)
 * Queue    : wish.price.notification.queue
 * Key      : wish.price.notification
 */
public record WishPriceMessage(
        Long         productId,
        String       productTitle,
        String       productThumbnailUrl,
        BigDecimal   oldPrice,
        BigDecimal   newPrice,
        List<Long>   wishedMemberIds   // 이 상품을 찜한 전체 회원 ID
) {}
