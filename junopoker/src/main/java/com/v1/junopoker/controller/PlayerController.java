package com.v1.junopoker.controller;

import com.v1.junopoker.model.Player;
import com.v1.junopoker.dto.PlayerRequest;
import com.v1.junopoker.service.PlayerService;
import com.v1.junopoker.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
/*
Controller for communication between view and back-end logic for Player related actions
 */
public class PlayerController {
    private final PlayerService playerService;
    private final TableService tableService;

    @Autowired
    public PlayerController(PlayerService playerService, TableService tableService) {
        this.playerService = playerService;
        this.tableService = tableService;
    }

    @PostMapping("/createPlayer")
    //calls method to create a player in the backend logic using user-entered data from front end
    public ResponseEntity<String> createPlayer(@RequestBody PlayerRequest playerRequest) {
        //TODO try catch block
        tableService.addPlayer(playerRequest.getTableID(), playerRequest.getPlayer(), playerRequest.getSeatIndex());

        return ResponseEntity.ok().body("{\"status\": \"success\"}");
    }

    @GetMapping("/removePlayerAtSeat")
    //calls methods to find and then remove a player ata certain seat
    public ResponseEntity<Player> removePlayerAtSeat(@RequestParam int seat, @RequestParam String tableID) {
        //todo try catch block
        Player player = tableService.getPlayer(tableID, seat);
        tableService.removePlayer(tableID, seat);

        return ResponseEntity.ok(player);
    }
}
