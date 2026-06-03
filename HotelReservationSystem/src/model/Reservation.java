package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reservation {
    private String id;
    private String roomNumber;
    private String guestName;
    private String email;
    private String phone;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String status; // confirmed, cancelled, in_progress, completed
    private String specialNotes;

    public Reservation() {}

    public Reservation(String id, String roomNumber, String guestName, String email, String phone, 
                       LocalDate checkInDate, LocalDate checkOutDate, String status, String specialNotes) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.guestName = guestName;
        this.email = email;
        this.phone = phone;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
        this.specialNotes = specialNotes;
    }

    public long getNumberOfNights() {
        if (checkInDate != null && checkOutDate != null && checkOutDate.isAfter(checkInDate)) {
            return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        }
        return 0;
    }

    public double getTotalAmount(double pricePerNight) {
        return getNumberOfNights() * pricePerNight;
    }

    public boolean overlapsWith(LocalDate start, LocalDate end) {
        if ("cancelled".equalsIgnoreCase(this.status) || "completed".equalsIgnoreCase(this.status)) {
            return false; // Cancelled/completed stays release room inventory allocations
        }
        return !(end.isBefore(this.checkInDate) || start.isAfter(this.checkOutDate) || 
                 end.equals(this.checkInDate) || start.equals(this.checkOutDate));
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSpecialNotes() { return specialNotes; }
    public void setSpecialNotes(String specialNotes) { this.specialNotes = specialNotes; }
}