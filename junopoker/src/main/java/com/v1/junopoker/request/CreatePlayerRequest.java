package com.v1.junopoker.request;

import com.v1.junopoker.model.Player;
import lombok.*;

@Data
public class CreatePlayerRequest {
    private RequestType type;
    private Player player;
    private int seat;
}
