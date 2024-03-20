package com.v1.junopoker.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
public class Player {
    private String username;
    private BigDecimal chipCount;
    private Card[] holeCards = null;
    private Hand hand = null;
    private boolean inHand = false;
    private boolean isAllIn = false;
    private BigDecimal currentBet;
    private BigDecimal startingStackThisHand = BigDecimal.valueOf(0);
    private BigDecimal amountBetThisHand = BigDecimal.valueOf(0);
    private boolean isActive = false;
}
