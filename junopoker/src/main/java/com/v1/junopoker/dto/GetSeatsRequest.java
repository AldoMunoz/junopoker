package com.v1.junopoker.dto;

import lombok.Data;

@Data
public class GetSeatsRequest {
    private RequestType type;
    private String tableId;
    private String username;
}
