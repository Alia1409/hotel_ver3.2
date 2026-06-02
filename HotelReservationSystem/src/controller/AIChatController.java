package controller;

import model.Room;
import service.RoomService;
import service.ReservationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIChatController {
    private final RoomService roomService;
    private final ReservationService reservationService;
    private static final java.time.format.DateTimeFormatter dateForm = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Guardrail topics to keep the AI focused on the hotel project scope
    private static final List<String> TOPIC_GUARDRAILS = Arrays.asList(
            "room", "reservation", "booking", "price", "rate", "check-in", "checkout",
            "stay", "occupancy", "cancel", "available", "free", "cost", "night"
    );

    public AIChatController(RoomService roomService, ReservationService reservationService) {
        this.roomService = roomService;
        this.reservationService = reservationService;
    }

    public String processQuery(String promptText) {
        if (promptText == null || promptText.trim().isEmpty()) {
            return "Assistant: Please enter a query.";
        }

        String query = promptText.toLowerCase().trim();
        boolean matchesTopic = TOPIC_GUARDRAILS.stream().anyMatch(query::contains);

        // Guardrail safety check
        if (!matchesTopic) {
            return "Assistant: I am configured to answer hotel-related queries only (e.g., room availability, booking policies, check-in statuses, or reservation pricing guidelines). Please try again with a related question.";
        }

        // 1. EXTRACT ROOM NUMBER (Look for digits in the prompt text, e.g., "101")
        String roomNumber = null;
        Pattern pattern = Pattern.compile("\\b\\d{3,4}\\b"); // Matches 3 or 4 digit numbers
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            roomNumber = matcher.group();
        }

        // 2. LOGIC: Check for specific room availability (e.g., "is room 101 available?")
        if (roomNumber != null && (query.contains("available") || query.contains("free") || query.contains("status"))) {
            Room room = roomService.getRoomByNumber(roomNumber);
            if (room == null) {
                return "Assistant: I couldn't find Room " + roomNumber + " in our hotel room inventory asset directory.";
            }
            
            // Check if it is available for today
            LocalDate today = LocalDate.now();
            // FIXED: Removed trailing ', null' parameter to match the 3-parameter signature
            boolean isAvailable = reservationService.isRoomAvailable(roomNumber, today, today.plusDays(1));
            
            if ("maintenance".equalsIgnoreCase(room.getStatus())) {
                return "Assistant: Room " + roomNumber + " is currently offline for maintenance.";
            } else if (isAvailable) {
                return "Assistant: Yes! Room " + roomNumber + " (" + room.getType() + ") is currently AVAILABLE for booking today at a rate of $" + room.getPricePerNight() + " per night.";
            } else {
                return "Assistant: No, Room " + roomNumber + " is currently BOOKED or OCCUPIED for today.";
            }
        }

        // 3. LOGIC: General list of available rooms (e.g., "what are the rooms that are available")
        if (query.contains("rooms") && (query.contains("available") || query.contains("free"))) {
            StringBuilder availableRoomsList = new StringBuilder("Assistant: Here are the rooms currently available for today:\n");
            int count = 0;
            LocalDate today = LocalDate.now();

            for (Room r : roomService.getAllRooms()) {
                // FIXED: Removed trailing ', null' parameter here as well
                boolean available = reservationService.isRoomAvailable(r.getRoomNumber(), today, today.plusDays(1));
                if (available && !"maintenance".equalsIgnoreCase(r.getStatus())) {
                    availableRoomsList.append("- Room ").append(r.getRoomNumber())
                                      .append(" (").append(r.getType()).append(") - $")
                                      .append(r.getPricePerNight()).append("/night\n");
                    count++;
                }
            }

            if (count == 0) {
                return "Assistant: I'm sorry, there are no rooms available in our real-time occupancy panel map today.";
            }
            return availableRoomsList.toString();
        }

        // 4. LOGIC: Room Count Summary
        if (query.contains("how many rooms") || (query.contains("room") && query.contains("count"))) {
            return "Assistant: There are currently " + roomService.getAllRooms().size() + " total rooms configured inside the hotel system data files.";
        }

        // Fallback default response for hotel topics that aren't specific actions
        return "Assistant: I recognized your hotel-related query. To help you better, you can ask things like:\n" +
               "- 'Is room 101 available?'\n" +
               "- 'What rooms are available?'\n" +
               "- 'How many rooms do we have?'";
    }
}