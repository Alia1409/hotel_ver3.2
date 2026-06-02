package service;

import model.Room;
import persistence.StorageManager;
import java.util.List;

public class RoomService {
    private final StorageManager storageManager;
    private final List<Room> rooms;

    public RoomService(StorageManager storageManager) {
        this.storageManager = storageManager;
        // FIXED: Changed loadData() to loadRooms()
        this.rooms = storageManager.loadRooms(); 
    }

    public List<Room> getAllRooms() {
        return rooms;
    }

    public void addRoom(Room room) {
        rooms.add(room);
        // FIXED: Changed saveData() to saveRooms()
        storageManager.saveRooms(rooms); 
    }

    public void updateRoom(String roomNumber, Room updatedRoom) {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomNumber().equals(roomNumber)) {
                rooms.set(i, updatedRoom);
                break;
            }
        }
        // FIXED: Changed saveData() to saveRooms()
        storageManager.saveRooms(rooms); 
    }

    public void deleteRoom(String roomNumber) {
        rooms.removeIf(r -> r.getRoomNumber().equals(roomNumber));
        // FIXED: Changed saveData() to saveRooms()
        storageManager.saveRooms(rooms); 
    }

    public Room getRoomByNumber(String roomNumber) {
        return rooms.stream()
                .filter(r -> r.getRoomNumber().equals(roomNumber))
                .findFirst()
                .orElse(null);
    }
}