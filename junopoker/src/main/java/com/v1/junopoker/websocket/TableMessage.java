package com.v1.junopoker.websocket;

import com.v1.junopoker.model.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TableMessage {
    private MessageType messageType;
    private Player player;
}
