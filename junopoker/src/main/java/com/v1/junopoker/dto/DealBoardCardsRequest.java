package com.v1.junopoker.dto;

import com.v1.junopoker.model.Card;
import lombok.Data;

import java.util.ArrayList;

@Data
public class DealBoardCardsRequest {
    RequestType type;
    ArrayList<Card> cards;
}
