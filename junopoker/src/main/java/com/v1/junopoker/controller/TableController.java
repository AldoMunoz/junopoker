package com.v1.junopoker.controller;

import com.v1.junopoker.model.Player;
import com.v1.junopoker.model.Table;
import com.v1.junopoker.service.TableService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
public class TableController {
    private final TableService tableService;

    @Autowired
    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @PostMapping("/createTable")
    public ResponseEntity<String> createTable(@RequestBody Table table, HttpSession session) {
        // Process the user data and create an instance of User
        Table sessionTable = new Table(table.getGameType(), table.getStakes());

        session.setAttribute("createTable", sessionTable);
        System.out.println("Created table successfully");

        return ResponseEntity.ok().body("{\"status\": \"success\"}");

    }
}
