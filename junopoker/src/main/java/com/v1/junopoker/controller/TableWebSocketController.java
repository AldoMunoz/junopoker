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
/*
Controller for communication between view and back-end logic for Table related actions via WebSocket
 */
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
    //returns Player Request sent from the front-end back to the front end
    //For when a player takes a seat at the table
    public PlayerRequest tableEvents(@Payload PlayerRequest request) {
        return request;
    }

    @MessageMapping("/playerEvents")
    //calls backend method to retrieve information on the table seats (which seats are occupied, and by whom)
    //creates a response method and sends it to a specific player in the front-end
    //TODO change the name of this method, not specific enough
    public void playerEvents(@Payload PlayerRequest request) {
        Player[] seats = tableService.getSeats(request.getTableID());

        SeatsAndSeatResponse response = new SeatsAndSeatResponse();
        response.setType(RequestType.SIT);
        response.setSeats(seats);
        response.setSeatIndex(request.getSeatIndex());

        messagingTemplate.convertAndSend("/topic/playerEvents/" + request.getPlayer().getUsername(), response);
    }

    @MessageMapping("/getSeats")
    //similar to the method above, but used for a different purpose
    //TODO figure out the difference between this method and playerEvents()
    public void getSeats(@Payload GetSeatsRequest request) {
        Player[] seats = tableService.getSeats(request.getTableId());

        SeatsResponse response = new SeatsResponse();
        response.setType(RequestType.SEATS);
        response.setSeats(seats);
        response.setTableID(response.getTableID());

        //System.out.println("sending message from getSeats");
        messagingTemplate.convertAndSend("/topic/playerEvents/" + request.getUsername(), response);
    }


    @MessageMapping("/countPlayers")
    //calls method to count the players seated at the table after a hand is completed
    public void countPlayers(@Payload String tableID) {
        tableService.countPlayers(tableID);
    }

    @MessageMapping("/standPlayerAtSeat")
    //calls method stand a player at a given seat
    public void standPlayerAtSeat(@Payload StandRequest request) {
        tableService.removePlayer(request.getTableId(), request.getSeatIndex());
    }

    @MessageMapping("/foldEvent")
    //creates dto to be passed to a player in the front end after they fold
    public void foldEvent(@Payload FoldRequest request) {
        SeatRequest seatRequest = new SeatRequest();
        seatRequest.setType(request.getType());
        seatRequest.setSeatIndex(request.getSeat());

        messagingTemplate.convertAndSend("/topic/playerEvents/" + request.getUsername(), seatRequest);
    }

    @MessageMapping("/rebuy")
    //calls method in the back end for logic when a player gets stacked and needs to rebuy
    public void rebuy(@Payload RebuyResponse request) {
        tableService.rebuyPlayer(request.getRebuyAmount(), request.getSeatIndex(), request.getTableId());
    }

    //Calls method in the back end to handle a player's input on their turn
    @MessageMapping("/playerActionEvent")
    public void returnPreFlopAction(@Payload PlayerActionResponse response) {
        tableService.handlePlayerAction(response);
    }

    /*
    START OF CALLBACK METHODS

    The following methods are called from the back end, used to send information to the front end
     */


    //sends DTO to front end to set the dealer button
    public void onButtonSet(int buttonIndex) {
        MoveButtonRequest request = new MoveButtonRequest();
        request.setType(RequestType.MOVE_BUTTON);
        request.setButton(buttonIndex);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    //send DTO to front end to initiate the pot by collecting blinds and antes
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

    //Creates 2 DTOs, one for public message and one private
    //Sends message to front end to deal hole cards
    //Each method call sends a pair of hole cards to each player
    public void onHoleCardsDealt(String username, int seat, Card[] holeCards) {
        SeatRequest publicRequest = new SeatRequest();
        publicRequest.setType(RequestType.DEAL_PRE);
        publicRequest.setSeatIndex(seat);

        PrivateHoleCardsRequest privateRequest = new PrivateHoleCardsRequest();
        privateRequest.setType(RequestType.DEAL_PRE);
        privateRequest.setSeat(seat);
        privateRequest.setCards(holeCards);

        //public message used to show dealt hole cards, face down
        messagingTemplate.convertAndSend("/topic/tableEvents", publicRequest);
        //Adds a slight delay between message
        //Used to prevent errors in the front end for if private message completes before public one
        try {
            Thread.sleep(100); // Sleep for 100 milliseconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //private message used to show a player their hole cards, face up
        messagingTemplate.convertAndSend("/topic/playerEvents/" + username, privateRequest);
    }

    //Creates 2 DTOs, one for public message and one private
    //Send public message to highlight which player's turn it is
    //Sends private message to populate view with various actions a player can take on their turn
    public void onAction(Player player, int seat, BigDecimal currentBet, BigDecimal potSize, BigDecimal minBet) {
        SeatRequest publicRequest = new SeatRequest();
        publicRequest.setType(RequestType.PLAYER_ACTION);
        publicRequest.setSeatIndex(seat);

        messagingTemplate.convertAndSend("/topic/tableEvents", publicRequest);

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

    //Creates DTO to send the winners of a given hand to the front end
    public void onCompleteHand(HashMap<Integer, Player> indexAndWinner) {
        IntPlayerMapRequest request = new IntPlayerMapRequest();
        request.setType(RequestType.COMPLETE_HAND);
        request.setIndexAndPlayer(indexAndWinner);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    //Creates DTO to send community cards to the front end (flop, turn, or river)
    public void onBoardCardsDealt(ArrayList<Card> cards) {
        DealBoardCardsRequest request = new DealBoardCardsRequest();
        request.setType(RequestType.BOARD_CARDS);
        request.setCards(cards);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    //Creates DTO to privately send a player's hand ranking to that player
    public void onHandRanking(String handRanking, String username) {
        HandRankingRequest request = new HandRankingRequest();
        request.setType(RequestType.HAND_RANKING);
        request.setHandRanking(handRanking);
        request.setUsername(username);

        messagingTemplate.convertAndSend("/topic/playerEvents/" + request.getUsername(), request);
    }

    //Creates DTO to privately send add player back to the game after they re-bought
    //TODO check if that description is right
    public void onRebuy(String username, int seatIndex, String tableId) {
        RebuyRequest request = new RebuyRequest();
        request.setType(RequestType.REBUY);
        request.setSeatIndex(seatIndex);
        request.setTableId(tableId);

        messagingTemplate.convertAndSend("/topic/playerEvents/" + username, request);
    }

    //Creates DTO to send a rebuy amount to the front end
    public void onAddOn(int seatIndex, BigDecimal rebuyAmount) {
        AddOnRequest request = new AddOnRequest();
        request.setType(RequestType.ADD_ON);
        request.setSeatIndex(seatIndex);
        request.setRebuyAmount(rebuyAmount);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    //Creates DTO to collect the pot after each betting round
    public void onCleanUp(boolean isHandOver) {
        CleanUpRequest request = new CleanUpRequest();
        request.setType(RequestType.CLEAN_UP);
        request.setHandOver(isHandOver);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    //Creates DTO after interpreting a player's action to show response in the front end
    //For example if a player bet $2, that action will be sent and presented in the front end
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

    //Creates DTO with players still in the hand to display a showdown in the front-end
    public void onShowdown(HashMap<Integer, Player> indexAndPlayer) {
        IntPlayerMapRequest request = new IntPlayerMapRequest();
        request.setType(RequestType.SHOWDOWN);
        request.setIndexAndPlayer(indexAndPlayer);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    //Creates DTO with all in equities to display in the front end
    public void onCalculateEquity(HashMap<Integer, String> indexAndCards, String boardInStringForm) {
        CalculateEquityRequest request = new CalculateEquityRequest();
        request.setType(RequestType.CALC_EQUITY);
        request.setIndexAndCards(indexAndCards);
        request.setBoardInStringForm(boardInStringForm);

        messagingTemplate.convertAndSend("/topic/tableEvents", request);
    }

    //Creates DTO to stand a player up in the front-end
    public void onStand(int seatIndex, String username) {
        StandResponse response = new StandResponse();
        response.setType(RequestType.STAND);
        response.setSeatIndex(seatIndex);
        response.setUsername(username);

        messagingTemplate.convertAndSend("/topic/tableEvents", response);
        messagingTemplate.convertAndSend("/topic/playerEvents/" + username, response);
    }
}
