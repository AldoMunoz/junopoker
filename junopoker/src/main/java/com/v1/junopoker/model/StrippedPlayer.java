package com.v1.junopoker.model;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class StrippedPlayer {
    private String username;
    private BigDecimal chipCount;
    private boolean inHand;
    private int seatIndex;
    private BigDecimal currentBet;
}
