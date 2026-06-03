package test;

import org.junit.jupiter.api.Test;

import model.Reservation;

import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class ReservationTest {
    
    private Reservation reservation;
    private LocalDate checkIn;
    private LocalDate checkOut;
    
    @BeforeEach
    void setUp() {
        checkIn = LocalDate.of(2026, 5, 15);
        checkOut = LocalDate.of(2026, 5, 18);
        reservation = new Reservation(
            "RES-001", "101", "John Doe", "john.doe@email.com", "5550123",
            checkIn, checkOut, "confirmed", "Late arrival expected"
        );
    }
    
    @Test
    void testReservationConstructor() {
        assertEquals("RES-001", reservation.getId());
        assertEquals("101", reservation.getRoomNumber());
        assertEquals("John Doe", reservation.getGuestName());
        assertEquals("john.doe@email.com", reservation.getEmail());
        assertEquals("5550123", reservation.getPhone());
        assertEquals(checkIn, reservation.getCheckInDate());
        assertEquals(checkOut, reservation.getCheckOutDate());
        assertEquals("confirmed", reservation.getStatus());
        assertEquals("Late arrival expected", reservation.getSpecialNotes());
    }
    
    @Test
    void testDefaultConstructor() {
        Reservation defaultRes = new Reservation();
        assertNull(defaultRes.getId());
        assertNull(defaultRes.getRoomNumber());
        assertNull(defaultRes.getGuestName());
        assertNull(defaultRes.getEmail());
        assertNull(defaultRes.getPhone());
        assertNull(defaultRes.getCheckInDate());
        assertNull(defaultRes.getCheckOutDate());
        assertNull(defaultRes.getStatus());
        assertNull(defaultRes.getSpecialNotes());
    }
    
    @Test
    void testGetNumberOfNights() {
        long nights = reservation.getNumberOfNights();
        assertEquals(3, nights, "Should calculate 3 nights between May 15 and May 18");
    }
    
    @Test
    void testGetNumberOfNightsInvalidRange() {
        Reservation invalidRes = new Reservation(
            "RES-002", "101", "Test", "test@test.com", "1234567",
            LocalDate.of(2026, 5, 18), LocalDate.of(2026, 5, 15),
            "confirmed", ""
        );
        assertEquals(0, invalidRes.getNumberOfNights(), "Should return 0 for invalid date range");
    }
    
    @Test
    void testGetNumberOfNightsNullDates() {
        Reservation nullDatesRes = new Reservation();
        assertEquals(0, nullDatesRes.getNumberOfNights(), "Should return 0 for null dates");
    }
    
    @Test
    void testGetTotalAmount() {
        double total = reservation.getTotalAmount(100.0);
        assertEquals(300.0, total, "3 nights at $100/night should equal $300");
    }
    
    @Test
    void testGetTotalAmountZeroNights() {
        Reservation invalidRes = new Reservation(
            "RES-003", "101", "Test", "test@test.com", "1234567",
            LocalDate.of(2026, 5, 18), LocalDate.of(2026, 5, 15),
            "confirmed", ""
        );
        assertEquals(0.0, invalidRes.getTotalAmount(100.0), "Should return 0 for invalid date range");
    }
    
    @Test
    void testOverlapsWithOverlappingDates() {
        LocalDate start = LocalDate.of(2026, 5, 16);
        LocalDate end = LocalDate.of(2026, 5, 20);
        assertTrue(reservation.overlapsWith(start, end), "Should detect overlapping dates");
    }
    
    @Test
    void testOverlapsWithNonOverlappingDates() {
        LocalDate start = LocalDate.of(2026, 5, 20);
        LocalDate end = LocalDate.of(2026, 5, 25);
        assertFalse(reservation.overlapsWith(start, end), "Should not detect overlap for non-overlapping dates");
    }
    
    @Test
    void testOverlapsWithCancelledReservation() {
        Reservation cancelledRes = new Reservation(
            "RES-004", "101", "Test", "test@test.com", "1234567",
            checkIn, checkOut, "cancelled", ""
        );
        LocalDate start = LocalDate.of(2026, 5, 16);
        LocalDate end = LocalDate.of(2026, 5, 20);
        assertFalse(cancelledRes.overlapsWith(start, end), "Cancelled reservation should not overlap");
    }
    
    @Test
    void testOverlapsWithCompletedReservation() {
        Reservation completedRes = new Reservation(
            "RES-005", "101", "Test", "test@test.com", "1234567",
            checkIn, checkOut, "completed", ""
        );
        LocalDate start = LocalDate.of(2026, 5, 16);
        LocalDate end = LocalDate.of(2026, 5, 20);
        assertFalse(completedRes.overlapsWith(start, end), "Completed reservation should not overlap");
    }
    
    @Test
    void testOverlapsWithAdjacentDates() {
        LocalDate start = LocalDate.of(2026, 5, 10);
        LocalDate end = LocalDate.of(2026, 5, 15);
        assertFalse(reservation.overlapsWith(start, end), "Adjacent dates should not overlap");
    }
    
    @Test
    void testSetters() {
        reservation.setId("RES-999");
        reservation.setRoomNumber("202");
        reservation.setGuestName("Jane Smith");
        reservation.setEmail("jane@test.com");
        reservation.setPhone("999888777");
        reservation.setCheckInDate(LocalDate.of(2026, 6, 1));
        reservation.setCheckOutDate(LocalDate.of(2026, 6, 5));
        reservation.setStatus("in-progress");
        reservation.setSpecialNotes("Early check-in");
        
        assertEquals("RES-999", reservation.getId());
        assertEquals("202", reservation.getRoomNumber());
        assertEquals("Jane Smith", reservation.getGuestName());
        assertEquals("jane@test.com", reservation.getEmail());
        assertEquals("999888777", reservation.getPhone());
        assertEquals(LocalDate.of(2026, 6, 1), reservation.getCheckInDate());
        assertEquals(LocalDate.of(2026, 6, 5), reservation.getCheckOutDate());
        assertEquals("in-progress", reservation.getStatus());
        assertEquals("Early check-in", reservation.getSpecialNotes());
    }
    
    @Test
    void testCheckOutAfterCheckIn() {
        assertTrue(reservation.getCheckOutDate().isAfter(reservation.getCheckInDate()),
            "Check-out date must be after check-in date");
    }
    
    @Test
    void testValidEmailFormat() {
        String email = reservation.getEmail();
        assertTrue(email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"),
            "Email should match RFC 5322 format");
    }
    
    @Test
    void testValidPhoneFormat() {
        String phone = reservation.getPhone();
        assertTrue(phone.matches("\\d{7,15}"), "Phone should contain 7-15 digits");
    }
}