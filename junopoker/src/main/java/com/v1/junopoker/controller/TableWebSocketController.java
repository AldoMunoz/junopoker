package com.v1.junopoker.controller;

import com.v1.junopoker.websocket.MessageType;
import com.v1.junopoker.websocket.TableMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class TableWebSocketController {
    @MessageMapping("/add-user")
    @SendTo("topic/seated-players")
    public CreatePlayerRequest handleNewSeatedPlayer(CreatePlayerRequest request) {
        return request;
    }
}
