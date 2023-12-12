package com.v1.junopoker.dto;

import com.v1.junopoker.model.Card;
import lombok.Data;

@Data
public class EndPlayerActionRequest {
    private RequestType type;
    private char action;
    private String username;
    private int seat;
    private double bet;
    private double stackSize;
    private double potSize;
    private double currentStreetPotSize;
    private boolean isPreFlop;
}
