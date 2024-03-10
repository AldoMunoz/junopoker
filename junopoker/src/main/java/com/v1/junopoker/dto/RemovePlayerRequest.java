package com.v1.junopoker.dto;

import com.v1.junopoker.model.Player;
import lombok.Data;

@Data
public class RemovePlayerRequest {
    private RequestType type;
    private Player[] seats;
    private String username;
}
