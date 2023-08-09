package com.v1.junopoker.controller;

import com.v1.junopoker.model.Player;
import lombok.*;

@Data
public class CreatePlayerRequest {
    private Player player;
    private int seat;
}
