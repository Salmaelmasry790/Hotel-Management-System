package advancedProject;

import java.util.ArrayList;
import java.util.*;

public class Hotel implements ISearchable {
    private String hotelName;
    private String address;
    private ArrayList<Room> rooms;
    private ArrayList<Customer> customers;
    private ArrayList<Booking> bookings;
    private ArrayList<AdditionalService> services;

    // Constructor
    public Hotel(String hotelName, String address) {
        this.hotelName = hotelName;
        this.address = address;
        this.rooms = new ArrayList<>();
        this.customers = new ArrayList<>();
        this.bookings = new ArrayList<>();
        this.services = new ArrayList<>();
    }

    // Add a room to the hotel
    public void addRoom(Room room) {
        rooms.add(room);
    }

    // Add a customer to the hotel
    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    // Create a new booking
    public Booking createBooking(Customer customer, Room room, Date checkInDate, Date checkOutDate) {
        if (!room.isAvailable()) {
            throw new IllegalStateException("Room is not available for booking!");
        }

        int newBookingId = bookings.size() + 1;
        Booking booking = new Booking(newBookingId, customer, room, checkInDate, checkOutDate);
        bookings.add(booking);
        return booking;
    }

    // Cancel a booking
    public void cancelBooking(int bookingId) throws Exception {
        Booking booking = searchBookingById(bookingId);
        if (booking == null) {
            throw new Exception("Booking not found!");
        }

        if (booking.isCheckedIn()) {
            throw new Exception("Cannot cancel booking after check-in!");
        }

        booking.getRoom().releaseRoom();
        bookings.remove(booking);
    }

    // Add a service to the hotel
    public void addService(AdditionalService service) {
        services.add(service);
    }

    // Implementation of ISearchable interface methods

    @Override
    public ArrayList<Room> searchAvailableRooms(String roomType, Date checkInDate, Date checkOutDate) {
        ArrayList<Room> availableRooms = new ArrayList<>();

        for (Room room : rooms) {
            // Check if room is available and matches the requested type
            if (room.isAvailable() && (roomType == null || room.getRoomType().equals(roomType))) {
                availableRooms.add(room);
            }
        }

        return availableRooms;
    }

    @Override
    public Customer searchCustomerById(int customerId) {
        for (Customer customer : customers) {
            if (customer.getId() == customerId) {
                return customer;
            }
        }
        return null;
    }

    @Override
    public Customer searchCustomerByPhone(String phoneNumber) {
        for (Customer customer : customers) {
            if (customer.getPhoneNumber().equals(phoneNumber)) {
                return customer;
            }
        }
        return null;
    }

    @Override
    public Booking searchBookingById(int bookingId) {
        for (Booking booking : bookings) {
            if (booking.getBookingId() == bookingId) {
                return booking;
            }
        }
        return null;
    }

    private <T> ArrayList<T> mergeSort(ArrayList<T> list, Comparator<T> comparator) {
        if (list.size() <= 1) {
            return list;
        }
    
        int mid = list.size() / 2;
    
        // Split the list into two halves
        ArrayList<T> left = new ArrayList<>(list.subList(0, mid));
        ArrayList<T> right = new ArrayList<>(list.subList(mid, list.size()));
    
        // Recursively sort both halves
        left = mergeSort(left, comparator);
        right = mergeSort(right, comparator);
    
        // Merge the sorted halves
        return merge(left, right, comparator);
    }
    
    private <T> ArrayList<T> merge(ArrayList<T> left, ArrayList<T> right, Comparator<T> comparator) {
        ArrayList<T> merged = new ArrayList<>();
        int i = 0, j = 0;
    
        // Merge elements from both lists in sorted order
        while (i < left.size() && j < right.size()) {
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                merged.add(left.get(i));
                i++;
            } else {
                merged.add(right.get(j));
                j++;
            }
        }
    
        // Add remaining elements from the left list
        while (i < left.size()) {
            merged.add(left.get(i));
            i++;
        }
    
        // Add remaining elements from the right list
        while (j < right.size()) {
            merged.add(right.get(j));
            j++;
        }
    
        return merged;
    }
    
    // Method to sort customers by name using the generic merge sort
    public void sortCustomersByName() {
        customers = mergeSort(customers, new Comparator<Customer>() {
            @Override
            public int compare(Customer c1, Customer c2) {
                return c1.getName().compareTo(c2.getName());
            }
        });
    }
    
    // Method to sort rooms by rate using the generic merge sort
    public void sortRoomsByRate() {
        rooms = mergeSort(rooms, new Comparator<Room>() {
            @Override
            public int compare(Room r1, Room r2) {
                return Double.compare(r1.getRoomRate(), r2.getRoomRate());
            }
        });
    }
    

    // Get all bookings for a specific customer
    public ArrayList<Booking> getBookingsForCustomer(int customerId) {
        ArrayList<Booking> customerBookings = new ArrayList<>();
        Customer customer = searchCustomerById(customerId);

        if (customer != null) {
            for (Booking booking : bookings) {
                if (booking.getCustomer().getId() == customerId) {
                    customerBookings.add(booking);
                }
            }
        }

        return customerBookings;
    }

    // Getters
    public String getHotelName() {
        return hotelName;
    }

    public String getAddress() {
        return address;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }


    public ArrayList<Booking> getBookings() {
        return bookings;
    }

    public ArrayList<AdditionalService> getServices() {
        return services;
    }
}
