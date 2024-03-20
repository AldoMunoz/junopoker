package com.v1.junopoker.model;

import com.v1.junopoker.registry.TableRegistry;
import com.v1.junopoker.service.DeckService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;

@Getter
@Setter
public class Table {
    private String gameType;
    private final BigDecimal[] stakes;
    private final String TABLE_ID;
    private final int SEAT_COUNT;
    private BigDecimal pot;
    private BigDecimal currentStreetPot;
    private Player[] seats;
    private boolean[] playersSittingOut;
    private ArrayList<Card> board;
    private int activePlayerCount;
    private int seatedPlayerCount;
    private int seatedFoldCount;
    private int allInCount;
    private Deck deck;
    private int bigBlindIndex;
    private int smallBlindIndex;
    private int dealerButtonIndex;
    private BigDecimal currentBet;
    private boolean gameRunning;
    private boolean handOver;
    private boolean actionComplete;
    private boolean headsUp;
    private DeckService deckService;

    //initiates table
    public Table (String gameType, BigDecimal[] stakes) {
        this.stakes = stakes;
        this.gameType = gameType;

        //generate a table ID upon creation of the table
        TableRegistry tableRegistry = new TableRegistry();
        TABLE_ID = tableRegistry.generateID(this.gameType);

        SEAT_COUNT = 6;
        playersSittingOut = new boolean[SEAT_COUNT];
        seats = new Player[SEAT_COUNT];
        board = new ArrayList<>();
        seatedPlayerCount = 0;
        deck = new Deck();
        bigBlindIndex = -1;
        smallBlindIndex = -1;
        dealerButtonIndex = -1;
        headsUp = false;
        gameRunning = false;
        handOver = false;
        actionComplete = false;
        pot = BigDecimal.valueOf(0);
        currentStreetPot = BigDecimal.valueOf(0);
    }
}
