package com.v1.junopoker.controller;

import com.v1.junopoker.request.CreatePlayerRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class TableWebSocketController {
    @MessageMapping("/addUser")
    @SendTo("/topic/seatedPlayers")
    public CreatePlayerRequest addUser(@Payload CreatePlayerRequest request) {
        return request;
    }
}
