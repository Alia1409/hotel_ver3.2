package test;

import model.Reservation;
import model.Room;
import persistence.StorageManager;
import service.ReservationService;
import service.RoomService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class ReservationServiceTest {
    
    private ReservationService reservationService;
    private RoomService roomService;
    private StorageManager storageManager;
    
    // Your actual file names from StorageManager
    private static final String ROOM_TXT = "Room.txt";
    private static final String ROOM_JSON = "Room.json";
    private static final String RES_TXT = "Reservations.txt";
    private static final String RES_JSON = "Reservations.json";
    
    @BeforeEach
    void setUp() {
        // Delete ALL files before creating services
        deleteAllFiles();
        
        storageManager = new StorageManager();
        roomService = new RoomService(storageManager);
        reservationService = new ReservationService(roomService, storageManager);
        
        // Add a test room
        Room room = new Room("101", "single", 1, 85.0, "available", "Test room");
        roomService.addRoom(room);
    }
    
    @AfterEach
    void tearDown() {
        // Delete ALL files after test
        deleteAllFiles();
    }
    
    private void deleteAllFiles() {
        deleteFile(ROOM_TXT);
        deleteFile(ROOM_JSON);
        deleteFile(RES_TXT);
        deleteFile(RES_JSON);
    }
    
    private void deleteFile(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }
    
    @Test
    void testCreateReservation() {
        Reservation res = new Reservation(
            "RES-001", "101", "John Doe", "john@test.com", "5550123",
            LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 18),
            "confirmed", ""
        );
        
        boolean result = reservationService.createReservation(res);
        assertTrue(result, "Reservation should be created successfully");
        
        List<Reservation> allReservations = reservationService.getAllReservations();
        assertEquals(1, allReservations.size(), "Should have one reservation");
        assertEquals("RES-001", allReservations.get(0).getId());
    }
    
    @Test
    void testIsRoomAvailableNoReservations() {
        boolean available = reservationService.isRoomAvailable(
            "101", LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 18)
        );
        assertTrue(available, "Room should be available when no reservations exist");
    }
    
    @Test
    void testIsRoomAvailableWithReservation() {
        Reservation res = new Reservation(
            "RES-001", "101", "John Doe", "john@test.com", "5550123",
            LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 18),
            "confirmed", ""
        );
        reservationService.createReservation(res);
        
        boolean available = reservationService.isRoomAvailable(
            "101", LocalDate.of(2026, 5, 16), LocalDate.of(2026, 5, 20)
        );
        assertFalse(available, "Room should not be available for overlapping dates");
    }
    
    @Test
    void testIsRoomAvailableNonOverlappingDates() {
        Reservation res = new Reservation(
            "RES-001", "101", "John Doe", "john@test.com", "5550123",
            LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 18),
            "confirmed", ""
        );
        reservationService.createReservation(res);
        
        boolean available = reservationService.isRoomAvailable(
            "101", LocalDate.of(2026, 5, 20), LocalDate.of(2026, 5, 25)
        );
        assertTrue(available, "Room should be available for non-overlapping dates");
    }
    
    @Test
    void testIsRoomAvailableCancelledReservation() {
        Reservation res = new Reservation(
            "RES-001", "101", "John Doe", "john@test.com", "5550123",
            LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 18),
            "cancelled", ""
        );
        reservationService.createReservation(res);
        
        boolean available = reservationService.isRoomAvailable(
            "101", LocalDate.of(2026, 5, 16), LocalDate.of(2026, 5, 17)
        );
        assertTrue(available, "Room should be available when reservation is cancelled");
    }
    
    @Test
    void testIsRoomAvailableCompletedReservation() {
        Reservation res = new Reservation(
            "RES-001", "101", "John Doe", "john@test.com", "5550123",
            LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 18),
            "completed", ""
        );
        reservationService.createReservation(res);
        
        boolean available = reservationService.isRoomAvailable(
            "101", LocalDate.of(2026, 5, 16), LocalDate.of(2026, 5, 17)
        );
        assertTrue(available, "Room should be available when reservation is completed");
    }
    
    @Test
    void testProcessCheckIn() {
        Reservation res = new Reservation(
            "RES-001", "101", "John Doe", "john@test.com", "5550123",
            LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 18),
            "confirmed", ""
        );
        reservationService.createReservation(res);
        
        reservationService.processCheckIn("RES-001");
        
        Reservation updatedRes = reservationService.getAllReservations().get(0);
        assertEquals("in-progress", updatedRes.getStatus(), "Status should be in-progress after check-in");
        
        Room updatedRoom = roomService.getRoomByNumber("101");
        assertEquals("occupied", updatedRoom.getStatus(), "Room should be occupied after check-in");
    }
    
    @Test
    void testProcessCheckOut() {
        Reservation res = new Reservation(
            "RES-001", "101", "John Doe", "john@test.com", "5550123",
            LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 18),
            "in-progress", ""
        );
        reservationService.createReservation(res);
        roomService.getRoomByNumber("101").setStatus("occupied");
        
        reservationService.processCheckOut("RES-001");
        
        Reservation updatedRes = reservationService.getAllReservations().get(0);
        assertEquals("completed", updatedRes.getStatus(), "Status should be completed after check-out");
        
        Room updatedRoom = roomService.getRoomByNumber("101");
        assertEquals("available", updatedRoom.getStatus(), "Room should be available after check-out");
    }
    
    @Test
    void testUpdateReservation() {
        Reservation res = new Reservation(
            "RES-001", "101", "John Doe", "john@test.com", "5550123",
            LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 18),
            "confirmed", ""
        );
        reservationService.createReservation(res);
        
        Reservation updatedRes = new Reservation(
            "RES-001", "101", "Jane Smith", "jane@test.com", "999888777",
            LocalDate.of(2026, 5, 20), LocalDate.of(2026, 5, 25),
            "confirmed", "Updated notes"
        );
        reservationService.updateReservation(updatedRes);
        
        Reservation result = reservationService.getAllReservations().get(0);
        assertEquals("Jane Smith", result.getGuestName());
        assertEquals("jane@test.com", result.getEmail());
        assertEquals("Updated notes", result.getSpecialNotes());
    }
    
    @Test
    void testDeleteReservation() {
        Reservation res = new Reservation(
            "RES-001", "101", "John Doe", "john@test.com", "5550123",
            LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 18),
            "confirmed", ""
        );
        reservationService.createReservation(res);
        
        reservationService.deleteReservation("RES-001");
        
        assertEquals(0, reservationService.getAllReservations().size(), 
            "Reservation list should be empty after deletion");
    }
    
    @Test
    void testGetAllReservations() {
        Reservation res1 = new Reservation(
            "RES-001", "101", "John Doe", "john@test.com", "5550123",
            LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 18),
            "confirmed", ""
        );
        Reservation res2 = new Reservation(
            "RES-002", "101", "Jane Smith", "jane@test.com", "999888777",
            LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5),
            "confirmed", ""
        );
        reservationService.createReservation(res1);
        reservationService.createReservation(res2);
        
        List<Reservation> allReservations = reservationService.getAllReservations();
        assertEquals(2, allReservations.size(), "Should return all reservations");
    }
}