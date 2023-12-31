package com.v1.junopoker.dto;

import com.v1.junopoker.model.Player;
import lombok.*;

@Data
public class PlayerRequest {
    private RequestType type;
    private Player player;
    private int seat;
}
