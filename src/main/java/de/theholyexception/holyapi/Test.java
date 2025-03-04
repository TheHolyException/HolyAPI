package de.theholyexception.holyapi;

import de.theholyexception.holyapi.datastorage.sql.Result;
import de.theholyexception.holyapi.datastorage.sql.interfaces.MySQLInterface;
import de.theholyexception.holyapi.util.logger.LogLevel;
import de.theholyexception.holyapi.util.logger.LoggerProxy;

public class Test {

    public static void main(String[] args) throws ClassNotFoundException {
        LoggerProxy.log(LogLevel.DEBUG, "debug");
        LoggerProxy.log(LogLevel.INFO, "info");
        LoggerProxy.log(LogLevel.WARN, "warn");
        LoggerProxy.log(LogLevel.ERROR, "error");
    }

    static MySQLInterface db;

    public static void setup() {
    }

    public static void createProcedure() {
        if (db == null) setup();
        try {
            String query = "CREATE OR REPLACE PROCEDURE `holyapi-test`.`spTest`()\n" +
                    "BEGIN\n" +
                    "    select 1 as 'nError', 2 as 'nCode';\n" +
                    "    select 3 as 'nData', '4' as 'szData';\n" +
                    "END";
            System.out.println(query);
            db.execute(query);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert false;
        }
        assert true;
    }

    public static void readData() {
        try {
            Result rs = db.getResult("call spTest()");
            System.out.println(rs.getTable(1).getRow(0).get(1, String.class));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
