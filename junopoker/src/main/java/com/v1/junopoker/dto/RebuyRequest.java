package com.v1.junopoker.dto;

import lombok.Data;

@Data
public class RebuyRequest {
    private RequestType type;
    private int seatIndex;
    private String tableId;
}
