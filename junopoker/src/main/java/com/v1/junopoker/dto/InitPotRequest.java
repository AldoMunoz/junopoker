package com.v1.junopoker.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InitPotRequest {
    private RequestType type;
    private int smallBlind;
    private int bigBlind;
    private BigDecimal sbAmount;
    private BigDecimal bbAmount;
    private BigDecimal potSize;
}
