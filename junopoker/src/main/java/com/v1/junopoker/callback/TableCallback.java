package com.v1.junopoker.callback;

import com.v1.junopoker.model.Card;
import com.v1.junopoker.model.Player;

import java.util.ArrayList;

public interface TableCallback {
    void onButtonSet(int buttonIndex);

    void onPotInit(int sbIndex, int bbIndex, double sbAmount, double bbAmount, double potSize);

    void onHoleCardsDealt(String username, int seat, Card[] holeCards);

    void onPreFlopAction(Player player, int seat, float currentBet, float potSize, float minBet);

    void onCompleteHand(Player[] seats);

    void onFlopDealt(ArrayList<Card> flop);

}
