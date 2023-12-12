package com.v1.junopoker.model;

import lombok.Data;

@Data
public class Player {
    private String username;
    private float chipCount;
    private Card[] holeCards = null;
    private Hand hand = null;
    private boolean inHand = false;
    private boolean isAllIn = false;
    private float currentBet = 0;
    private boolean isActive = false;
}
