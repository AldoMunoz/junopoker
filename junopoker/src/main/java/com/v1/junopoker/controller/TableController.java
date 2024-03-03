package com.v1.junopoker.controller;

import com.v1.junopoker.dto.TableRequest;
import com.v1.junopoker.model.Table;
import com.v1.junopoker.service.TableService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
/*
Controller for communication between view and back-end logic for Table related actions via Http
Eventually this class shouldn't be used, all communications should be done using WebSocket
 */
public class TableController {
    private final TableService tableService;

    @Autowired
    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping("/doesTableExist")
    //checks back end logic to see if table with given table ID exists
    public ResponseEntity<Boolean> doesTableExist(HttpSession session) {
        //TODO try catch
        String tableID = (String) session.getAttribute("tableId");
        if(tableID == null) return ResponseEntity.ok().body(false);
        System.out.println("table ID: " + tableID);
        boolean tableExists = tableService.doesTableExist(tableID);

        //returns true of false depending on the existence of the table
        if(tableExists) return ResponseEntity.ok().body(true);
        else return ResponseEntity.ok().body(false);
    }

    @PostMapping("/storeTableData")
    //calls method to register a table in the backend
    public ResponseEntity<String> storeTableData(@RequestBody Table table, HttpSession session) {
        //TODO try catch
        session.setAttribute("tableId", table.getTABLE_ID());

        tableService.registerTable(table);
        System.out.println(table.getTABLE_ID());

        return ResponseEntity.ok().body(table.getTABLE_ID());
    }

    @GetMapping("/getTableData")
    //calls method to retrieve basic table info from the backend
    public ResponseEntity<TableRequest> getTableData(HttpSession session) {
        //TODO try catch
        String tableID = (String) session.getAttribute("tableId");
        return ResponseEntity.ok().body(tableService.getBasicTableInfo(tableID));
    }
}
