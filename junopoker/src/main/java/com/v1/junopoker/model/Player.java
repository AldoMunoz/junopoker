package com.v1.junopoker.model;

import lombok.Data;
@Data
public class Player {
    private String username;
    private int chipCount;
    private Card[] holeCards;
    private Hand hand;
    private boolean inHand;
}
