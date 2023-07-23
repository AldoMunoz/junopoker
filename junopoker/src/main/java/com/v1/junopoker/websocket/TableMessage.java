package com.v1.junopoker.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TableMessage {
    private String messageType;
    private String content;
}
