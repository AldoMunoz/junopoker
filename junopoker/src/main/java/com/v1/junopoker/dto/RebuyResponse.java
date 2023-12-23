package com.v1.junopoker.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RebuyResponse {
    private RequestType type;
    private BigDecimal rebuyAmount;
    private int seatIndex;
    private String tableId;
}
