package test;

import model.Room;
import model.Reservation;
import persistence.StorageManager;
import service.RoomService;
import service.ReservationService;
import org.junit.jupiter.api.Test;

import controller.AIChatController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.time.LocalDate;

public class AIChatControllerTest {
    
    private AIChatController aiChatController;
    private RoomService roomService;
    private ReservationService reservationService;
    private StorageManager storageManager;
    
    // File names from StorageManager
    private static final String ROOM_TXT = "Room.txt";
    private static final String ROOM_JSON = "Room.json";
    private static final String RES_TXT = "Reservations.txt";
    private static final String RES_JSON = "Reservations.json";
    
    @BeforeEach
    void setUp() {
        // Clean up ALL files before each test
        deleteAllFiles();
        
        storageManager = new StorageManager();
        roomService = new RoomService(storageManager);
        reservationService = new ReservationService(roomService, storageManager);
        aiChatController = new AIChatController(roomService, reservationService);
        
        // Add test rooms
        roomService.addRoom(new Room("101", "single", 1, 85.0, "available", "Street view"));
        roomService.addRoom(new Room("102", "double", 1, 120.0, "available", "Garden view"));
        roomService.addRoom(new Room("103", "suite", 2, 200.0, "maintenance", "Ocean view"));
    }
    
    @AfterEach
    void tearDown() {
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
    void testEmptyQuery() {
        String response = aiChatController.processQuery("");
        assertEquals("Assistant: Please enter a query.", response);
    }
    
    @Test
    void testNullQuery() {
        String response = aiChatController.processQuery(null);
        assertEquals("Assistant: Please enter a query.", response);
    }
    
    @Test
    void testOutOfScopeQuery() {
        String response = aiChatController.processQuery("What is the weather today?");
        assertTrue(response.contains("I am configured to answer hotel-related queries only"),
            "Should reject non-hotel queries with guardrail message");
    }
    
    @Test
    void testRoomAvailabilityQuery() {
        String response = aiChatController.processQuery("Is room 101 available?");
        assertTrue(response.contains("Room 101"), "Should mention room number");
        assertTrue(response.contains("AVAILABLE") || response.contains("available"), 
            "Should indicate availability. Actual: " + response);
    }
    
    @Test
    void testMaintenanceRoomQuery() {
        String response = aiChatController.processQuery("Is room 103 available?");
        assertTrue(response.contains("maintenance"), "Should indicate maintenance status");
    }
    
    @Test
    void testNonExistingRoomQuery() {
        String response = aiChatController.processQuery("Is room 999 available?");
        assertTrue(response.contains("couldn't find"), "Should indicate room not found");
    }
    
    @Test
    void testListAvailableRoomsQuery() {
        String response = aiChatController.processQuery("What rooms are available?");
        assertTrue(response.contains("Room 101") || response.contains("101"), 
            "Should list room 101. Actual: " + response);
        assertTrue(response.contains("Room 102") || response.contains("102"), 
            "Should list room 102. Actual: " + response);
    }
    
    @Test
    void testRoomCountQuery() {
        String response = aiChatController.processQuery("How many rooms do we have?");
        assertTrue(response.contains("3"), 
            "Should report 3 rooms. Actual: " + response);
    }
    
    @Test
    void testCaseInsensitiveQuery() {
        String response = aiChatController.processQuery("IS ROOM 101 AVAILABLE?");
        assertTrue(response.contains("AVAILABLE") || response.contains("available"), 
            "Should handle uppercase. Actual: " + response);
    }
    
    @Test
    void testFallbackResponse() {
        String response = aiChatController.processQuery("hotel price");
        assertTrue(response.contains("recognized your hotel-related query"),
            "Should provide fallback for vague hotel queries");
    }
    
    @Test
    void testOccupiedRoomQuery() {
        // Create a reservation to make room 101 occupied
        Reservation res = new Reservation(
            "RES-001", "101", "John Doe", "john@test.com", "5550123",
            LocalDate.now(), LocalDate.now().plusDays(3),
            "confirmed", ""
        );
        reservationService.createReservation(res);
        
        String response = aiChatController.processQuery("Is room 101 available?");
        assertTrue(response.contains("BOOKED") || response.contains("OCCUPIED") || 
                   response.contains("booked") || response.contains("occupied"),
            "Should indicate room is occupied. Actual: " + response);
    }
}