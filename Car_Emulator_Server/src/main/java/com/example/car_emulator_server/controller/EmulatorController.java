package com.example.car_emulator_server.controller;

import com.example.car_emulator_server.service.EmulatorService;
import com.example.car_emulator_server.service.TokenService;
import org.springframework.ui.Model;
import com.example.car_emulator_server.util.CarRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    public void changeState(@PathVariable("number") String number, @PathVariable("state") String state) {
        //1. 차량 on, off 상태 바꾸기
        emulatorService.changeState(number, state);

        //2. 서버로 정보 전송
        if(state.equals("ON")) {
            //2-1. 인증 토큰 받아오기
            tokenService.getTokenFromServer(number);

            //2-2. On 정보 서버로 전송
            emulatorService.sendOn(number);
        }
        else{
            //2-3. Off 정보 서버로 전송
            emulatorService.sendOff(number);
        }
    }

}
