package com.v1.junopoker.controller;

import com.v1.junopoker.dto.TableRequest;
import com.v1.junopoker.model.Table;
import com.v1.junopoker.service.TableService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TableController {
    private final TableService tableService;

    @Autowired
    public TableController(TableService tableService) {
        this.tableService = tableService;
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

        tableService.registerTable(table);
        return ResponseEntity.ok().body(table.getTABLE_ID());
    }

    @GetMapping("/getTableData")
    public ResponseEntity<TableRequest> getTableData(HttpSession session) {
        String tableID = (String) session.getAttribute("tableId");
        return ResponseEntity.ok().body(tableService.getBasicTableInfo(tableID));
    }
}
