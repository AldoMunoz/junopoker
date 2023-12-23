package com.v1.junopoker.dto;

import lombok.Data;

@Data
public class UsernameRequest {
    private RequestType requestType;
    private String username;
    private int seatIndex;
}
