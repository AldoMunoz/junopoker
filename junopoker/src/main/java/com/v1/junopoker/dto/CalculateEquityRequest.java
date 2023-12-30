package com.v1.junopoker.dto;

import lombok.Data;

import java.util.HashMap;

@Data
public class CalculateEquityRequest {
    private RequestType type;
    private HashMap<Integer, String> indexAndCards;
    private String boardInStringForm;
}
