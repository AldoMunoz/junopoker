package com.v1.junopoker.controller;

import com.v1.junopoker.model.Player;
import com.v1.junopoker.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TableController {
    private final TableService tableService;

    @Autowired
    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @PostMapping("/action")
    public String handlePlayerAction(@RequestParam String action, @RequestParam int betAmount) {
        return"redirect:/";
    }
    @PostMapping("/poker")
    public ResponseEntity<String> createPlayer(@RequestParam Player player) {

        return new ResponseEntity<>("User added successfully!", HttpStatus.OK);
    }
}
