package persistence;

import model.Reservation;
import model.Room;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {
    private static final String TXT_FILE_PATH = "Reservations.txt";
    private static final String JSON_FILE_PATH = "Reservations.json";
    private static final String ROOM_FILE_PATH = "Room.txt";
    private static final String ROOM_JSON_FILE_PATH = "Room.json"; 

    public synchronized void saveReservations(List<Reservation> reservations) {
        // Save to your TXT log file
        saveToTextFile(reservations);

        // Save to the JSON file using native Java string building
        saveToJsonFile(reservations);
    }

    public synchronized List<Reservation> loadReservations() {
        List<Reservation> reservations = new ArrayList<>();
        File file = new File(TXT_FILE_PATH);
        
        if (!file.exists()) {
            return reservations; 
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split("\\|");
                if (parts.length >= 8) {
                    String id = parts[0];
                    String roomNum = parts[1];
                    String name = parts[2];
                    String email = parts[3];
                    String phone = parts[4];
                    LocalDate checkIn = LocalDate.parse(parts[5]);
                    LocalDate checkOut = LocalDate.parse(parts[6]);
                    String status = parts[7];
                    String notes = parts.length == 9 ? parts[8] : "";

                    Reservation r = new Reservation(id, roomNum, name, email, phone, checkIn, checkOut, status, notes);
                    reservations.add(r);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading reservation data: " + e.getMessage());
        }
        return reservations;
    }

    public synchronized List<Room> loadRooms() {
        List<Room> rooms = new ArrayList<>();
        File file = new File(ROOM_FILE_PATH);

        if (!file.exists()) {
            return rooms; 
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    String roomNumber = parts[0];
                    String type = parts[1];
                    int floor = Integer.parseInt(parts[2]);
                    double pricePerNight = Double.parseDouble(parts[3]);
                    String status = parts[4];
                    String description = parts.length == 6 ? parts[5] : "";

                    Room room = new Room(roomNumber, type, floor, pricePerNight, status, description);
                    rooms.add(room);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading room data inventory: " + e.getMessage());
        }
        return rooms;
    }

    public synchronized void saveRooms(List<Room> rooms) {
        // 1. Save to the traditional pipe-separated text file format
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(ROOM_FILE_PATH)))) {
            for (Room r : rooms) {
                pw.println(String.format("%s|%s|%d|%.2f|%s|%s",
                    r.getRoomNumber(),
                    r.getType(),
                    r.getFloor(),
                    r.getPricePerNight(),
                    r.getStatus(),
                    (r.getDescription() != null ? r.getDescription().replace("|", " ") : "")
                ));
            }
        } catch (IOException e) {
            System.err.println("Error saving room data inventory to TXT: " + e.getMessage());
        }

        // 2. Automatically save out to the room_records.json file layout simultaneously
        saveRoomsToJsonFile(rooms);
    }

    private void saveToTextFile(List<Reservation> reservations) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(TXT_FILE_PATH)))) {
            for (Reservation r : reservations) {
                pw.println(String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s",
                    r.getId(), 
                    r.getRoomNumber(), 
                    r.getGuestName(), 
                    r.getEmail(),
                    r.getPhone(), 
                    r.getCheckInDate(), 
                    r.getCheckOutDate(),
                    r.getStatus(), 
                    (r.getSpecialNotes() != null ? r.getSpecialNotes().replace("|", " ") : "")
                ));
            }
        } catch (IOException e) {
            System.err.println("Error saving to TXT: " + e.getMessage());
        }
    }

    private void saveToJsonFile(List<Reservation> reservations) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(JSON_FILE_PATH)))) {
            StringBuilder json = new StringBuilder();
            json.append("[\n"); 

            for (int i = 0; i < reservations.size(); i++) {
                Reservation r = reservations.get(i);
                
                String safeNotes = (r.getSpecialNotes() != null) ? r.getSpecialNotes().replace("\"", "\\\"").trim() : "";
                String safeName = (r.getGuestName() != null) ? r.getGuestName().replace("\"", "\\\"").trim() : "";
                String safeEmail = (r.getEmail() != null) ? r.getEmail().replace("\"", "\\\"").trim() : "";

                json.append("  {\n");
                json.append(String.format("    \"id\": \"%s\",\n", r.getId()));
                json.append(String.format("    \"roomNumber\": \"%s\",\n", r.getRoomNumber()));
                json.append(String.format("    \"guestName\": \"%s\",\n", safeName));
                json.append(String.format("    \"email\": \"%s\",\n", safeEmail));
                json.append(String.format("    \"phone\": \"%s\",\n", r.getPhone()));
                json.append(String.format("    \"checkInDate\": \"%s\",\n", r.getCheckInDate()));
                json.append(String.format("    \"checkOutDate\": \"%s\",\n", r.getCheckOutDate()));
                json.append(String.format("    \"status\": \"%s\",\n", r.getStatus()));
                json.append(String.format("    \"specialNotes\": \"%s\"\n", safeNotes));
                json.append("  }");

                if (i < reservations.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }

            json.append("]"); 
            pw.print(json.toString());
            
        } catch (IOException e) {
            System.err.println("Error saving to JSON: " + e.getMessage());
        }
    }

    // NEW METHOD: Pure Java JSON String-builder for your Room collection records
    private void saveRoomsToJsonFile(List<Room> rooms) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(ROOM_JSON_FILE_PATH)))) {
            StringBuilder json = new StringBuilder();
            json.append("[\n");

            for (int i = 0; i < rooms.size(); i++) {
                Room r = rooms.get(i);
                
                // Sanitize user text specifications to keep the generated JSON valid
                String safeType = (r.getType() != null) ? r.getType().replace("\"", "\\\"").trim() : "";
                String safeDesc = (r.getDescription() != null) ? r.getDescription().replace("\"", "\\\"").trim() : "";

                json.append("  {\n");
                json.append(String.format("    \"roomNumber\": \"%s\",\n", r.getRoomNumber()));
                json.append(String.format("    \"type\": \"%s\",\n", safeType));
                json.append(String.format("    \"floor\": %d,\n", r.getFloor()));
                json.append(String.format("    \"pricePerNight\": %.2f,\n", r.getPricePerNight()));
                json.append(String.format("    \"status\": \"%s\",\n", r.getStatus()));
                json.append(String.format("    \"description\": \"%s\"\n", safeDesc));
                json.append("  }");

                if (i < rooms.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }

            json.append("]");
            pw.print(json.toString());

        } catch (IOException e) {
            System.err.println("Error saving room data to JSON format: " + e.getMessage());
        }
    }
}