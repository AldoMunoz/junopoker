package com.v1.junopoker.dto;

import lombok.Data;

@Data
public class BlindRequest {
    private RequestType type;
    private int smallBlind;
    private int bigBlind;
    private int button;
}
