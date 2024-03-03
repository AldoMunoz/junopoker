package com.v1.junopoker.callback;

import com.v1.junopoker.model.Card;
import com.v1.junopoker.model.Player;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
/*
Interface for all callback methods called by the backend game logic
methods are used to send information to the front-end whenever required
 */
public interface TableCallback {
    void onButtonSet(int buttonIndex);

    void onPotInit(int sbIndex, int bbIndex, BigDecimal sbAmount, BigDecimal bbAmount, BigDecimal potSize);

    void onHoleCardsDealt(String username, int seat, Card[] holeCards);

    void onAction(Player player, int seat, BigDecimal currentBet, BigDecimal potSize, BigDecimal minBet);

    void onEndPlayerAction(char action, String username, int seatIndex, BigDecimal betAmount, BigDecimal stackSize, BigDecimal potSize, BigDecimal currentStreetPotSize, boolean isPreFlop);

    void onShowdown(HashMap<Integer, Player> indexAndPlayer);

    void onCalculateEquity(HashMap<Integer, String > indexAndCards, String boardInStringForm);
    void onCompleteHand(HashMap<Integer, Player> indexAndWinner);

    void onBoardCardsDealt(ArrayList<Card> cards);

    void onHandRanking(String handRanking, String username);

    void onRebuy(String username, int seatIndex, String tableId);

    void onAddOn(int seatIndex, BigDecimal rebuyAmount);
    void onCleanUp(boolean isHandOver);
    void onStand(int seatIndex, String username);
}
