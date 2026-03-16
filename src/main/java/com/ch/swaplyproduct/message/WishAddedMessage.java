package com.ch.swaplyproduct.message;

/**
 * product-service → RabbitMQ → notification-service
 *
 * 누군가 상품을 찜했을 때 발행하는 메시지.
 * notification-service 의 WishAddedNotificationMessage 와 필드가 완전히 일치해야 한다.
 *
 * Exchange : wish.added.notification.exchange (DirectExchange)
 * Queue    : wish.added.notification.queue
 * Key      : wish.added.notification
 */
public record WishAddedMessage(
        Long   productId,
        String productTitle,
        String productThumbnailUrl,
        Long   wishMemberId,       // 찜을 누른 사람
        String wishMemberNickname, // 찜을 누른 사람 닉네임 (알림 문구용)
        Long   sellerId            // 알림 수신자 (판매자)
) {}
