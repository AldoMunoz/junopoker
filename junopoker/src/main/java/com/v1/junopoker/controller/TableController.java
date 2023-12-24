package com.v1.junopoker.controller;

import com.v1.junopoker.dto.TableRequest;
import com.v1.junopoker.model.Player;
import com.v1.junopoker.model.Table;
import com.v1.junopoker.service.TableService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/doesTableExist")
    public ResponseEntity<Boolean> doesTableExist(HttpSession session) {
        String tableID = (String) session.getAttribute("tableId");
        if(tableID == null) return ResponseEntity.ok().body(false);
        System.out.println("table ID: " + tableID);
        boolean tableExists = tableService.doesTableExist(tableID);

        if(tableExists) return ResponseEntity.ok().body(true);
        else return ResponseEntity.ok().body(false);
    }

    @PostMapping("/storeTableData")
    public ResponseEntity<String> storeTableData(@RequestBody Table table, HttpSession session) {
        session.setAttribute("tableId", table.getTABLE_ID());

        System.out.println("storeTableData in TableController: " + table.getTABLE_ID());
        tableService.registerTable(table);

        return ResponseEntity.ok().body(table.getTABLE_ID());
    }

    @GetMapping("/getTableData")
    public ResponseEntity<TableRequest> getTableData(HttpSession session) {
        String tableID = (String) session.getAttribute("tableId");
        return ResponseEntity.ok().body(tableService.getBasicTableInfo(tableID));
    }



    @PutMapping("/setTableData")
    public ResponseEntity<String> setTableData(@RequestBody Table table, HttpSession session) {
        Table sessionTable = (Table) session.getAttribute("table");
        sessionTable.setGameType(table.getGameType());
        sessionTable.setSmallBlindIndex(table.getSmallBlindIndex());
        sessionTable.setBigBlindIndex(table.getBigBlindIndex());

        return ResponseEntity.ok().body("{\"status\": \"stored\"}");
    }

    @GetMapping("/startGame")
    public ResponseEntity<Table> startGame(HttpSession session) {
        Table sessionTable = (Table) session.getAttribute("table");
        System.out.println("startGame in TableController: " + sessionTable.getTABLE_ID());
        return ResponseEntity.ok(sessionTable);
    }
}
