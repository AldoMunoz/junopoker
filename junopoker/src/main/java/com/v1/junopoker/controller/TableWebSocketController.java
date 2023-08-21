package com.v1.junopoker.controller;

import com.v1.junopoker.callback.TableCallback;
import com.v1.junopoker.model.Player;
import com.v1.junopoker.dto.BlindRequest;
import com.v1.junopoker.dto.PlayerRequest;
import com.v1.junopoker.dto.RequestType;
import com.v1.junopoker.service.TableService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class TableWebSocketController implements TableCallback {
    private SimpMessagingTemplate messagingTemplate;
    private TableService tableService;

    public TableWebSocketController (SimpMessagingTemplate messagingTemplate, TableService tableService) {
        this.messagingTemplate = messagingTemplate;
        this.tableService = tableService;
        tableService.setTableCallback(this);
    }

    @MessageMapping("/tableEvents")
    @SendTo("/topic/tableEvents")
    public PlayerRequest tableEvents(@Payload PlayerRequest request) {
        return request;
    }

    @MessageMapping("/playerEvents")
    public void playerEvents(@Payload PlayerRequest request) {
        String username = request.getPlayer().getUsername();
        messagingTemplate.convertAndSend("/topic/playerEvents/" + username, request);
    }

    @Override
    public void onBlindsSet(int smallBlindIndex, int bigBlindIndex, int buttonIndex) {
        System.out.println("entered callback on controller");
        BlindRequest request = new BlindRequest();
        request.setType(RequestType.MOVE_BLINDS);
        request.setSmallBlind(smallBlindIndex);
        request.setBigBlind(bigBlindIndex);
        request.setButton(buttonIndex);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    @Override
    public void onCardsDealt(Player[] players) {

    }

    @Override
    public void onPreFlopBetting(Player player) {

    }
}
