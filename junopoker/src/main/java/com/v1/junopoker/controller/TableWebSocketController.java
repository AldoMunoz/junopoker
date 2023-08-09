package com.v1.junopoker.controller;

import com.v1.junopoker.model.Table;
import com.v1.junopoker.service.TableService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class TableWebSocketController {
    @MessageMapping("/addUser")
    @SendTo("/topic/seatedPlayers")
    public CreatePlayerRequest addUser(@Payload CreatePlayerRequest request) {
        System.out.println(request.toString());
        return request;
    }
}
