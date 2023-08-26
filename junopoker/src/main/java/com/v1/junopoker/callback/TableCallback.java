package com.v1.junopoker.callback;

import com.v1.junopoker.model.Card;
import com.v1.junopoker.model.Player;

public interface TableCallback {
    void onButtonSet(int buttonIndex);

    void onPotInit(int sbIndex, int bbIndex, double sbAmount, double bbAmount, double potSize);

    void onHoleCardsDealt(String username, int seat, Card[] holeCards);

    void onPreFlopBetting(Player player);
}
