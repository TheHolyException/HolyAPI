package de.theholyexception.holyapi.datastorage.file;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.*;

 @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConfigPropertyTest {

    private static ConfigProperty property;
    private static ConfigProperty property2;
    private static File file;
    private static File file2;

    @BeforeAll
    public static void setup() {
        file = new File("testfile.properties");
        file2 = new File("testfile2.properties");
        property = new ConfigProperty(file, "test");
        property2 = new ConfigProperty(file2, "test");
    }

    @AfterAll
    public static void cleanup() {
        file.delete();
        file2.delete();
    }

    @Test
    @Order(1)
    void createNew() {
        property.createNew();
        assertEquals(true, file.exists());
        property.setValue("testkey", "testvalue");
        property.saveConfig();
    }

    @Test
    @Order(2)
    void testCreateNew() {
        try {
            FileInputStream fis = new FileInputStream(file);

            property2.createNew(fis);
            assertEquals(true, file2.exists());

            String result = property2.getValue("testkey", String.class);
            assertEquals("testvalue", result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    @Order(3)
    void createNewIfNotExists() {
        try {
            property2.createNewIfNotExists();
            String result = property2.getValue("testkey", String.class);
            assertEquals("testvalue", result);
            result = property2.getValue("testvalue", String.class);
            file2.delete();
            assertEquals(null, result);
            assertEquals(false, property2.getFile().exists());
            property2.createNewIfNotExists();
            assertEquals(true, property2.getFile().exists());
            property2.setValue("testkey", "testvalue");
            property2.saveConfig();
            assertEquals("testvalue", property2.getValue("testkey", String.class));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    @Order(4)
    void testCreateNewIfNotExists() {
        try {
            FileInputStream fis = new FileInputStream(file);

            property2.createNewIfNotExists(fis);
            String result = property2.getValue("testkey", String.class);
            assertEquals("testvalue", result);
            result = property2.getValue("testvalue", String.class);
            file2.delete();
            assertEquals(null, result);
            assertEquals(false, property2.getFile().exists());
            property2.createNewIfNotExists(fis);
            assertEquals(true, property2.getFile().exists());
            assertEquals("testvalue", property2.getValue("testkey", String.class));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
    @Test
    @Order(5)
    void loadConfig() {
    }

    @Test
    @Order(6)
    void saveConfig() {
    }

    @Test
    @Order(9)
    void getFile() {
    }
     */
}