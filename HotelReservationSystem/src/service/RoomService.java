package service;

import model.Room;
import persistence.StorageManager;
import java.util.List;

public class RoomService {
    private final StorageManager storageManager;
    private final List<Room> rooms;

    public RoomService(StorageManager storageManager) {
        this.storageManager = storageManager;

        this.rooms = storageManager.loadRooms(); 
    }

    public List<Room> getAllRooms() {
        return rooms;
    }

    public void addRoom(Room room) {
        rooms.add(room);

        storageManager.saveRooms(rooms); 
    }

    public void updateRoom(String roomNumber, Room updatedRoom) {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomNumber().equals(roomNumber)) {
                rooms.set(i, updatedRoom);
                break;
            }
        }

        storageManager.saveRooms(rooms); 
    }

    public void deleteRoom(String roomNumber) {
        rooms.removeIf(r -> r.getRoomNumber().equals(roomNumber));

        storageManager.saveRooms(rooms); 
    }

    public Room getRoomByNumber(String roomNumber) {
        return rooms.stream()
                .filter(r -> r.getRoomNumber().equals(roomNumber))
                .findFirst()
                .orElse(null);
    }
}