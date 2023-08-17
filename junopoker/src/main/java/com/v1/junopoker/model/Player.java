package com.v1.junopoker.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player {
    private String username;
    private int chipCount;
    private Card[] holeCards;
    private Hand hand;
    private boolean inHand;
    private int currentBet;
    private boolean isActive;

    public Player (String username, int chipCount) {
        this.username = username;
        this.chipCount = chipCount;

        currentBet = 0;
    }

    public boolean getInHand() {
        return inHand;
    }

    public boolean getIsActive() {
        return isActive;
    }
}
