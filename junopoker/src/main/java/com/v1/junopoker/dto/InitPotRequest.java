package com.v1.junopoker.dto;

import lombok.Data;

@Data
public class InitPotRequest {
    private RequestType type;
    private int smallBlind;
    private int bigBlind;
    private double sbAmount;
    private double bbAmount;
    private double potSize;
}
