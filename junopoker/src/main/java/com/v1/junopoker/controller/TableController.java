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
        Table sessionTable = (Table) session.getAttribute("table");

        if (sessionTable == null) {
            sessionTable = table;
            session.setAttribute("table", sessionTable);
            System.out.println("Created table successfully");
        }
        else {
            sessionTable.setGameType(table.getGameType());
            sessionTable.setBigBlind(table.getBigBlind());
            sessionTable.setSmallBlind(table.getSmallBlind());
            System.out.println("Updated table successfully");
        }
        //Table sessionTable = new Table(table.getGameType(), table.getStakes());
        return ResponseEntity.ok().body("{\"status\": \"success\"}");
    }
}
