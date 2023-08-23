package com.v1.junopoker.dto;

import lombok.Data;

@Data
public class MoveButtonRequest {
    private RequestType type;
    private int button;
}
