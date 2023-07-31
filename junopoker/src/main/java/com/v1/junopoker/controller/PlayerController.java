package com.v1.junopoker.controller;

import com.v1.junopoker.model.Player;
import com.v1.junopoker.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
public class PlayerController {
    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/createPlayer")
    public ResponseEntity<String> createPlayer (@RequestBody Player player, HttpSession session) {
        Player newPlayer = new Player(player.getUsername(), player.getChipCount());

        session.setAttribute("createTable", newPlayer);
        System.out.println("Added player successfully");

        return ResponseEntity.ok().body("{\"status\": \"success\"}");
    }
}
