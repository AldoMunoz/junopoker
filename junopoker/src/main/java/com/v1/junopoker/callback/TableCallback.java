package com.v1.junopoker.callback;

import com.v1.junopoker.model.Card;
import com.v1.junopoker.model.Player;

import java.util.ArrayList;
import java.util.HashMap;

public interface TableCallback {
    void onButtonSet(int buttonIndex);

    void onPotInit(int sbIndex, int bbIndex, double sbAmount, double bbAmount, double potSize);

    void onHoleCardsDealt(String username, int seat, Card[] holeCards);

    void onPreFlopAction(Player player, int seat, double currentBet, double potSize, double minBet);

    void onEndPlayerAction(char action, String username, int seatIndex, double betAmount, double stackSize, double potSize, double currentStreetPotSize, boolean isPreFlop);

    void onShowdown(HashMap<Integer, Player> indexAndPlayer);
    void onCompleteHand(HashMap<Integer, Player> indexAndWinner);

    void onBoardCardsDealt(ArrayList<Card> cards);

    void onHandRanking(String handRanking, String username);
    void onCleanUp(boolean isHandOver);
}
