package service;

import model.Room;
import model.Reservation;
import persistence.StorageManager;

import java.time.LocalDate;
import java.util.List;

public class ReservationService {
    private final RoomService roomService;
    private final StorageManager storageManager;
    private final List<Reservation> reservations;

    public ReservationService(RoomService roomService, StorageManager storageManager) {
        this.roomService = roomService;
        this.storageManager = storageManager;
        this.reservations = storageManager.loadReservations(); 
    }

    public List<Reservation> getAllReservations() {
        return reservations;
    }

    public boolean createReservation(Reservation r) {
        
        reservations.add(r);
        // FIXED: Changed saveData() to saveReservations()
        storageManager.saveReservations(reservations); 
        return true;
    }

    public boolean updateReservation(Reservation r) {
        // (Your existing validation logic stays here...)

        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getId().equals(r.getId())) {
                reservations.set(i, r);
                break;
            }
        }
        // FIXED: Changed saveData() to saveReservations()
        storageManager.saveReservations(reservations); 
        return true;
    }

    public void deleteReservation(String id) {
        reservations.removeIf(r -> r.getId().equals(id));
        // FIXED: Changed saveData() to saveReservations()
        storageManager.saveReservations(reservations); 
    }
    public void processCheckIn(String id) {
        // FIXED: Explicitly added the correct stream assignment wrapper
        Reservation res = reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (res != null) {
            res.setStatus("in-progress");
            // FIXED: Changed r.getRoomNumber() to res.getRoomNumber()
            Room room = roomService.getRoomByNumber(res.getRoomNumber());
            if (room != null) {
                room.setStatus("occupied");
            }
            storageManager.saveRooms(roomService.getAllRooms());
            storageManager.saveReservations(reservations);
        }
    }

    public void processCheckOut(String id) {
        Reservation res = reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (res != null) {
            res.setStatus("completed");
            // FIXED: Changed r.getRoomNumber() to res.getRoomNumber()
            Room room = roomService.getRoomByNumber(res.getRoomNumber());
            if (room != null) {
                room.setStatus("available");
            }
            storageManager.saveRooms(roomService.getAllRooms());
            storageManager.saveReservations(reservations);
        }
    }
    public boolean isRoomAvailable(String roomNumber, LocalDate checkIn, LocalDate checkOut) {
        for (Reservation res : reservations) {
            // Skip reservations that are already cancelled or completed
            if (res.getStatus().equalsIgnoreCase("cancelled") || res.getStatus().equalsIgnoreCase("completed")) {
                continue;
            }
            
            // Check if the reservation matches the target room number
            if (res.getRoomNumber().equals(roomNumber)) {
                // Booking overlap conflict logic
                if (checkIn.isBefore(res.getCheckOutDate()) && checkOut.isAfter(res.getCheckInDate())) {
                    return false; // The room is taken during these dates!
                }
            }
        }
        return true; // No overlaps found, the room is free!
    }    
}