package service;

import model.Reservation;
import model.Room;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;

public class ExportService {

    public static void exportReservationsToCSV(List<Reservation> reservations, List<Room> rooms) {
        // Saves directly to your root project workspace folder silently!
        File file = new File("monthly_report.csv");

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("ID,RoomNumber,Guest,CheckIn,CheckOut,Nights,TotalAmount,Status,Special Notes");
            
            for (Reservation r : reservations) {
                Room room = rooms.stream()
                        .filter(rm -> rm.getRoomNumber().equals(r.getRoomNumber()))
                        .findFirst()
                        .orElse(null);
                double roomPrice = (room != null) ? room.getPricePerNight() : 0.0;

                long nights = java.time.temporal.ChronoUnit.DAYS.between(r.getCheckInDate(), r.getCheckOutDate());
                if (nights <= 0) nights = 1; 
                
                double totalAmount = r.getTotalAmount(roomPrice); 

                String notes = (r.getSpecialNotes() != null) ? r.getSpecialNotes().trim() : "";
                notes = notes.replace(",", " "); 

                pw.println(String.format("%s,%s,%s,%s,%s,%d,%.2f,%s,%s",
                    r.getId(), r.getRoomNumber(), r.getGuestName(),
                    r.getCheckInDate().toString(), r.getCheckOutDate().toString(), 
                    nights, totalAmount, r.getStatus(), notes
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}