package com.v1.junopoker.model;

import com.v1.junopoker.service.DeckService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class Table {
    private String gameType;
    private final int seatCount;
    private float pot;
    private final int[] stakes;
    //Changed "players" to "seats", more accurate
    private Player[] seats;
    private ArrayList<Card> board;
    //Changed "playerCount" to "seatedPlayerCount", more specific
    private int seatedPlayerCount;
    private int seatedFoldCount;
    private Deck deck;
    private int bigBlind;
    private int smallBlind;
    private int dealerButton;
    private float currentBet;
    private boolean gameRunning;
    private boolean handOver;
    private DeckService deckService;

    //initiates table
    public Table (String gameType, int[] stakes) {
        this.stakes = stakes;
        this.gameType = gameType;

        seatCount = 6;
        seats = new Player[seatCount];
        board = new ArrayList<>();
        seatedPlayerCount = 0;
        deck = new Deck();
        bigBlind = -1;
        smallBlind = -1;
        dealerButton = -1;
        gameRunning = false;
        handOver = false;
    }
}
