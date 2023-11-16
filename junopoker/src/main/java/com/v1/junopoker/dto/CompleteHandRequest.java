package com.v1.junopoker.dto;

import com.v1.junopoker.model.Player;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class CompleteHandRequest {
    private RequestType type;
    private HashMap<Integer, Player> indexAndWinner;
}
