package com.example.kafka.converter;

import com.example.emulator.model.GpsCycleData;
import com.example.emulator.model.GpsCycleRequest;
import com.example.emulator.model.OnOffRequest;
import com.example.kafka.model.GpsData;
import com.example.kafka.model.GpsMsg;
import com.example.kafka.model.OnOffMsg;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MsgConverter {

    public OnOffMsg OnOffRequestToOnOffMsg(OnOffRequest req, String type) {
        return OnOffMsg.builder()
                .id(UUID.randomUUID().toString())
                .type(type)
                .mdn(req.getMdn())
                .tid(req.getTid())
                .mid(req.getMid())
                .pv(req.getPv())
                .did(req.getDid())
                .onTime(req.getOnTime())
                .offTime(req.getOffTime())
                .gcd(req.getGcd())
                .lat(req.getLat())
                .lon(req.getLon())
                .ang(req.getAng())
                .spd(req.getSpd())
                .sum(req.getSum())
                .build();
    }

    public GpsMsg GpsCycleRequestToGpsMsg(GpsCycleRequest req, String type) {

        List<GpsCycleData> raw = Optional.ofNullable(req.getCList()).orElse(Collections.emptyList());

        // List<GpsCycleData> -> List<GpsData>
        List<GpsData> items = raw.stream()
                .map(d -> GpsData.builder()
                        .sec(d.getSec())
                        .gcd(d.getGcd())
                        .lat(d.getLat())
                        .lon(d.getLon())
                        .ang(d.getAng())
                        .spd(d.getSpd())
                        .sum(d.getSum())
                        .bat(d.getBat())
                        .build())
                .toList();

        return GpsMsg.builder()
                .id(UUID.randomUUID().toString())
                .type(type)
                .mdn(req.getMdn())
                .tid(req.getTid())
                .mid(req.getMid())
                .pv(req.getPv())
                .did(req.getDid())
                .oTime(req.getOTime())
                .cCnt(req.getCCnt())
                .cList(items)
                .build();
    }
}
