package com.v1.junopoker.dto;

import lombok.Data;

@Data
public class PlayerActionResponse {
    private char action;
    private double betAmount;
    private int seat;
    private double stackSize;
    private double potSize;
}
