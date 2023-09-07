package com.v1.junopoker.dto;

import com.v1.junopoker.model.Card;
import lombok.Data;

@Data
public class EndPlayerActionRequest {
    private RequestType type;
    private int seat;
    private float bet;
    private char action;
    private float stackSize;
    private float potSize;
}
