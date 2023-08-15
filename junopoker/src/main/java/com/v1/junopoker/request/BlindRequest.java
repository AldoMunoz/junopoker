package com.v1.junopoker.request;

import lombok.Data;

@Data
public class BlindRequest {
    private RequestType type;
    private int smallBlind;
    private int bigBlind;
}
