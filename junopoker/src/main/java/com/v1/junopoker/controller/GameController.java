package com.v1.junopoker.controller;

import com.v1.junopoker.model.Player;
import com.v1.junopoker.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GameController {
    private final TableService tableService;

    @Autowired
    public GameController(TableService tableService) {
        this.tableService = tableService;
    }

    @PostMapping("/action")
    public String handlePlayerAction(@RequestParam String action, @RequestParam int betAmount) {
        return"redirect:/";
    }
}
