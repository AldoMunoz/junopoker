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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
public class TableWebSocketController implements TableCallback {
    private SimpMessagingTemplate messagingTemplate;
    private TableService tableService;

    public TableWebSocketController (SimpMessagingTemplate messagingTemplate, TableService tableService) {
        this.messagingTemplate = messagingTemplate;
        this.tableService = tableService;
        this.tableService.setTableCallback(this);
    }

    @MessageMapping("/tableEvents")
    @SendTo("/topic/tableEvents")
    public PlayerRequest tableEvents(@Payload PlayerRequest request) {
        return request;
    }

    @MessageMapping("/playerEvents")
    public void playerEvents(@Payload PlayerRequest request) {
        Player[] seats = tableService.getSeats(request.getTableID());

        SeatsAndSeatResponse response = new SeatsAndSeatResponse();
        response.setType(RequestType.SIT);
        response.setSeats(seats);
        response.setSeatIndex(request.getSeatIndex());

        String username = request.getPlayer().getUsername();
        messagingTemplate.convertAndSend("/topic/playerEvents/" + username, response);
    }

    @MessageMapping("/getSeats")
    public void getSeats(@Payload String tableID) {
        Player[] seats = tableService.getSeats(tableID);

        SeatsResponse response = new SeatsResponse();
        response.setType(RequestType.SEATS);
        response.setSeats(seats);
        response.setTableID(tableID);

        messagingTemplate.convertAndSend("/topic/tableEvents", response);
    }


    @MessageMapping("/countPlayers")
    public void countPlayers(@Payload String tableID) {
        tableService.countPlayers(tableID);
    }
    /*
    @MessageMapping("/startGame")
    public void startGame(@Payload Table table) {
        System.out.println("startGame, TableWebSocketController: " + table.getTABLE_ID());
        tableService.runGame(table);
    }
     */
    @MessageMapping("/rebuy")
    public void rebuy(@Payload RebuyResponse request) {
        tableService.rebuyPlayer(request.getRebuyAmount(), request.getSeatIndex(), request.getTableId());
    }

    public void onButtonSet(int buttonIndex) {
        MoveButtonRequest request = new MoveButtonRequest();
        request.setType(RequestType.MOVE_BUTTON);
        request.setButton(buttonIndex);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    public void onPotInit(int sbIndex, int bbIndex, BigDecimal sbAmount, BigDecimal bbAmount, BigDecimal potSize) {
        InitPotRequest request = new InitPotRequest();
        request.setType(RequestType.INIT_POT);
        request.setSmallBlind(sbIndex);
        request.setBigBlind(bbIndex);
        request.setSbAmount(sbAmount);
        request.setBbAmount(bbAmount);
        request.setPotSize(potSize);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    public void onHoleCardsDealt(String username, int seat, Card[] holeCards) {
        SeatRequest publicRequest = new SeatRequest();
        publicRequest.setType(RequestType.DEAL_PRE);
        publicRequest.setSeatIndex(seat);

        PrivateHoleCardsRequest privateRequest = new PrivateHoleCardsRequest();
        privateRequest.setType(RequestType.DEAL_PRE);
        privateRequest.setSeat(seat);
        privateRequest.setCards(holeCards);

        messagingTemplate.convertAndSend("/topic/tableEvents", publicRequest);
        // Add a slight delay between messages
        try {
            Thread.sleep(100); // Sleep for 100 milliseconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        messagingTemplate.convertAndSend("/topic/playerEvents/" + username, privateRequest);
    }

    //send a request to the front end for the player to input an action (check, bet, or fold)
    //receive that action and send it to TableService.java using a CompletableFuture
    public void onPreFlopAction(Player player, int seat, BigDecimal currentBet, BigDecimal potSize, BigDecimal minBet) {
        SeatRequest publicRequest= new SeatRequest();
        publicRequest.setType(RequestType.PLAYER_ACTION);
        publicRequest.setSeatIndex(seat);

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

    //Add the player(s) who won the pot and send that via WebSocket Object
    public void onCompleteHand(HashMap<Integer, Player> indexAndWinner) {
        IntPlayerMapRequest request = new IntPlayerMapRequest();
        request.setType(RequestType.COMPLETE_HAND);
        request.setIndexAndPlayer(indexAndWinner);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    public void onBoardCardsDealt(ArrayList<Card> cards) {
        DealBoardCardsRequest request = new DealBoardCardsRequest();
        request.setType(RequestType.BOARD_CARDS);
        request.setCards(cards);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    public void onHandRanking(String handRanking, String username) {
        HandRankingRequest request = new HandRankingRequest();
        request.setType(RequestType.HAND_RANKING);
        request.setHandRanking(handRanking);
        request.setUsername(username);

        messagingTemplate.convertAndSend("/topic/playerEvents/" + request.getUsername(), request);
    }

    public void onRebuy(String username, int seatIndex, String tableId) {
        RebuyRequest request = new RebuyRequest();
        request.setType(RequestType.REBUY);
        request.setSeatIndex(seatIndex);
        request.setTableId(tableId);

        messagingTemplate.convertAndSend("/topic/playerEvents/" + username, request);
    }

    public void onAddOn(int seatIndex, BigDecimal rebuyAmount) {
        AddOnRequest request = new AddOnRequest();
        request.setType(RequestType.ADD_ON);
        request.setSeatIndex(seatIndex);
        request.setRebuyAmount(rebuyAmount);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    public void onCleanUp(boolean isHandOver) {
        CleanUpRequest request = new CleanUpRequest();
        request.setType(RequestType.CLEAN_UP);
        request.setHandOver(isHandOver);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    @MessageMapping("/playerActionEvent")
    public void returnPreFlopAction(@Payload PlayerActionResponse response) {
        tableService.handlePlayerAction(response);
    }

    public void onEndPlayerAction(char action, String username, int seatIndex, BigDecimal betAmount, BigDecimal stackSize, BigDecimal potSize, BigDecimal currentStreetPotSize, boolean isPreFlop) {
        EndPlayerActionRequest request = new EndPlayerActionRequest();
        request.setType(RequestType.END_PLAYER_ACTION);
        request.setAction(action);
        request.setUsername(username);
        request.setSeat(seatIndex);
        request.setBet(betAmount);
        request.setStackSize(stackSize);
        request.setPotSize(potSize);
        request.setCurrentStreetPotSize(currentStreetPotSize);
        request.setPreFlop(isPreFlop);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    public void onShowdown(HashMap<Integer, Player> indexAndPlayer) {
        IntPlayerMapRequest request = new IntPlayerMapRequest();
        request.setType(RequestType.SHOWDOWN);
        request.setIndexAndPlayer(indexAndPlayer);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    public void onCalculateEquity(HashMap<Integer, String> indexAndCards, String boardInStringForm) {
        CalculateEquityRequest request = new CalculateEquityRequest();
        request.setType(RequestType.CALC_EQUITY);
        request.setIndexAndCards(indexAndCards);
        request.setBoardInStringForm(boardInStringForm);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    @MessageMapping("/foldEvent")
    public void foldEvent(@Payload FoldRequest request) {
        SeatRequest seatRequest = new SeatRequest();
        seatRequest.setType(request.getType());
        seatRequest.setSeatIndex(request.getSeat());
        messagingTemplate.convertAndSend("/topic/playerEvents/" + request.getUsername(), seatRequest);
    }
}
