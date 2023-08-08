package com.v1.junopoker.controller;

import com.v1.junopoker.model.Player;
import com.v1.junopoker.model.Table;
import com.v1.junopoker.service.PlayerService;
import com.v1.junopoker.service.TableService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
public class PlayerController {
    private final PlayerService playerService;
    private final TableService tableService;

    @Autowired
    public PlayerController(PlayerService playerService, TableService tableService) {
        this.playerService = playerService;
        this.tableService = tableService;
    }

    @PostMapping("/createPlayer")
    public ResponseEntity<String> createPlayer(@RequestBody CreatePlayerRequest createPlayerRequest, HttpServletRequest request) {
        Player newPlayer = createPlayerRequest.getPlayer();
        int seat = createPlayerRequest.getSeat();
        HttpSession session = request.getSession();
        Table table = (Table) session.getAttribute("table");

        if (table == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            tableService.addPlayer(table, newPlayer, seat);
            System.out.println("Added player successfully");

            return ResponseEntity.ok().body("{\"status\": \"success\"}");
        }
    }
}
