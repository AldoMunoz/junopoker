package com.v1.junopoker.controller;

import com.v1.junopoker.model.Player;
import com.v1.junopoker.model.Table;
import com.v1.junopoker.dto.PlayerRequest;
import com.v1.junopoker.service.PlayerService;
import com.v1.junopoker.service.TableService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> createPlayer(@RequestBody PlayerRequest createPlayerRequest, HttpSession session) {
        Player newPlayer = createPlayerRequest.getPlayer();
        int seat = createPlayerRequest.getSeat();
        Table table = (Table) session.getAttribute("table");

        if (table == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            tableService.addPlayer(table, newPlayer, seat);
            System.out.println("Added player successfully");

            return ResponseEntity.ok().body("{\"status\": \"success\"}");
        }
    }

    @GetMapping("/removePlayerAtSeat")
    public ResponseEntity<Player> removePlayerAtSeat(@RequestParam int seat, HttpSession session) {
        Table table = (Table) session.getAttribute("table");

        Player player = tableService.getPlayerAtSeat(table, seat);
        tableService.removePlayer(table, seat);

        return ResponseEntity.ok(player);
    }
}
