package com.v1.junopoker.dto;

import com.v1.junopoker.model.Card;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EndPlayerActionRequest {
    private RequestType type;
    private char action;
    private String username;
    private int seat;
    private BigDecimal bet;
    private BigDecimal stackSize;
    private BigDecimal potSize;
    private BigDecimal currentStreetPotSize;
    private boolean isPreFlop;
}
