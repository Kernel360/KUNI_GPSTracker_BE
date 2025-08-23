package com.example.emulator.controller;

import com.example.emulator.model.HtmlRequest;
import com.example.emulator.model.OnRequestPayload;
import com.example.emulator.model.RangeRequest;
import com.example.emulator.service.EmulatorService;
import com.example.emulator.service.TokenService;
import com.example.emulator.util.CarRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;

@Controller
@RequiredArgsConstructor
@RequestMapping("/emulator")
public class EmulatorController {

    private final CarRegistry carRegistry;
    private final EmulatorService emulatorService;
    private final ResourceLoader resourceLoader;

    @Value("${emulator.gps.location:classpath:gps}")
    private String gpsLocation;

    @GetMapping
    public String mainPage(Model model) {
        model.addAttribute("cars", carRegistry.all());
        return "emulator";
    }

    @PostMapping("/{number}/{state}")
    @ResponseBody
    public void changeState(@PathVariable("number") String number, @PathVariable("state") String state, @RequestBody HtmlRequest req) throws IOException {
        Resource res = resourceLoader.getResource(gpsLocation + "/" + number + ".csv");
        //2. 서버로 정보 전송
        if(state.equals("ON")) emulatorService.start(res, number, req.getInterval());
        else emulatorService.stop(number);
    }
}
