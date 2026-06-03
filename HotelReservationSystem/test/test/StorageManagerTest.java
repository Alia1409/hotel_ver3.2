package test;

import model.Reservation;
import model.Room;
import persistence.StorageManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions; // Corrected standard Jupiter API import

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StorageManagerTest {

    private StorageManager storageManager;
    
    // Track file paths used by StorageManager to clean them up after tests
    private static final String TXT_FILE = "data_records.txt";
    private static final String JSON_FILE = "data_records.json";
    private static final String ROOM_TXT_FILE = "room_records.txt";
    private static final String ROOM_JSON_FILE = "room_records.json";

    @BeforeEach
    public void setUp() {
        storageManager = new StorageManager();
        // Clear any existing production files before running tests to ensure a clean state
        deleteTemporaryFiles();
    }

    @AfterEach
    public void tearDown() {
        // Remove files generated during the test lifecycle so they don't mess with real data
        deleteTemporaryFiles();
    }

    @Test
    public void testSaveAndLoadRooms() {
        // 1. Create a dummy list of rooms
        List<Room> testRooms = new ArrayList<>();
        testRooms.add(new Room("101", "Standard", 1, 85.50, "available", "Cozy single room"));
        testRooms.add(new Room("102", "Deluxe", 1, 150.00, "occupied", "Double bed with balcony"));

        // 2. Save using the storage manager
        storageManager.saveRooms(testRooms);

        // 3. Verify that both the TXT and JSON files were created on disk
        Assertions.assertTrue(new File(ROOM_TXT_FILE).exists(), "Room TXT file should be created.");
        Assertions.assertTrue(new File(ROOM_JSON_FILE).exists(), "Room JSON file should be created.");

        // 4. Load the rooms back from disk
        List<Room> loadedRooms = storageManager.loadRooms();

        // 5. Assertions to confirm the data matches exactly
        Assertions.assertEquals(2, loadedRooms.size(), "Should load exactly 2 rooms.");
        
        Room room1 = loadedRooms.get(0);
        Assertions.assertEquals("101", room1.getRoomNumber());
        Assertions.assertEquals("Standard", room1.getType());
        Assertions.assertEquals(1, room1.getFloor());
        Assertions.assertEquals(85.50, room1.getPricePerNight(), 0.001);
        Assertions.assertEquals("available", room1.getStatus());
        Assertions.assertEquals("Cozy single room", room1.getDescription());
    }

    @Test
    public void testSaveAndLoadReservations() {
        // 1. Create dummy reservation records
        List<Reservation> testReservations = new ArrayList<>();
        Reservation res1 = new Reservation(
                "RES-001", "101", "Alice Smith", "alice@example.com", "555-0192",
                LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 15), "confirmed", "Needs extra towels"
        );
        testReservations.add(res1);

        // 2. Save using the storage manager (which triggers both TXT and JSON writes)
        storageManager.saveReservations(testReservations);

        // 3. Verify that both expected reservation files exist on disk
        Assertions.assertTrue(new File(TXT_FILE).exists(), "Reservation TXT file should be created.");
        Assertions.assertTrue(new File(JSON_FILE).exists(), "Reservation JSON file should be created.");

        // 4. Load the data back in
        List<Reservation> loadedReservations = storageManager.loadReservations();

        // 5. Confirm structural data values match down to the attributes
        Assertions.assertEquals(1, loadedReservations.size(), "Should load exactly 1 reservation.");
        
        Reservation actual = loadedReservations.get(0);
        Assertions.assertEquals("RES-001", actual.getId());
        Assertions.assertEquals("101", actual.getRoomNumber());
        Assertions.assertEquals("Alice Smith", actual.getGuestName());
        Assertions.assertEquals("alice@example.com", actual.getEmail());
        Assertions.assertEquals("555-0192", actual.getPhone());
        Assertions.assertEquals(LocalDate.of(2026, 6, 10), actual.getCheckInDate());
        Assertions.assertEquals(LocalDate.of(2026, 6, 15), actual.getCheckOutDate());
        Assertions.assertEquals("confirmed", actual.getStatus());
        Assertions.assertEquals("Needs extra towels", actual.getSpecialNotes());
    }

    @Test
    public void testLoadReservationsWhenFileDoesNotExist() {
        // Ensure no file is present
        deleteTemporaryFiles();

        // Loading should gracefully return an empty list instead of throwing an Exception
        List<Reservation> loaded = storageManager.loadReservations();
        Assertions.assertNotNull(loaded, "Returned collection should never be null.");
        Assertions.assertTrue(loaded.isEmpty(), "Returned collection should be empty when no data file exists.");
    }

    private void deleteTemporaryFiles() {
        String[] files = {TXT_FILE, JSON_FILE, ROOM_TXT_FILE, ROOM_JSON_FILE};
        for (String path : files) {
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
        }
    }
}