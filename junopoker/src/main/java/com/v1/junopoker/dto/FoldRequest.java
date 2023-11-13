package com.v1.junopoker.dto;

import lombok.Data;

@Data
public class FoldRequest {
    private RequestType type;
    private String username;
    private int seat;
}
