package com.v1.junopoker.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class TableWebSocketHandler extends TextWebSocketHandler {
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        //Handle incoming WebSocket messages
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        //when a new WebSocket connection is established (user enters the page)
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        //WebSocket connection is closed (user leaves the page)
    }
}
