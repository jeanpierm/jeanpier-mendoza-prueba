package com.jmendoza.customer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmendoza.customer.core.exception.NotFoundException;
import com.jmendoza.customer.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageListener {

    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.find-customer-request.name}")
    @SendTo
    public Message<String> listenFindCustomer(String customerId) throws JsonProcessingException {
        log.info("{}", customerId);
        var response = ApiResponse.ok();
        try {
            var customer = customerService.find(customerId);
            response.setData(customer);
        } catch (NotFoundException e) {
            log.error(e.getMessage());
            response.setHttpStatus(e.getHttpStatus());
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        }
        var str = objectMapper.writeValueAsString(response);
        return MessageBuilder.withPayload(str).build();
    }
}
