package com.v1.junopoker.dto;

import lombok.Data;

import java.util.HashMap;

@Data
public class CalculateEquityRequest {
    private RequestType type;
    private HashMap<String, Integer> cardsAndIndex;
    private String boardInStringForm;
}
