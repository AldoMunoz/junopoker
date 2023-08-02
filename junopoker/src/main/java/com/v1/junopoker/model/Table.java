package com.v1.junopoker.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class Table {
    private String gameType;
    private final int seatCount;
    private int pot;
    private final int[] stakes;
    //Changed "players" to "seats", more accurate
    private Player[] seats;
    private ArrayList<Card> board;
    //Changed "playerCount" to "seatedPlayerCount", more specific
    private int seatedPlayerCount;
    private Deck deck;
    private int bigBlind;
    private int smallBlind;
    //Changed "isRunning" to "gameIsRunning", more specific
    private boolean gameIsRunning;

    //initiates table
    public Table (String gameType, int[] stakes) {
        this.stakes = stakes;
        this.gameType = gameType;

        seatCount = 6;
        seats = new Player[seatCount];
        board = new ArrayList<Card>();
        seatedPlayerCount = 0;
        deck = new Deck();
        bigBlind = -1;
        smallBlind = -1;
        gameIsRunning = false;
    }
}
