package net.musma.hhi.middleware.mewp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class DisplayController {

    @GetMapping("/")
    public String chatGET(){

        //log.info("@ChatController, chat GET()");

        return "display.html";
    }
}