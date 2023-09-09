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
        SeatRequest publicRequest = new SeatRequest();
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
    public void onPreFlopAction(Player player, int seat, float currentBet, float potSize, float minBet) {
        System.out.println("entered onPreFlopAction()");

        //TODO: change name of PublicHoleCardsRequest to something more generic
        //TODO: maybe seatRequest
        SeatRequest publicRequest= new SeatRequest();
        publicRequest.setType(RequestType.PLAYER_ACTION);
        publicRequest.setSeat(seat);

        messagingTemplate.convertAndSend("/topic/tableEvents", publicRequest);

        //create a player request object and populate it with the passed fields
        PlayerActionRequest privateRequest = new PlayerActionRequest();
        privateRequest.setType(RequestType.PLAYER_ACTION);
        privateRequest.setPlayer(player);
        privateRequest.setSeat(seat);
        privateRequest.setCurrentBet(currentBet);
        privateRequest.setPotSize(potSize);
        privateRequest.setMinBet(minBet);
        //send websocket message to player whose turn is to act
        messagingTemplate.convertAndSend("/topic/playerEvents/" + player.getUsername(), privateRequest);
    }
    @MessageMapping("/playerActionEvent")
    public void returnPreFlopAction(@Payload PlayerActionResponse response) {
        EndPlayerActionRequest request = new EndPlayerActionRequest();
        request.setType(RequestType.END_PLAYER_ACTION);
        request.setSeat(response.getSeat());
        request.setBet(response.getBetAmount());
        request.setAction(response.getAction());
        request.setStackSize(response.getStackSize());
        request.setPotSize(response.getPotSize());

        messagingTemplate.convertAndSend("/topic/tableEvents", request);

        tableService.handlePlayerAction(response);
    }

    @MessageMapping("/foldEvent")
    public void foldEvent(@Payload FoldRequest request) {
        System.out.println("Entered fold event");
        SeatRequest seatRequest = new SeatRequest();
        seatRequest.setType(request.getType());
        seatRequest.setSeat(request.getSeat());
        messagingTemplate.convertAndSend("/topic/playerEvents/" + request.getUsername(), seatRequest);
        messagingTemplate.convertAndSend("/topic/tableEvents", seatRequest);
    }
}
