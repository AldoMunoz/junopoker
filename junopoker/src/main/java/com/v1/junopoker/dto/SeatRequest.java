package com.v1.junopoker.dto;

import lombok.Data;
@Data
public class SeatRequest {
    private RequestType type;
    private int seat;
}
