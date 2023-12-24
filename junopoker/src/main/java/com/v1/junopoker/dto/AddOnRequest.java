package com.v1.junopoker.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddOnRequest {
    private RequestType type;
    private int seatIndex;
    private BigDecimal rebuyAmount;
}
