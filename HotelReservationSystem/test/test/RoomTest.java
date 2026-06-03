package test;

import org.junit.jupiter.api.Test;

import model.Room;

import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class RoomTest {
    
    private Room room;
    
    @BeforeEach
    void setUp() {
        room = new Room("101", "single", 1, 85.0, "available", "Street view, single bed");
    }
    
    @Test
    void testRoomConstructor() {
        assertEquals("101", room.getRoomNumber());
        assertEquals("single", room.getType());
        assertEquals(1, room.getFloor());
        assertEquals(85.0, room.getPricePerNight());
        assertEquals("available", room.getStatus());
        assertEquals("Street view, single bed", room.getDescription());
    }
    
    @Test
    void testDefaultConstructor() {
        Room defaultRoom = new Room();
        assertNull(defaultRoom.getRoomNumber());
        assertNull(defaultRoom.getType());
        assertEquals(0, defaultRoom.getFloor());
        assertEquals(0.0, defaultRoom.getPricePerNight());
        assertNull(defaultRoom.getStatus());
        assertNull(defaultRoom.getDescription());
    }
    
    @Test
    void testSetRoomNumber() {
        room.setRoomNumber("202");
        assertEquals("202", room.getRoomNumber());
    }
    
    @Test
    void testSetType() {
        room.setType("double");
        assertEquals("double", room.getType());
    }
    
    @Test
    void testSetFloor() {
        room.setFloor(2);
        assertEquals(2, room.getFloor());
    }
    
    @Test
    void testSetPricePerNight() {
        room.setPricePerNight(120.0);
        assertEquals(120.0, room.getPricePerNight());
    }
    
    @Test
    void testSetStatus() {
        room.setStatus("occupied");
        assertEquals("occupied", room.getStatus());
    }
    
    @Test
    void testSetDescription() {
        room.setDescription("Ocean view, king bed");
        assertEquals("Ocean view, king bed", room.getDescription());
    }
    
    @Test
    void testPriceGreaterThanZero() {
        assertTrue(room.getPricePerNight() > 0, "Price per night must be greater than zero");
    }
    
    @Test
    void testRoomNumberNotNull() {
        assertNotNull(room.getRoomNumber(), "Room number must not be null");
    }
}