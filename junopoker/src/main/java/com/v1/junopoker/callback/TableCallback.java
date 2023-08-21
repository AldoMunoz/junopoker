package com.v1.junopoker.callback;

import com.v1.junopoker.model.Player;

public interface TableCallback {
    void onBlindsSet(int smallBlindIndex, int bigBlindIndex, int dealerIndex);

    void onCardsDealt(Player[] players);

    void onPreFlopBetting(Player player);
}
