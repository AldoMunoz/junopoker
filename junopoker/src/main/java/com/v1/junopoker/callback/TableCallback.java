package com.v1.junopoker.callback;

import com.v1.junopoker.model.Player;
import com.v1.junopoker.request.BlindRequest;

public interface TableCallback {
    void onBlindsSet(int smallBlindIndex, int bigBlindIndex);

    void onCardsDealt(Player[] players);

    void onPreFlopBetting(Player player);
}
