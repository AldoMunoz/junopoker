package com.v1.junopoker.dto;

import com.v1.junopoker.model.Player;
import lombok.Data;

import java.math.BigDecimal;

@Data

public class PlayerActionRequest {
    private RequestType type;
    private Player player;
    private int seat;
    private BigDecimal currentBet;
    private BigDecimal potSize;
    private BigDecimal minBet;
}
