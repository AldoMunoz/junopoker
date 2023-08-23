package com.v1.junopoker.dto;

import com.v1.junopoker.model.Card;
import lombok.Data;

@Data
public class PrivateHoleCardsRequest {
    private RequestType type;
    private int seat;
    private Card[] cards;
}
