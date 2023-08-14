package com.v1.junopoker.controller;

import com.v1.junopoker.model.Table;
import com.v1.junopoker.request.CreatePlayerRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class TableWebSocketController {
    private SimpMessagingTemplate messagingTemplate;

    public TableWebSocketController (SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    @MessageMapping("/tableEvents")
    @SendTo("/topic/tableEvents")
    public CreatePlayerRequest addUser(@Payload CreatePlayerRequest request) {
        return request;
    }

    @MessageMapping("/playerEvents")
    public void hideSitButtons(@Payload CreatePlayerRequest request) {
        String username = request.getPlayer().getUsername();
        messagingTemplate.convertAndSend("/topic/playerEvents/" + username, request);
    }
}
