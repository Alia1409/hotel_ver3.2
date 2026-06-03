package persistence;

import model.Room;
import model.Reservation;
import service.RoomService;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static final String ROOMS_FILE = "rooms.txt";
    private static final String RESERVATIONS_FILE = "reservations.txt";

    // ROOM FILE HANDLING
    public static void saveRooms(List<Room> rooms) {
        try (PrintWriter out = new PrintWriter(new FileWriter(ROOMS_FILE))) {
            for (Room r : rooms) {
                out.println(r.getRoomNumber() + "," + r.getType() + "," + r.getFloor() + "," + 
                            r.getPricePerNight() + "," + r.getStatus() + "," + r.getDescription());
            }
        } catch (IOException e) {
            System.out.println("Error writing rooms data: " + e.getMessage());
        }
    }

    public static List<Room> loadRooms() {
        List<Room> loadedRooms = new ArrayList<>();
        File file = new File(ROOMS_FILE);
        if (!file.exists()) return loadedRooms;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] t = line.split(",");
                if (t.length >= 6) {
                    loadedRooms.add(new Room(t[0], t[1], Integer.parseInt(t[2]), 
                                   Double.parseDouble(t[3]), t[4], t[5]));
                }
            }
        } catch (Exception e) {
            System.out.println("Notice: Room file parser hit an anomaly. Resetting file registry.");
        }
        return loadedRooms;
    }

    // RESERVATION FILE HANDLING 
    public static void saveReservations(List<Reservation> reservations) {
        try (PrintWriter out = new PrintWriter(new FileWriter(RESERVATIONS_FILE))) {
            for (Reservation res : reservations) {
                out.println(res.getId() + "," + res.getRoomNumber() + "," + res.getGuestName() + "," + 
                            res.getEmail() + "," + res.getPhone() + "," + res.getCheckInDate() + "," + 
                            res.getCheckOutDate() + "," + res.getStatus() + "," + res.getSpecialNotes());
            }
        } catch (IOException e) {
            System.out.println("Error writing reservations data: " + e.getMessage());
        }
    }

    public static List<Reservation> loadReservations(RoomService roomService) {
        List<Reservation> list = new ArrayList<>();
        File file = new File(RESERVATIONS_FILE);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] t = line.split(",");
                if (t.length >= 9) {
                    Room r = roomService.getRoomByNumber(t[1]); 
                    
                    list.add(new Reservation(
                             t[0], 
                             t[1], 
                             t[2], 
                             t[3], 
                             t[4], 
                             LocalDate.parse(t[5]), 
                             LocalDate.parse(t[6]), 
                             t[7], 
                             t[8]
                    ));
                }
            }
        } catch (Exception e) { // The catch block is now structurally sound
            System.out.println("Notice: Reservation file parser hit an anomaly.");
        }
        return list;
    }
}