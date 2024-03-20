package com.v1.junopoker.dto;

import lombok.Data;

@Data
public class NewPlayerRequest {
    private String tableID;
    private String username;
}
