package com.ch.swaplyproduct.message;

import lombok.Getter;
import lombok.NoArgsConstructor;

// RabbitMQ에게 보낼 메시지( 반드시 requestId 추가)
@Getter
@NoArgsConstructor
public class OrderCreatedMessage {
    private String requestId;
    private Long orderId;
    private Long productId;
    private String productName;

    public OrderCreatedMessage(String requestId, Long orderId, Long productId, String productName) {
        this.requestId = requestId;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
    }
}
