package test;

import model.Room;
import persistence.StorageManager;
import service.RoomService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.util.List;

public class RoomServiceTest {
    
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
    void testAddRoom() {
        Room room = new Room("101", "single", 1, 85.0, "available", "Test room");
        roomService.addRoom(room);
        
        List<Room> allRooms = roomService.getAllRooms();
        assertEquals(1, allRooms.size(), "Should have one room after adding");
        assertEquals("101", allRooms.get(0).getRoomNumber());
    }
    
    @Test
    void testGetAllRooms() {
        Room room1 = new Room("101", "single", 1, 85.0, "available", "Room 1");
        Room room2 = new Room("102", "double", 1, 120.0, "available", "Room 2");
        roomService.addRoom(room1);
        roomService.addRoom(room2);
        
        List<Room> allRooms = roomService.getAllRooms();
        assertEquals(2, allRooms.size(), "Should return all rooms");
    }
    
    @Test
    void testGetRoomByNumberExisting() {
        Room room = new Room("101", "single", 1, 85.0, "available", "Test room");
        roomService.addRoom(room);
        
        Room found = roomService.getRoomByNumber("101");
        assertNotNull(found, "Should find existing room");
        assertEquals("single", found.getType());
        assertEquals(85.0, found.getPricePerNight());
    }
    
    @Test
    void testGetRoomByNumberNonExisting() {
        Room found = roomService.getRoomByNumber("999");
        assertNull(found, "Should return null for non-existing room");
    }
    
    @Test
    void testUpdateRoom() {
        Room room = new Room("101", "single", 1, 85.0, "available", "Original");
        roomService.addRoom(room);
        
        Room updated = new Room("101", "double", 2, 120.0, "occupied", "Updated");
        roomService.updateRoom("101", updated);
        
        Room result = roomService.getRoomByNumber("101");
        assertEquals("double", result.getType());
        assertEquals(2, result.getFloor());
        assertEquals(120.0, result.getPricePerNight());
        assertEquals("occupied", result.getStatus());
        assertEquals("Updated", result.getDescription());
    }
    
    @Test
    void testDeleteRoom() {
        Room room = new Room("101", "single", 1, 85.0, "available", "Test room");
        roomService.addRoom(room);
        
        roomService.deleteRoom("101");
        
        assertEquals(0, roomService.getAllRooms().size(), "Room list should be empty after deletion");
        assertNull(roomService.getRoomByNumber("101"), "Deleted room should not be found");
    }
    
    @Test
    void testDeleteRoomMultipleRooms() {
        Room room1 = new Room("101", "single", 1, 85.0, "available", "Room 1");
        Room room2 = new Room("102", "double", 1, 120.0, "available", "Room 2");
        roomService.addRoom(room1);
        roomService.addRoom(room2);
        
        roomService.deleteRoom("101");
        
        List<Room> remaining = roomService.getAllRooms();
        assertEquals(1, remaining.size(), "Should have one room remaining");
        assertEquals("102", remaining.get(0).getRoomNumber());
    }
    
    @Test
    void testRoomPriceGreaterThanZero() {
        Room room = new Room("101", "single", 1, 85.0, "available", "Test");
        roomService.addRoom(room);
        
        Room result = roomService.getRoomByNumber("101");
        assertTrue(result.getPricePerNight() > 0, "Room price must be greater than zero");
    }
    
    @Test
    void testEmptyRoomList() {
        List<Room> allRooms = roomService.getAllRooms();
        assertNotNull(allRooms, "Should return empty list, not null");
        assertEquals(0, allRooms.size(), "Should have no rooms initially");
    }
}