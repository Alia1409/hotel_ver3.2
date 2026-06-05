package test;

import model.Reservation;
import model.Room;
import persistence.StorageManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StorageManagerTest {

    private StorageManager storageManager;
    
    // Direct targets to match your exact hardcoded file string fields
    private static final String TXT_FILE = "Reservations.txt";
    private static final String JSON_FILE = "Reservations.json";
    private static final String ROOM_TXT_FILE = "Room.txt";
    private static final String ROOM_JSON_FILE = "Room.json";

    // Dynamic backup path locations
    private static final String BACKUP_PREFIX = "backup_";

    @BeforeEach
    public void setUp() {
        storageManager = new StorageManager();
        // Step 1: Securely preserve your real application data records
        backupFile(TXT_FILE);
        backupFile(JSON_FILE);
        backupFile(ROOM_TXT_FILE);
        backupFile(ROOM_JSON_FILE);
    }

    @AfterEach
    public void tearDown() {
        // Step 3: Revert your project folder exactly back to how it was before the test ran
        restoreFile(TXT_FILE);
        restoreFile(JSON_FILE);
        restoreFile(ROOM_TXT_FILE);
        restoreFile(ROOM_JSON_FILE);
    }

    @Test
    public void testSaveAndLoadRooms() {
        List<Room> testRooms = new ArrayList<>();
        testRooms.add(new Room("101", "Standard", 1, 85.50, "available", "Garden View"));
        testRooms.add(new Room("102", "Deluxe", 1, 150.00, "occupied", "Partial Sea View"));

        // Executes write routines straight to "Room.txt" / "Room.json"
        storageManager.saveRooms(testRooms);

        Assertions.assertTrue(new File(ROOM_TXT_FILE).exists(), "Room TXT output file must be present.");
        Assertions.assertTrue(new File(ROOM_JSON_FILE).exists(), "Room JSON output file must be present.");

        List<Room> loadedRooms = storageManager.loadRooms();

        Assertions.assertEquals(2, loadedRooms.size(), "Should load 2 test records.");
        Assertions.assertEquals("101", loadedRooms.get(0).getRoomNumber());
        Assertions.assertEquals("Garden View", loadedRooms.get(0).getDescription());
    }

    @Test
    public void testSaveAndLoadReservations() {
        List<Reservation> testReservations = new ArrayList<>();
        testReservations.add(new Reservation(
                "RES-001", "101", "Alice Smith", "alice@example.com", "555-0192",
                LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 15), "confirmed", "Needs extra towels"
        ));

        // Executes write routines straight to "Reservations.txt" / "Reservations.json"
        storageManager.saveReservations(testReservations);

        Assertions.assertTrue(new File(TXT_FILE).exists(), "Reservations TXT output file must be present.");
        Assertions.assertTrue(new File(JSON_FILE).exists(), "Reservations JSON output file must be present.");

        List<Reservation> loadedReservations = storageManager.loadReservations();

        Assertions.assertEquals(1, loadedReservations.size());
        Assertions.assertEquals("RES-001", loadedReservations.get(0).getId());
        Assertions.assertEquals("Alice Smith", loadedReservations.get(0).getGuestName());
    }

    @Test
    public void testLoadReservationsWhenFileDoesNotExist() {
        // Delete active files explicitly to force empty evaluation branch check
        deleteFile(TXT_FILE);
        deleteFile(JSON_FILE);

        List<Reservation> loaded = storageManager.loadReservations();
        Assertions.assertNotNull(loaded, "Returned collection should never be null.");
        Assertions.assertTrue(loaded.isEmpty(), "Collection should be empty when target data files don't exist.");
    }

    // Helper utilities to isolate file management states
    private void backupFile(String filename) {
        File original = new File(filename);
        if (original.exists()) {
            try {
                Files.copy(original.toPath(), new File(BACKUP_PREFIX + filename).toPath(), StandardCopyOption.REPLACE_EXISTING);
                original.delete(); // Clear original so test starts fresh
            } catch (IOException e) {
                System.err.println("Could not create backup for " + filename + ": " + e.getMessage());
            }
        }
    }

    private void restoreFile(String filename) {
        File backup = new File(BACKUP_PREFIX + filename);
        File original = new File(filename);
        
        // Wipe test artifacts
        if (original.exists()) {
            original.delete();
        }
        
        // Restore real user data assets
        if (backup.exists()) {
            try {
                Files.move(backup.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println("Could not restore data for " + filename + ": " + e.getMessage());
            }
        }
    }

    private void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            f.delete();
        }
    }
}
