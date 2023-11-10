package com.v1.junopoker.dto;

import lombok.Data;

@Data
public class CleanUpRequest {
    private RequestType type;
    private boolean handOver;
}
