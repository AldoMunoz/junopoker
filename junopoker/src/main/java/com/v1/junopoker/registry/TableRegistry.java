package com.v1.junopoker.registry;

import com.v1.junopoker.model.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

//Registry is used to store Table information
//Substitute for a Database, which will be implemented at a later date
public class TableRegistry {
    private Map<String, Table> tableMap;

    public TableRegistry() {
        tableMap = new HashMap<>();
    }

    public void registerTable(String tableID, Table table) {
        tableMap.put(tableID, table);
    }

    public String generateID(String gameType) {
        Random rand = new Random();
        int idNo = rand.nextInt(10000);
        String stringIdNo = String.format("%04d", idNo);

        if (gameType.equals("NLH")) return "NLH" + stringIdNo;
        return "";
    }

    public Table getTableByID (String tableID) {
        return tableMap.get(tableID);
    }
}
