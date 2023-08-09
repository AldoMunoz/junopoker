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

    @GetMapping("/checkTableData")
    public ResponseEntity<String> checkTableData(HttpSession session) {
        Table table = (Table) session.getAttribute("table");
        if (table != null) {
            return ResponseEntity.ok().body("{\"status\": \"exists\"}");
        } else {
            return ResponseEntity.ok().body("{\"status\": \"notExists\"}");
        }
    }

    @GetMapping("/getTableData")
    public ResponseEntity<Table> getTableData(HttpSession session) {
        Table table = (Table) session.getAttribute("table");
        if (table != null) {
            return ResponseEntity.ok().body(table);
        } else {
            // Return an appropriate status code if the table data is not available
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/storeTableData")
    public ResponseEntity<String> storeTableData(@RequestBody Table table, HttpSession session) {
        session.setAttribute("table", table);
        return ResponseEntity.ok().body("{\"status\": \"stored\"}");
    }
}
