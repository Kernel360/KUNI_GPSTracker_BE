package com.example.BackendServer.kafka.producer;

import com.example.BackendServer.kafka.model.GpsMsg;
import com.example.BackendServer.kafka.model.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class GpsProducer {

    ObjectMapper objectMapper = new ObjectMapper();

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(GpsMsg message) throws JsonProcessingException {

        //if(message.getId()==null) message.setId(UUID.randomUUID().toString());

        kafkaTemplate.send(
                Topic.MDN_TOPIC,
            String.valueOf(message.getMid()),
            objectMapper.writeValueAsString(message)
        );
    }
}
