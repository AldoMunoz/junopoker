package com.v1.junopoker.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TableRequest {
    private BigDecimal[] stakes;
    private String gameType;
    private String tableID;
}
