package advancedProject;

import java.util.ArrayList;

class Room {
    private int roomNumber;
    private String roomType; // e.g., Single, Double, Suite
    private double roomRate;
    private boolean isAvailable;
    private ArrayList<String> amenities;

    // Constructor
    public Room(int roomNumber, String roomType, double roomRate, boolean isAvailable) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomRate = roomRate;
        this.isAvailable = true;
        this.amenities = new ArrayList<>();
    }


    // Add amenity to the room
    public void addAmenity(String amenity) {
        amenities.add(amenity);
    }

    // Get all amenities in the room
    public ArrayList<String> getAmenities() {
        return amenities;
    }

    // Book a room (set availability to false)
    public void bookRoom() {
        if (isAvailable) {
            isAvailable = false;
        } else {
            throw new IllegalStateException("Room is already booked!");
        }
    }

    // Release a room (set availability to true)
    public void releaseRoom() {
        isAvailable = true;
    }

    // Getters and Setters
    public int getRoomNumber() {
        return roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public double getRoomRate() {
        return roomRate;
    }

    public void setRoomRate(double roomRate) {
        this.roomRate = roomRate;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
