package com.v1.junopoker.controller;

import com.v1.junopoker.callback.TableCallback;
import com.v1.junopoker.dto.*;
import com.v1.junopoker.model.Card;
import com.v1.junopoker.model.Player;
import com.v1.junopoker.model.Table;
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

    @MessageMapping("/startGame")
    public void startGame(@Payload Table table) {
        tableService.runGame(table);
    }

    @Override
    public void onButtonSet(int buttonIndex) {
        MoveButtonRequest request = new MoveButtonRequest();
        request.setType(RequestType.MOVE_BUTTON);
        request.setButton(buttonIndex);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }
    @Override
    public void onPotInit(int sbIndex, int bbIndex, double sbAmount, double bbAmount, double potSize) {
        InitPotRequest request = new InitPotRequest();
        request.setType(RequestType.INIT_POT);
        request.setSmallBlind(sbIndex);
        request.setBigBlind(bbIndex);
        request.setSbAmount(sbAmount);
        request.setBbAmount(bbAmount);
        request.setPotSize(potSize);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }
    @Override
    public void onHoleCardsDealt(String username, int seat, Card[] holeCards) {
        PublicHoleCardsRequest publicRequest = new PublicHoleCardsRequest();
        publicRequest.setType(RequestType.DEAL_PRE);
        publicRequest.setSeat(seat);

        PrivateHoleCardsRequest privateRequest = new PrivateHoleCardsRequest();
        privateRequest.setType(RequestType.DEAL_PRE);
        privateRequest.setSeat(seat);
        privateRequest.setCards(holeCards);

        messagingTemplate.convertAndSend("/topic/tableEvents", publicRequest);
        messagingTemplate.convertAndSend("/topic/playerEvents/" + username, privateRequest);
    }

    @Override
    public void onPreFlopAction(Player player) {

    }
}
