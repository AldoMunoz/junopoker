package com.v1.junopoker.dto;

import lombok.Data;

@Data
public class HandRankingRequest {
    private RequestType type;
    private String handRanking;
    private String username;
}
