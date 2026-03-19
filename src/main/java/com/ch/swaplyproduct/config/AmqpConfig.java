package com.ch.swaplyproduct.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * product-service AMQP 설정
 *
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │ [기존] order.created.exchange  → order.created.queue                    │
 * │ [기존] wish.added.notification.exchange → wish.added.notification.queue │
 * │ [기존] wish.price.notification.exchange → wish.price.notification.queue │
 * │                                                                          │
 * │ [신규] report.product.delete.exchange → report.product.delete.queue     │
 * │   Publisher: swaply-report-service                                       │
 * │   Consumer : 이 서비스(ReportProductDeleteConsumer)                       │
 * └──────────────────────────────────────────────────────────────────────────┘
 */
@Configuration
public class AmqpConfig {

    // ── 기존: 주문 생성 ──────────────────────────────────────────────────────
    public static final String ORDER_CREATED_EXCHANGE    = "order.created.exchange";
    public static final String ORDER_CREATED_QUEUE       = "order.created.queue";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

    // ── 기존: 찜 추가 알림 ────────────────────────────────────────────────────
    public static final String WISH_ADDED_EXCHANGE = "wish.added.notification.exchange";
    public static final String WISH_ADDED_QUEUE    = "wish.added.notification.queue";
    public static final String WISH_ADDED_KEY      = "wish.added.notification";

    // ── 기존: 가격 변동 알림 ──────────────────────────────────────────────────
    public static final String WISH_PRICE_EXCHANGE = "wish.price.notification.exchange";
    public static final String WISH_PRICE_QUEUE    = "wish.price.notification.queue";
    public static final String WISH_PRICE_KEY      = "wish.price.notification";

    // ── 신규: 신고 승인 상품 소프트 삭제 ─────────────────────────────────────
    public static final String REPORT_PRODUCT_DELETE_EXCHANGE = "report.product.delete.exchange";
    public static final String REPORT_PRODUCT_DELETE_QUEUE    = "report.product.delete.queue";
    public static final String REPORT_PRODUCT_DELETE_KEY      = "report.product.delete";

    @Bean
    public Declarables orderCreatedDeclare() {
        DirectExchange exchange = new DirectExchange(ORDER_CREATED_EXCHANGE, true, false);
        Queue queue = QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(ORDER_CREATED_ROUTING_KEY);
        return new Declarables(exchange, queue, binding);
    }

    @Bean
    public Declarables wishAddedDeclare() {
        DirectExchange exchange = new DirectExchange(WISH_ADDED_EXCHANGE, true, false);
        Queue queue = QueueBuilder.durable(WISH_ADDED_QUEUE).build();
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(WISH_ADDED_KEY);
        return new Declarables(exchange, queue, binding);
    }

    @Bean
    public Declarables wishPriceDeclare() {
        DirectExchange exchange = new DirectExchange(WISH_PRICE_EXCHANGE, true, false);
        Queue queue = QueueBuilder.durable(WISH_PRICE_QUEUE).build();
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(WISH_PRICE_KEY);
        return new Declarables(exchange, queue, binding);
    }

    @Bean
    public Declarables reportProductDeleteDeclare() {
        DirectExchange exchange = new DirectExchange(REPORT_PRODUCT_DELETE_EXCHANGE, true, false);
        Queue queue = QueueBuilder.durable(REPORT_PRODUCT_DELETE_QUEUE).build();
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(REPORT_PRODUCT_DELETE_KEY);
        return new Declarables(exchange, queue, binding);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }
}
