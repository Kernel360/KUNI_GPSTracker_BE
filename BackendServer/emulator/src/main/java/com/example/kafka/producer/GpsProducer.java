package com.example.kafka.producer;

import com.example.kafka.model.GpsMsg;
import com.example.kafka.model.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GpsProducer {

    ObjectMapper objectMapper = new ObjectMapper();

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(GpsMsg message) throws JsonProcessingException {

        //if(message.getId()==null) message.setId(UUID.randomUUID().toString());

        kafkaTemplate.send(
                Topic.MDN_TOPIC,
            String.valueOf(message.getMdn()),
            objectMapper.writeValueAsString(message)
        );
    }
}
