package com.example.emulator.controller;

import com.example.emulator.service.EmulatorService;
import com.example.emulator.service.TokenService;
import com.example.emulator.util.CarRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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
    public void changeState(@PathVariable("number") String number, @PathVariable("state") String state) throws IOException {
        //1. 차량 on, off 상태 바꾸기
        emulatorService.changeState(number, state);

        Resource res = new ClassPathResource("gps/"+number+".csv");
        Path csv = res.getFile().toPath();
        //2. 서버로 정보 전송
        if(state.equals("ON")) emulatorService.start(csv, number);
        else emulatorService.stop(number);
        //TODO : 백그라운드에서 동작하기 때문에 모니터링 코드 추가 필요


    }

}
