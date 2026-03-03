package com.ch.swaplyproduct.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 *  [ 목적 ]
 *       - 주문 생성 이벤트를 위한 Exchange( 우체국 )/ Queue / Binding 선언
 *       - 메시지 보낼때 json 변경할 수 있는 컨버터 처리
 * */
@Configuration
public class AmqpConfig {   //Advanced Massage queue p...

    // 주문 생성용 이벤트
    public static final String ORDER_CREATED_EXCHANGE="order.created.exchange";
    public static final String ORDER_CREATED_QUEUE="order.created.queue";
    public static final String ORDER_CREATED_ROUTING_KEY="order.created";

    @Bean
    public Declarables amqpDeclare(){
        DirectExchange exchange = new DirectExchange(ORDER_CREATED_EXCHANGE,true,false);
        Queue queue = QueueBuilder.durable(ORDER_CREATED_QUEUE).build();

        // 이 Exchange 로 들어오는 메시지 중 routing key 가 ORDER_CREATED_ROUTING_KEY 와 일치하면, 지정한 큐로 전달하라!!
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(ORDER_CREATED_ROUTING_KEY);



        return new Declarables(exchange,queue,binding);
    }

    /*
    * RabbitMQ에게 데이터 전송 시 자바 -> JSON
    * */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }



}
