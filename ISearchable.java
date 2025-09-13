package advancedProject;

import java.util.ArrayList;
import java.util.Date;

public interface ISearchable {
    ArrayList<Room> searchAvailableRooms(String roomType, Date checkInDate, Date checkOutDate);

    Customer searchCustomerById(int customerId);

    Customer searchCustomerByPhone(String phoneNumber);

    Booking searchBookingById(int bookingId);
}