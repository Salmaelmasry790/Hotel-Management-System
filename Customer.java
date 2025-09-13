package advancedProject;

import java.util.ArrayList;

class Customer extends Person {
    private String address;
    private String idProof;
    private ArrayList<Booking> bookings;

    // Constructor
    public Customer(int id, String name, String phoneNumber, String email,
            String address, String idProof) {
        super(id, name, phoneNumber, email);
        this.address = address;
        this.idProof = idProof;
        this.bookings = new ArrayList<>();
    }

    // Method Override example
    @Override
    public String getDetails() {
        return "Customer ID: " + id +
                "\nName: " + name +
                "\nPhone: " + phoneNumber +
                "\nEmail: " + email +
                "\nAddress: " + address;
    }

    // Add a booking to this customer
    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    // Get all bookings for this customer
    public ArrayList<Booking> getBookings() {
        return bookings;
    }

    // Method Overloading example
    public void updateContact(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void updateContact(String phoneNumber, String email) {
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdProof() {
        return idProof;
    }

    public void setIdProof(String idProof) {
        this.idProof = idProof;
    }
}