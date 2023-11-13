package com.v1.junopoker.dto;

import com.v1.junopoker.model.Player;
import lombok.Data;

import java.util.ArrayList;

@Data
public class CompleteHandRequest {
    private RequestType type;
    private Player[] seats;
}
