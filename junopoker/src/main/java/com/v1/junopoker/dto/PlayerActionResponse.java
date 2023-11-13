package com.v1.junopoker.dto;

import lombok.Data;

@Data
public class PlayerActionResponse {
    private char action;
    private float betAmount;
    private int seat;
    private float stackSize;
    private float potSize;
}
