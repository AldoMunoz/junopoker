package com.v1.junopoker.callback;

import com.v1.junopoker.model.Card;
import com.v1.junopoker.model.Player;

import java.util.ArrayList;

public interface TableCallback {
    void onButtonSet(int buttonIndex);

    void onPotInit(int sbIndex, int bbIndex, double sbAmount, double bbAmount, double potSize);

    void onHoleCardsDealt(String username, int seat, Card[] holeCards);

    void onPreFlopAction(Player player, int seat, float currentBet, float potSize, float minBet);

    void onEndPlayerAction(char action, String username, int seatIndex, float betAmount, float stackSize, float potSize);

    void onCompleteHand(Player[] seats);

    void onBoardCardsDealt(ArrayList<Card> cards);

    void onCleanUp();

}
