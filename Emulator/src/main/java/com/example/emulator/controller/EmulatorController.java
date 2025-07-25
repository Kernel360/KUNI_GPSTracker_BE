package com.example.emulator.controller;

import com.example.emulator.model.HtmlRequest;
import com.example.emulator.model.OnRequestPayload;
import com.example.emulator.model.RangeRequest;
import com.example.emulator.service.EmulatorService;
import com.example.emulator.service.TokenService;
import com.example.emulator.util.CarRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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
    private final TokenService tokenService;

    @GetMapping
    public String mainPage(Model model) {
        model.addAttribute("cars", carRegistry.all());
        return "emulator";
    }

    @PostMapping("/{number}/{state}")
    @ResponseBody
    public void changeState(@PathVariable("number") String number, @PathVariable("state") String state, @RequestBody HtmlRequest req) throws IOException {
        Resource res = new ClassPathResource("gps/"+number+".csv");
        Path csv = res.getFile().toPath();
        //2. 서버로 정보 전송
        if(state.equals("ON")) emulatorService.start(csv, number, req.getInterval());
        else emulatorService.stop(number);

    }

    @PostMapping("/all/{state}")
    @ResponseBody
    public void turnOnAll(@PathVariable String state, @RequestBody OnRequestPayload payload) {
        if(state.equals("ON")) {
            emulatorService.turnOnAll(payload.getInterval());
        }
        else emulatorService.turnOffAll();

    }

    @PostMapping("/range/{state}")
    @ResponseBody
    public void turnOnRange(@PathVariable String state, @RequestBody RangeRequest payload) {
        if(state.equals("ON")) {
            emulatorService.turnOnRange(payload.getStart(), payload.getEnd(), payload.getInterval());
        }
        else emulatorService.turnOffRange(payload.getStart(), payload.getEnd());

    }

}
