package com.v1.junopoker.dto;


import com.v1.junopoker.model.Card;
import com.v1.junopoker.model.StrippedPlayer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;

@Data
public class TableInfoResponse {
    private RequestType type;
    private ArrayList<StrippedPlayer> strippedPlayers;
    private BigDecimal totalPot;
    private BigDecimal currentStreetPot;
    private int buttonIndex;
    private ArrayList<Card> board;
    private int actingPlayerIndex;
}
