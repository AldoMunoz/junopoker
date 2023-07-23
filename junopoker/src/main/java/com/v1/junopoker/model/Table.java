package com.v1.junopoker.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class Table {
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
    public Table (char tableType, int seatCount, int[] stakes, String gameType) {
        this.seatCount = seatCount;
        this.stakes = stakes;

        seats = new Player[seatCount];
        board = new ArrayList<Card>();
        seatedPlayerCount = 0;
        deck = new Deck();
        bigBlind = -1;
        smallBlind = -1;
        gameIsRunning = false;
    }
}
