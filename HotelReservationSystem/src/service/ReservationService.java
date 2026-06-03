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
        // 1. Check if the room is actually available for these dates
        if (!isRoomAvailable(r.getRoomNumber(), r.getCheckInDate(), r.getCheckOutDate())) {
            return false; // Reject the booking!
        }
        
        reservations.add(r);
        storageManager.saveReservations(reservations); 
        return true;
    }

    public boolean updateReservation(Reservation r) {
        // 1. Check if the room is available (excluding this specific reservation ID)
        if (!isRoomAvailableForUpdate(r.getId(), r.getRoomNumber(), r.getCheckInDate(), r.getCheckOutDate())) {
            return false; // Reject the update!
        }

        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getId().equals(r.getId())) {
                reservations.set(i, r);
                break;
            }
        }
        storageManager.saveReservations(reservations); 
        return true;
    }

    public void deleteReservation(String id) {
        reservations.removeIf(r -> r.getId().equals(id));
        storageManager.saveReservations(reservations); 
    }

    public void processCheckIn(String id) {
        Reservation res = reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (res != null) {
            res.setStatus("in-progress");
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
            // Skip cancelled or completed bookings
            if (res.getStatus().equalsIgnoreCase("cancelled") || res.getStatus().equalsIgnoreCase("completed")) {
                continue;
            }
            
            // If it matches the same room, verify the date boundaries don't overlap
            if (res.getRoomNumber().equals(roomNumber)) {
                if (checkIn.isBefore(res.getCheckOutDate()) && checkOut.isAfter(res.getCheckInDate())) {
                    return false; // Overlap detected!
                }
            }
        }
        return true; // Safe to book
    }

    // Helper method to allow updating a reservation without it conflicting with its own old dates
    private boolean isRoomAvailableForUpdate(String currentResId, String roomNumber, LocalDate checkIn, LocalDate checkOut) {
        for (Reservation res : reservations) {
            if (res.getId().equals(currentResId)) {
                continue; // Ignore checking against itself
            }
            if (res.getStatus().equalsIgnoreCase("cancelled") || res.getStatus().equalsIgnoreCase("completed")) {
                continue;
            }
            if (res.getRoomNumber().equals(roomNumber)) {
                if (checkIn.isBefore(res.getCheckOutDate()) && checkOut.isAfter(res.getCheckInDate())) {
                    return false; 
                }
            }
        }
        return true;
    }
}
