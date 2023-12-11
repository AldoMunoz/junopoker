package com.v1.junopoker.dto;

import com.v1.junopoker.model.Player;
import lombok.Data;

import java.util.HashMap;

@Data
public class IntPlayerMapRequest {
    private RequestType type;
    private HashMap<Integer, Player> indexAndPlayer;
}
