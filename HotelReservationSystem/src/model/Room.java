package model;

public class Room {
    private String roomNumber;
    private String type; // single, double, superior double, suite
    private int floor;
    private double pricePerNight;
    private String status; // available, occupied, maintenance
    private String description;

    public Room() {}

    public Room(String roomNumber, String type, int floor, double pricePerNight, String status, String description) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.floor = floor;
        this.pricePerNight = pricePerNight;
        this.status = status;
        this.description = description;
    }

    // Getters and Setters
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}