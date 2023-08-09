package com.v1.junopoker.controller;

import com.v1.junopoker.model.Player;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePlayerRequest {
    private Player player;
    private int seat;
}
