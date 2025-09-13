package advancedProject;

import java.util.ArrayList;
import java.util.Date;

class Booking {
    private int bookingId;
    private Customer customer;
    private Room room;
    private Date checkInDate;
    private Date checkOutDate;
    private boolean isCheckedIn;
    private boolean isCheckedOut;
    private ArrayList<AdditionalService> additionalServices;

    // Constructor
    public Booking(int bookingId, Customer customer, Room room, Date checkInDate, Date checkOutDate) {
        this.bookingId = bookingId;
        this.customer = customer;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.isCheckedIn = false;
        this.isCheckedOut = false;
        this.additionalServices = new ArrayList<>();

        // Book the room
        room.bookRoom();

        // Add this booking to the customer's bookings
        customer.addBooking(this);
    }

    // Check in
    public void checkIn() throws Exception {
        if (isCheckedIn) {
            throw new Exception("Already checked in!");
        }
        isCheckedIn = true;
    }

    // Check out
    public void checkOut() throws Exception {
        if (!isCheckedIn) {
            throw new Exception("Cannot check out without checking in first!");
        }
        if (isCheckedOut) {
            throw new Exception("Already checked out!");
        }
        isCheckedOut = true;
        room.releaseRoom();
    }

    // Calculate total days of stay
    public int calculateDaysOfStay() {
        long diffMillis = checkOutDate.getTime() - checkInDate.getTime();
        return (int) (diffMillis / (24 * 60 * 60 * 1000));
    }

    // Calculate room charge
    public double calculateRoomCharge() {
        return calculateDaysOfStay() * room.getRoomRate();
    }

    // Add additional service
    public void addAdditionalService(AdditionalService service) {
        additionalServices.add(service);
    }

    // Calculate total charge including additional services
    public double calculateTotalCharge() {
        double totalCharge = calculateRoomCharge();

        for (AdditionalService service : additionalServices) {
            totalCharge += service.getPrice();
        }

        return totalCharge;
    }

    // Generate bill
    public String generateBill() {
        String bill = "Booking ID: " + bookingId + "\n" +
                "Customer: " + customer.getName() + "\n" +
                "Room Number: " + room.getRoomNumber() + "\n" +
                "Check-in Date: " + checkInDate + "\n" +
                "Check-out Date: " + checkOutDate + "\n" +
                "Days of Stay: " + calculateDaysOfStay() + "\n" +
                "Room Charge: $" + calculateRoomCharge() + "\n";

        if (!additionalServices.isEmpty()) {
            bill += "\nAdditional Services:\n";
            for (int i = 0; i < additionalServices.size(); i++) {
                AdditionalService service = additionalServices.get(i);
                bill += "- " + service.getServiceName() + ": $" + service.getPrice() + "\n";
            }
        }

        bill += "\nTotal Charge: $" + calculateTotalCharge();

        return bill;
    }

    // Getters and Setters
    public int getBookingId() {
        return bookingId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Room getRoom() {
        return room;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public boolean isCheckedIn() {
        return isCheckedIn;
    }

    public boolean isCheckedOut() {
        return isCheckedOut;
    }

    public ArrayList<AdditionalService> getAdditionalServices() {
        return additionalServices;
    }
}
