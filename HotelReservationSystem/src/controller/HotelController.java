package controller;

import model.Reservation;
import model.Room;
import persistence.DataStore; // Uses your text file data store instead of Jackson JSON
import service.ExportService;

import java.time.LocalDate;
import java.util.List;

public class HotelController {
    private final List<Room> rooms;
    private final List<Reservation> reservations;

    public HotelController() {
        // Automatically loads rooms from rooms.txt on startup
        this.rooms = DataStore.loadRooms();
        
        // Pass a temporary/null RoomService if your DataStore loadReservations requires it, 
        // or let it read the text directly.
        this.reservations = DataStore.loadReservations(null); 
    }

    public List<Room> getAllRooms() { return rooms; }
    public List<Reservation> getAllReservations() { return reservations; }

    public Room getRoomByNumber(String num) {
        return rooms.stream().filter(r -> r.getRoomNumber().equals(num)).findFirst().orElse(null);
    }

    public boolean isRoomAvailable(String roomNum, LocalDate checkIn, LocalDate checkOut, String excludeResId) {
        return reservations.stream()
                .filter(res -> res.getRoomNumber().equals(roomNum))
                .filter(res -> excludeResId == null || !res.getId().equals(excludeResId))
                .noneMatch(res -> res.overlapsWith(checkIn, checkOut));
    }

    public void addRoom(Room r) {
        rooms.add(r);
        DataStore.saveRooms(rooms); // Saves to text file automatically
    }

    public void updateRoom(String oldNum, Room updated) {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomNumber().equals(oldNum)) {
                rooms.set(i, updated);
                break;
            }
        }
        DataStore.saveRooms(rooms); // Saves to text file automatically
    }

    public void deleteRoom(String roomNumber) {
        rooms.removeIf(r -> r.getRoomNumber().equals(roomNumber));
        DataStore.saveRooms(rooms); // Saves to text file automatically
    }

    public boolean createReservation(Reservation res) {
        if (!isRoomAvailable(res.getRoomNumber(), res.getCheckInDate(), res.getCheckOutDate(), null)) {
            return false;
        }
        reservations.add(res);
        DataStore.saveReservations(reservations); // Saves to text file automatically
        return true;
    }

    public boolean updateReservation(Reservation res) {
        if (!isRoomAvailable(res.getRoomNumber(), res.getCheckInDate(), res.getCheckOutDate(), res.getId())) {
            return false;
        }
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getId().equals(res.getId())) {
                reservations.set(i, res);
                break;
            }
        }
        DataStore.saveReservations(reservations); // Saves to text file automatically
        return true;
    }

    public void deleteReservation(String id) {
        reservations.removeIf(r -> r.getId().equals(id));
        DataStore.saveReservations(reservations); // Saves to text file automatically
    }

    public void exportToCSV() {
        ExportService.exportReservationsToCSV(reservations, rooms); 
    }
}