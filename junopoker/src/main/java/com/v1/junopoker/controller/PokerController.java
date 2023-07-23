package com.v1.junopoker.controller;

import com.v1.junopoker.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PokerController {
    private final PlayerService playerService;

    @Autowired
    public PokerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/")
    public String showPokerTable(Model model) {
        //TODO logic to load poker table into view and populate with relevant data
        return "index";
    }
}
