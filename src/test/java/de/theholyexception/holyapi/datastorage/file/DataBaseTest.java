package de.theholyexception.holyapi.datastorage.file;

import de.theholyexception.holyapi.datastorage.sql.interfaces.MySQLInterface;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;

public class DataBaseTest {

    private static MySQLInterface db;

    @BeforeAll
    public static void setup() {
        System.out.println("Init");
        db = new MySQLInterface("homelab", 3306, "holyapi-test", "holyapi-test", "holyapi-test");
    }

    @Test
    @Order(1)
    public void createProcedure() {
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

    @Test
    @Order(2)
    public void readData() {
        try {
            ResultSet rs = db.executeQuery("call spTest()");
        } catch (Exception ex) {
            assert false;
        }
        assert true;
    }

}
