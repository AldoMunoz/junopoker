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

import java.util.concurrent.CompletableFuture;

@Controller
public class TableWebSocketController implements TableCallback {
    private SimpMessagingTemplate messagingTemplate;
    private TableService tableService;

    public TableWebSocketController (SimpMessagingTemplate messagingTemplate, TableService tableService) {
        this.messagingTemplate = messagingTemplate;
        this.tableService = tableService;
        //revert this line to not use this if code doesn't work
        this.tableService.setTableCallback(this);
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
    //send a request to the front end for the player to input an action (check, bet, or fold)
    //receive that action and send it to TableService.java using a CompletableFuture
    public void onPreFlopAction(Player player, int seat, float currentBet) {
        System.out.println("entered onPreFlopAction()");
        //create a player request object and populate it with the passed fields
        PlayerActionRequest request = new PlayerActionRequest();
        request.setType(RequestType.PLAYER_ACTION);
        request.setPlayer(player);
        request.setSeat(seat);
        request.setCurrentBet(currentBet);

        //send websocket message to player whose turn is to act
        messagingTemplate.convertAndSend("/topic/playerEvents/" + player.getUsername(), request);
    }
    @MessageMapping("/playerActionEvent")
    public void returnPreFlopAction(@Payload PlayerActionResponse response) {
        tableService.handlePlayerAction(response);
    }
}
