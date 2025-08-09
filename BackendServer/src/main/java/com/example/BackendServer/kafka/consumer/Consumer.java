package com.example.BackendServer.kafka.consumer;

import com.example.BackendServer.kafka.model.GpsMsg;
import com.example.BackendServer.kafka.model.OnOffMsg;
import com.example.BackendServer.kafka.service.KafkaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.BackendServer.kafka.model.Topic.MDN_TOPIC;

@Component
@RequiredArgsConstructor
public class Consumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaService kafkaService;

    @KafkaListener(
        topics = {MDN_TOPIC},
        groupId = "mdn-consumer-group",
        concurrency = "1"
    )


    public void listen(ConsumerRecord<String,String> rec, Acknowledgment ack) throws Exception {
        // 1) 우선 트리로 파싱해서 type만 확인
        ObjectNode root = (ObjectNode) objectMapper.readTree(rec.value());
        String type = root.path("type").asText("");

        try {
            switch (type) {
                case "GPS" -> {
                    GpsMsg gps = objectMapper.treeToValue(root, GpsMsg.class);
                    kafkaService.handleGps(gps);
                }
                case "ON" -> {
                    OnOffMsg on = objectMapper.treeToValue(root, OnOffMsg.class);
                    kafkaService.handleOn(on);
                }
                case "OFF" -> {
                    OnOffMsg off = objectMapper.treeToValue(root, OnOffMsg.class);
                    kafkaService.handleOff(off);
                }
                default -> {
                    // 알 수 없는 타입 — 로그만 남기고 스킵
                    System.out.println("Unknown type: " + type + " / payload=" + rec.value());
                }
            }
            ack.acknowledge();
        } catch (DataIntegrityViolationException dup) {
            ack.acknowledge(); // 중복은 성공 취급
        }
    }


}
