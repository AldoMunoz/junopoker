package com.v1.junopoker.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class TableWebSocketController {
    @MessageMapping("/app/addUser")
    @SendTo("topic/seated-players")
    public CreatePlayerRequest handleNewSeatedPlayer(@Payload CreatePlayerRequest request) {
        System.out.println("Test");
        return request;
    }
}
