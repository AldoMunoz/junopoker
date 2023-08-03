package com.v1.junopoker.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class TableWebSocketController {
    /*@MessageMapping("/poker-event")
    @SendTo("topic/poker-events")
    public PokerEvent handlePokerEvent(PokerEvent event) {
        return event;
    }*/
}
