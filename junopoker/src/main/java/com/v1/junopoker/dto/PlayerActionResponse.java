package com.v1.junopoker.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlayerActionResponse {
    private char action;
    private BigDecimal betAmount;
    private int seat;
    private BigDecimal stackSize;
    private BigDecimal potSize;
}
