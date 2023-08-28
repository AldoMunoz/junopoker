package com.v1.junopoker.dto;

import com.v1.junopoker.model.Player;
import lombok.Data;

@Data

public class PlayerActionRequest {
    private RequestType type;
    private Player player;
    private int seat;
    private float currentBet;
}
