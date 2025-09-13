package advancedProject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.*;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HotelManagementSystem extends Application {

    private Hotel hotel;

    @Override
    public void start(Stage primaryStage) {
        // Initialize hotel
        hotel = new Hotel("Grand Hotel", "123 Main Street");
        initializeHotelData(); // Add sample data

        // Create the login scene
        Scene loginScene = createLoginScene(primaryStage);
        loginScene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        // Set the login scene as the initial scene
        primaryStage.setTitle("Hotel Management System - Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private Scene createLoginScene(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            try (Connection conn = DB.DBConnection();
                    PreparedStatement pst = conn.prepareStatement(
                            "SELECT * FROM Users WHERE username = ? AND password = ?")) {
                pst.setString(1, username);
                pst.setString(2, password);

                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    // Show the main application scene
                    Scene mainScene = createMainScene(primaryStage);
                    primaryStage.setTitle("Hotel Management System");
                    primaryStage.setScene(mainScene);
                } else {
                    // Show error message
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Login Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid username or password.");
                    alert.showAndWait();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 1, 2);
        grid.setAlignment(Pos.CENTER);

        return new Scene(grid, 900, 900);
    }

    private Scene createMainScene(Stage primaryStage) {
        TabPane tabPane = new TabPane();

        // Create tabs
        Tab bookingTab = createBookingTab();
        Tab cancelTab = createCancelBookingTab();
        Tab searchTab = createSearchTab();
        Tab checkInTab = createCheckInTab();
        Tab checkOutTab = createCheckOutTab();
        Tab customerTab = createCustomerTab();

        // Add tabs to pane
        tabPane.getTabs().addAll(bookingTab, cancelTab, searchTab, checkInTab, checkOutTab, customerTab);
        tabPane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        return new Scene(tabPane, 800, 600);

    }

    // Initialize hotel with sample data
    private void initializeHotelData() {
        try (Connection conn = DB.DBConnection()) {
            // Load rooms
            String roomQuery = "SELECT * FROM Rooms";
            try (PreparedStatement pst = conn.prepareStatement(roomQuery);
                    ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int roomNumber = rs.getInt("roomNumber");
                    String roomType = rs.getString("roomType");
                    double roomRate = rs.getDouble("roomRate");
                    boolean availability = rs.getString("availability").equals("Y");
                    hotel.addRoom(new Room(roomNumber, roomType, roomRate, availability));
                }
            }

            // Load customers
            String customerQuery = "SELECT * FROM Customers";
            try (PreparedStatement pst = conn.prepareStatement(customerQuery);
                    ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String phone = rs.getString("phone");
                    String email = rs.getString("email");
                    String address = rs.getString("address");
                    String idProof = rs.getString("idProof");
                    hotel.addCustomer(new Customer(id, name, phone, email, address, idProof));
                }
            }

            // Load services
            String serviceQuery = "SELECT * FROM Services";
            try (PreparedStatement pst = conn.prepareStatement(serviceQuery);
                    ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int serviceId = rs.getInt("serviceId");
                    String serviceName = rs.getString("serviceName");
                    double price = rs.getDouble("price");
                    String description = rs.getString("description");
                    hotel.addService(new AdditionalService(serviceId, serviceName, price, description));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Create booking tab
    private Tab createBookingTab() {
        Tab tab = new Tab("Room Booking");
        tab.setClosable(false);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        // Customer selection
        Label customerLabel = new Label("Select Customer:");
        ComboBox<String> customerCombo = new ComboBox<>();
        for (Customer customer : hotel.getCustomers()) {
            customerCombo.getItems().add(customer.getId() + " - " + customer.getName());
        }

        // Room selection
        Label roomLabel = new Label("Select Room:");
        ComboBox<String> roomCombo = new ComboBox<>();
        for (Room room : hotel.getRooms()) {
            if (room.isAvailable()) {
                roomCombo.getItems().add(room.getRoomNumber() + " - " +
                        room.getRoomType() + " - $" +
                        room.getRoomRate());
            }
        }

        // Dates
        Label checkInLabel = new Label("Check-in Date (yyyy-MM-dd):");
        TextField checkInField = new TextField();

        Label checkOutLabel = new Label("Check-out Date (yyyy-MM-dd):");
        TextField checkOutField = new TextField();

        // Book button
        Button bookButton = new Button("Book Room");
        bookButton.setOnAction(e -> {
            String getNextIdSQL = "SELECT NVL(MAX(bookingid), 0) + 1 AS nextId FROM Bookings";
            int nextId = 0;

            try (Connection conn = DB.DBConnection();
                    PreparedStatement pst = conn.prepareStatement(getNextIdSQL);
                    ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    nextId = rs.getInt("nextId");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try (Connection conn = DB.DBConnection()) {
                // Get selected customer
                String customerSelection = customerCombo.getValue();
                int customerId = Integer.parseInt(customerSelection.split(" - ")[0]);

                // Get selected room
                String roomSelection = roomCombo.getValue();
                int roomNumber = Integer.parseInt(roomSelection.split(" - ")[0]);

                // Parse dates
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date checkInDate = sdf.parse(checkInField.getText());
                Date checkOutDate = sdf.parse(checkOutField.getText());

                // Insert booking
                String insertBookingSQL = "INSERT INTO Bookings (bookingid, customerId, roomNumber, checkInDate, checkOutDate, status) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pst = conn.prepareStatement(insertBookingSQL)) {
                    pst.setInt(1, nextId);
                    pst.setInt(2, customerId);
                    pst.setInt(3, roomNumber);
                    pst.setDate(4, new java.sql.Date(checkInDate.getTime()));
                    pst.setDate(5, new java.sql.Date(checkOutDate.getTime()));
                    pst.setString(6, "Reserved");
                    pst.executeUpdate();
                }

                // Update room availability
                String updateRoomSQL = "UPDATE Rooms SET availability = 'N' WHERE roomNumber = ?";
                try (PreparedStatement pst = conn.prepareStatement(updateRoomSQL)) {
                    pst.setInt(1, roomNumber);
                    pst.executeUpdate();
                }

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Booking Successful");
                alert.setHeaderText(null);
                alert.setContentText("Booking created successfully!");
                alert.showAndWait();

                // Refresh room combo box
                roomCombo.getItems().clear();
                loadAvailableRooms(roomCombo);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        vbox.getChildren().addAll(customerLabel, customerCombo, roomLabel, roomCombo,
                checkInLabel, checkInField, checkOutLabel, checkOutField,
                bookButton);

        tab.setContent(vbox);
        return tab;
    }

    // Create cancel booking tab
    private Tab createCancelBookingTab() {
        Tab tab = new Tab("Cancel Booking");
        tab.setClosable(false);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label bookingLabel = new Label("Enter Booking ID:");
        TextField bookingField = new TextField();

        Button cancelButton = new Button("Cancel Booking");
        cancelButton.setOnAction(e -> {
            try (Connection conn = DB.DBConnection()) {
                int bookingId = Integer.parseInt(bookingField.getText());

                // Get room number for the booking
                String getRoomSQL = "SELECT roomNumber FROM Bookings WHERE bookingId = ?";
                int roomNumber = 0;
                try (PreparedStatement pst = conn.prepareStatement(getRoomSQL)) {
                    pst.setInt(1, bookingId);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        roomNumber = rs.getInt("roomNumber");
                    }
                }

                // Delete associated services from BookingServices
                String deleteServicesSQL = "DELETE FROM BookingServices WHERE bookingId = ?";
                try (PreparedStatement pst = conn.prepareStatement(deleteServicesSQL)) {
                    pst.setInt(1, bookingId);
                    pst.executeUpdate();
                }

                // Delete booking
                String deleteBookingSQL = "DELETE FROM Bookings WHERE bookingId = ?";
                try (PreparedStatement pst = conn.prepareStatement(deleteBookingSQL)) {
                    pst.setInt(1, bookingId);
                    pst.executeUpdate();
                }

                // Update room availability
                String updateRoomSQL = "UPDATE Rooms SET availability = 'Y' WHERE roomNumber = ?";
                try (PreparedStatement pst = conn.prepareStatement(updateRoomSQL)) {
                    pst.setInt(1, roomNumber);
                    pst.executeUpdate();
                }

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Booking Cancelled");
                alert.setHeaderText(null);
                alert.setContentText("Booking #" + bookingId + " has been cancelled.");
                alert.showAndWait();

                bookingField.clear();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        vbox.getChildren().addAll(bookingLabel, bookingField, cancelButton);

        tab.setContent(vbox);
        return tab;
    }

    // Create search tab
    private Tab createSearchTab() {
        Tab tab = new Tab("Search");
        tab.setClosable(false);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        // Room type filter
        Label roomTypeLabel = new Label("Room Type:");
        ComboBox<String> roomTypeCombo = new ComboBox<>();
        roomTypeCombo.getItems().addAll("Any", "Single", "Double", "Suite");
        roomTypeCombo.setValue("Any");

        // Results area
        Label resultsLabel = new Label("Available Rooms:");
        TextArea resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefHeight(200);

        // Search button
        Button searchButton = new Button("Search Rooms");
        searchButton.setOnAction(e -> {
            try (Connection conn = DB.DBConnection()) {

                // Get room type
                String roomType = roomTypeCombo.getValue();
                boolean filterByRoomType = !roomType.equals("Any");

                // Build SQL query
                StringBuilder sql = new StringBuilder("SELECT * FROM Rooms WHERE availability = 'Y'");
                if (filterByRoomType) {
                    sql.append(" AND roomType = ?");
                }

                try (PreparedStatement pst = conn.prepareStatement(sql.toString())) {
                    if (filterByRoomType) {
                        pst.setString(1, roomType);
                    }

                    ResultSet rs = pst.executeQuery();

                    // Display results
                    StringBuilder sb = new StringBuilder();
                    while (rs.next()) {
                        int roomNumber = rs.getInt("roomNumber");
                        String type = rs.getString("roomType");
                        double rate = rs.getDouble("roomRate");

                        sb.append("Room ").append(roomNumber)
                                .append(" - ").append(type)
                                .append(" - $").append(rate)
                                .append(" per night\n");
                    }

                    if (sb.length() == 0) {
                        resultsArea.setText("No available rooms matching your criteria.");
                    } else {
                        resultsArea.setText(sb.toString());
                    }
                }

            } catch (Exception ex) {
                // Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Search Error");
                alert.setHeaderText(null);
                alert.setContentText("Error searching rooms: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        vbox.getChildren().addAll(roomTypeLabel, roomTypeCombo, searchButton, resultsLabel, resultsArea);

        tab.setContent(vbox);
        return tab;
    }

    // Create check-in tab
    private Tab createCheckInTab() {
        Tab tab = new Tab("Check-in");
        tab.setClosable(false);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label bookingLabel = new Label("Enter Booking ID:");
        TextField bookingField = new TextField();

        // Booking details area
        Label detailsLabel = new Label("Booking Details:");
        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPrefHeight(150);

        // Search booking button
        Button searchButton = new Button("Search Booking");
        searchButton.setOnAction(e -> {
            try (Connection conn = DB.DBConnection();
                    PreparedStatement pst = conn.prepareStatement(
                            "SELECT b.bookingId, c.name AS customerName, r.roomNumber, r.roomType, b.checkInDate, b.checkOutDate, b.status "
                                    +
                                    "FROM Bookings b " +
                                    "JOIN Customers c ON b.customerId = c.id " +
                                    "JOIN Rooms r ON b.roomNumber = r.roomNumber " +
                                    "WHERE b.bookingId = ?")) {

                int bookingId = Integer.parseInt(bookingField.getText());
                pst.setInt(1, bookingId);

                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    StringBuilder details = new StringBuilder();
                    details.append("Booking ID: ").append(rs.getInt("bookingId")).append("\n");
                    details.append("Customer: ").append(rs.getString("customerName")).append("\n");
                    details.append("Room: ").append(rs.getInt("roomNumber"))
                            .append(" - ").append(rs.getString("roomType")).append("\n");
                    details.append("Check-in Date: ").append(rs.getDate("checkInDate")).append("\n");
                    details.append("Check-out Date: ").append(rs.getDate("checkOutDate")).append("\n");
                    details.append("Status: ").append(rs.getString("status"));

                    detailsArea.setText(details.toString());
                } else {
                    detailsArea.setText("Booking not found!");
                }

            } catch (Exception ex) {
                detailsArea.setText("Error: " + ex.getMessage());
            }
        });

        // Check-in button
        Button checkInButton = new Button("Check-in Guest");
        checkInButton.setOnAction(e -> {
            try (Connection conn = DB.DBConnection();
                    PreparedStatement pst = conn.prepareStatement(
                            "UPDATE Bookings SET status = 'Checked In' WHERE bookingId = ?")) {

                int bookingId = Integer.parseInt(bookingField.getText());
                pst.setInt(1, bookingId);

                int rowsUpdated = pst.executeUpdate();
                if (rowsUpdated > 0) {
                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Check-in Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("Guest has been checked in successfully!");
                    alert.showAndWait();

                    // Refresh booking details
                    searchButton.fire();
                } else {
                    throw new Exception("Booking not found!");
                }

            } catch (Exception ex) {
                // Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Check-in Error");
                alert.setHeaderText(null);
                alert.setContentText("Error during check-in: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        vbox.getChildren().addAll(bookingLabel, bookingField, searchButton,
                detailsLabel, detailsArea, checkInButton);

        tab.setContent(vbox);
        return tab;
    }

    // Create check-out tab
    private Tab createCheckOutTab() {
        Tab tab = new Tab("Check-out");
        tab.setClosable(false);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label bookingLabel = new Label("Enter Booking ID:");
        TextField bookingField = new TextField();

        // Booking details and bill area
        Label billLabel = new Label("Bill:");
        TextArea billArea = new TextArea();
        billArea.setEditable(false);
        billArea.setPrefHeight(250);

        // Search booking button
        Button searchButton = new Button("Generate Bill");
        searchButton.setOnAction(e -> {
            try (Connection conn = DB.DBConnection();
                    PreparedStatement pst = conn.prepareStatement(
                            "SELECT b.bookingId, c.name AS customerName, r.roomNumber, r.roomType, r.roomRate, b.checkInDate, b.checkOutDate "
                                    +
                                    "FROM Bookings b " +
                                    "JOIN Customers c ON b.customerId = c.id " +
                                    "JOIN Rooms r ON b.roomNumber = r.roomNumber " +
                                    "WHERE b.bookingId = ?")) {

                int bookingId = Integer.parseInt(bookingField.getText());
                pst.setInt(1, bookingId);

                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    // Calculate the total bill for the room
                    Date checkInDate = rs.getDate("checkInDate");
                    Date checkOutDate = rs.getDate("checkOutDate");
                    long diff = checkOutDate.getTime() - checkInDate.getTime();
                    long days = diff / (1000 * 60 * 60 * 24);
                    double roomRate = rs.getDouble("roomRate");
                    double totalBill = days * roomRate;

                    StringBuilder bill = new StringBuilder();
                    bill.append("Booking ID: ").append(rs.getInt("bookingId")).append("\n");
                    bill.append("Customer: ").append(rs.getString("customerName")).append("\n");
                    bill.append("Room: ").append(rs.getInt("roomNumber"))
                            .append(" - ").append(rs.getString("roomType")).append("\n");
                    bill.append("Check-in Date: ").append(checkInDate).append("\n");
                    bill.append("Check-out Date: ").append(checkOutDate).append("\n");
                    bill.append("Room Charges: $").append(totalBill).append("\n");

                    // Fetch and add service charges
                    String serviceQuery = "SELECT s.serviceName, s.price " +
                            "FROM BookingServices bs " +
                            "JOIN Services s ON bs.serviceId = s.serviceId " +
                            "WHERE bs.bookingId = ?";
                    try (PreparedStatement servicePst = conn.prepareStatement(serviceQuery)) {
                        servicePst.setInt(1, bookingId);
                        ResultSet serviceRs = servicePst.executeQuery();

                        double serviceTotal = 0.0;
                        while (serviceRs.next()) {
                            String serviceName = serviceRs.getString("serviceName");
                            double servicePrice = serviceRs.getDouble("price");
                            serviceTotal += servicePrice;

                            bill.append("Service: ").append(serviceName)
                                    .append(" - $").append(servicePrice).append("\n");
                        }

                        // Add service charges to the total bill
                        totalBill += serviceTotal;
                    }

                    bill.append("Total Bill: $").append(totalBill);

                    // Display the bill
                    billArea.setText(bill.toString());
                } else {
                    billArea.setText("Booking not found!");
                }

            } catch (Exception ex) {
                billArea.setText("Error: " + ex.getMessage());
            }
        });
        // Add service button
        Button addServiceButton = new Button("Add Service");
        addServiceButton.setOnAction(e -> {
            try (Connection conn = DB.DBConnection()) {
                int bookingId = Integer.parseInt(bookingField.getText());

                // Create a dialog to select service
                Dialog<Pair<Integer, String>> dialog = new Dialog<>();
                dialog.setTitle("Add Service");
                dialog.setHeaderText("Select a service to add:");

                // Set button types
                ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

                // Create the service selection ComboBox
                ComboBox<Pair<Integer, String>> serviceCombo = new ComboBox<>();
                try (PreparedStatement pst = conn.prepareStatement("SELECT * FROM Services");
                        ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        int serviceId = rs.getInt("serviceId");
                        String serviceName = rs.getString("serviceName");
                        double price = rs.getDouble("price");
                        serviceCombo.getItems().add(new Pair<>(serviceId, serviceName + " - $" + price));
                    }
                }

                // Layout for dialog
                VBox dialogVbox = new VBox(10);
                dialogVbox.getChildren().add(serviceCombo);
                dialog.getDialogPane().setContent(dialogVbox);

                // Convert the result to a Pair when the Add button is clicked
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == addButtonType) {
                        return serviceCombo.getValue();
                    }
                    return null;
                });

                // Show dialog and process result
                Optional<Pair<Integer, String>> result = dialog.showAndWait();
                result.ifPresent(servicePair -> {
                    try (PreparedStatement pst = conn.prepareStatement(
                            "INSERT INTO BookingServices (bookingId, serviceId) VALUES (?, ?)")) {
                        pst.setInt(1, bookingId);
                        pst.setInt(2, servicePair.getKey()); // Get the serviceId from the Pair
                        pst.executeUpdate();

                        // Extract the service price from the value
                        String serviceDetails = servicePair.getValue(); // Example: "ServiceName - $Price"
                        double servicePrice = Double.parseDouble(serviceDetails.split(" - \\$")[1]);

                        String currentBill = billArea.getText();
                        double totalBill = 0.0;

                        // Extract the current total bill if it exists
                        if (currentBill.contains("Total Bill: $")) {
                            String[] lines = currentBill.split("\n");
                            for (String line : lines) {
                                if (line.startsWith("Total Bill: $")) {
                                    totalBill = Double.parseDouble(line.replace("Total Bill: $", "").trim());
                                }
                            }
                        }

                        // Update the total bill with the service charge
                        totalBill += servicePrice;

                        // Append the service charge to the bill
                        StringBuilder updatedBill = new StringBuilder(currentBill);
                        updatedBill.append("\nService Added: $").append(servicePrice);
                        updatedBill.append("\nTotal Bill: $").append(totalBill);

                        billArea.setText(updatedBill.toString());

                        // Show confirmation
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Service Added");
                        alert.setHeaderText(null);
                        alert.setContentText("Service added to booking and bill updated.");
                        alert.showAndWait();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        // Show error message
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText("Failed to add service");
                        errorAlert.setContentText(ex.getMessage());
                        errorAlert.showAndWait();
                    }
                });

            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid booking ID");
                alert.showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Database Error");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });

        // Check-out button
        Button checkOutButton = new Button("Complete Check-out");
        checkOutButton.setOnAction(e -> {
            try (Connection conn = DB.DBConnection()) {
                int bookingId = Integer.parseInt(bookingField.getText());

                // Update booking status
                String updateBookingSQL = "UPDATE Bookings SET status = 'Checked Out' WHERE bookingId = ?";
                try (PreparedStatement pst = conn.prepareStatement(updateBookingSQL)) {
                    pst.setInt(1, bookingId);
                    pst.executeUpdate();
                }

                // Get room number for the booking
                String getRoomSQL = "SELECT roomNumber FROM Bookings WHERE bookingId = ?";
                int roomNumber = 0;
                try (PreparedStatement pst = conn.prepareStatement(getRoomSQL)) {
                    pst.setInt(1, bookingId);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        roomNumber = rs.getInt("roomNumber");
                    }
                }

                // Update room availability
                String updateRoomSQL = "UPDATE Rooms SET availability = 'Y' WHERE roomNumber = ?";
                try (PreparedStatement pst = conn.prepareStatement(updateRoomSQL)) {
                    pst.setInt(1, roomNumber);
                    pst.executeUpdate();
                }

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Check-out Successful");
                alert.setHeaderText(null);
                alert.setContentText("Check-out completed successfully!");
                alert.showAndWait();

            } catch (Exception ex) {
                // Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Check-out Error");
                alert.setHeaderText(null);
                alert.setContentText("Error during check-out: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        // Layout the controls
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(searchButton, addServiceButton, checkOutButton);

        vbox.getChildren().addAll(bookingLabel, bookingField, buttonBox,
                billLabel, billArea);

        tab.setContent(vbox);
        return tab;
    }

    // Create customer tab
    private Tab createCustomerTab() {
        Tab tab = new Tab("Customers");
        tab.setClosable(false);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        // Customer search section
        Label searchLabel = new Label("Search Customer:");
        TextField searchField = new TextField();

        // Customer details area
        Label detailsLabel = new Label("Customer Details:");
        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPrefHeight(150);

        // Customer bookings area
        Label bookingsLabel = new Label("Customer Bookings:");
        TextArea bookingsArea = new TextArea();
        bookingsArea.setEditable(false);
        bookingsArea.setPrefHeight(150);

        // Search by ID button
        Button searchIdButton = new Button("Search by ID");
        searchIdButton.setOnAction(e -> {
            try (Connection conn = DB.DBConnection()) {
                String idProof = searchField.getText(); // Get the ID Proof from the search field

                // Fetch customer details by ID Proof
                String customerQuery = "SELECT * FROM Customers WHERE idProof = ?";
                try (PreparedStatement pst = conn.prepareStatement(customerQuery)) {
                    pst.setString(1, idProof); // Set the ID Proof in the query
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        StringBuilder details = new StringBuilder();
                        details.append("ID: ").append(rs.getInt("id")).append("\n");
                        details.append("Name: ").append(rs.getString("name")).append("\n");
                        details.append("Phone: ").append(rs.getString("phone")).append("\n");
                        details.append("Email: ").append(rs.getString("email")).append("\n");
                        details.append("Address: ").append(rs.getString("address")).append("\n");
                        details.append("ID Proof: ").append(rs.getString("idProof"));
                        detailsArea.setText(details.toString());
                    } else {
                        detailsArea.setText("Customer not found!");
                        bookingsArea.clear();
                        return;
                    }
                }

                // Fetch customer bookings
                String bookingsQuery = "SELECT * FROM Bookings WHERE customerId = (SELECT id FROM Customers WHERE idProof = ?)";
                try (PreparedStatement pst = conn.prepareStatement(bookingsQuery)) {
                    pst.setString(1, idProof); // Set the ID Proof in the query
                    ResultSet rs = pst.executeQuery();

                    StringBuilder bookings = new StringBuilder();
                    while (rs.next()) {
                        bookings.append("Booking ID: ").append(rs.getInt("bookingId")).append("\n");
                        bookings.append("Room Number: ").append(rs.getInt("roomNumber")).append("\n");
                        bookings.append("Check-in Date: ").append(rs.getDate("checkInDate")).append("\n");
                        bookings.append("Check-out Date: ").append(rs.getDate("checkOutDate")).append("\n");
                        bookings.append("Status: ").append(rs.getString("status")).append("\n\n");
                    }

                    if (bookings.length() == 0) {
                        bookingsArea.setText("No bookings found for this customer.");
                    } else {
                        bookingsArea.setText(bookings.toString());
                    }
                }

            } catch (Exception ex) {
                detailsArea.setText("Error: " + ex.getMessage());
                bookingsArea.clear();
            }
        });

        // Search by Phone button
        Button searchPhoneButton = new Button("Search by Phone");
        searchPhoneButton.setOnAction(e -> {
            try (Connection conn = DB.DBConnection()) {
                String phone = searchField.getText();

                // Fetch customer details
                String customerQuery = "SELECT * FROM Customers WHERE phone = ?";
                try (PreparedStatement pst = conn.prepareStatement(customerQuery)) {
                    pst.setString(1, phone);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        StringBuilder details = new StringBuilder();
                        details.append("ID: ").append(rs.getInt("id")).append("\n");
                        details.append("Name: ").append(rs.getString("name")).append("\n");
                        details.append("Phone: ").append(rs.getString("phone")).append("\n");
                        details.append("Email: ").append(rs.getString("email")).append("\n");
                        details.append("Address: ").append(rs.getString("address")).append("\n");
                        details.append("ID Proof: ").append(rs.getString("idProof"));
                        detailsArea.setText(details.toString());
                    } else {
                        detailsArea.setText("Customer not found!");
                        bookingsArea.clear();
                        return;
                    }
                }

                // Fetch customer bookings
                String bookingsQuery = "SELECT * FROM Bookings WHERE customerId = (SELECT id FROM Customers WHERE phone = ?)";
                try (PreparedStatement pst = conn.prepareStatement(bookingsQuery)) {
                    pst.setString(1, phone);
                    ResultSet rs = pst.executeQuery();

                    StringBuilder bookings = new StringBuilder();
                    while (rs.next()) {
                        bookings.append("Booking ID: ").append(rs.getInt("bookingId")).append("\n");
                        bookings.append("Room Number: ").append(rs.getInt("roomNumber")).append("\n");
                        bookings.append("Check-in Date: ").append(rs.getDate("checkInDate")).append("\n");
                        bookings.append("Check-out Date: ").append(rs.getDate("checkOutDate")).append("\n");
                        bookings.append("Status: ").append(rs.getString("status")).append("\n\n");
                    }

                    if (bookings.length() == 0) {
                        bookingsArea.setText("No bookings found for this customer.");
                    } else {
                        bookingsArea.setText(bookings.toString());
                    }
                }

            } catch (Exception ex) {
                detailsArea.setText("Error: " + ex.getMessage());
                bookingsArea.clear();
            }
        });

        // Add new customer button
        Button addCustomerButton = new Button("Add New Customer");
        addCustomerButton.setOnAction(e -> {
            Dialog<Customer> dialog = new Dialog<>();
            dialog.setTitle("Add New Customer");
            dialog.setHeaderText("Enter Customer Details:");

            // Set button types
            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            // Create the form fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField nameField = new TextField();
            TextField phoneField = new TextField();
            TextField emailField = new TextField();
            TextField addressField = new TextField();
            TextField idProofField = new TextField();

            grid.add(new Label("Name:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Phone:"), 0, 1);
            grid.add(phoneField, 1, 1);
            grid.add(new Label("Email:"), 0, 2);
            grid.add(emailField, 1, 2);
            grid.add(new Label("Address:"), 0, 3);
            grid.add(addressField, 1, 3);
            grid.add(new Label("ID Proof:"), 0, 4);
            grid.add(idProofField, 1, 4);

            dialog.getDialogPane().setContent(grid);

            // Request focus on the name field by default
            Platform.runLater(() -> nameField.requestFocus());

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    String getNextIdSQL = "SELECT NVL(MAX(id), 0) + 1 AS nextId FROM Customers";
                    int nextId = 0;
                    try (Connection conn = DB.DBConnection();
                            PreparedStatement pst = conn.prepareStatement(getNextIdSQL);
                            ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            nextId = rs.getInt("nextId");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    String insertCustomerSQL = "INSERT INTO Customers (id, name, phone, email, address, idProof) VALUES (?, ?, ?, ?, ?, ?)";
                    try (Connection conn = DB.DBConnection()) {
                        try (PreparedStatement pst = conn.prepareStatement(insertCustomerSQL)) {
                            pst.setInt(1, nextId);
                            pst.setString(2, nameField.getText());
                            pst.setString(3, phoneField.getText());
                            pst.setString(4, emailField.getText());
                            pst.setString(5, addressField.getText());
                            pst.setString(6, idProofField.getText());
                            pst.executeUpdate();
                        }
                        return new Customer(0, nameField.getText(), phoneField.getText(), emailField.getText(),
                                addressField.getText(), idProofField.getText());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
            });

            Optional<Customer> result = dialog.showAndWait();
            result.ifPresent(customer -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Customer Added");
                alert.setHeaderText(null);
                alert.setContentText("New customer added successfully!");
                alert.showAndWait();
            });
        });

        // Layout the controls
        HBox searchButtonBox = new HBox(10);
        searchButtonBox.getChildren().addAll(searchIdButton, searchPhoneButton, addCustomerButton);

        vbox.getChildren().addAll(searchLabel, searchField, searchButtonBox,
                detailsLabel, detailsArea, bookingsLabel, bookingsArea);

        tab.setContent(vbox);
        return tab;
    }

    // Main method
    public static void main(String[] args) {
        launch(args);
    }

    // Custom exception for booking errors
    class BookingException extends Exception {
        public BookingException(String message) {
            super(message);
        }
    }

    // Custom exception for validation errors
    class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    // Method to load available rooms into the ComboBox
    private void loadAvailableRooms(ComboBox<String> roomCombo) {
        try (Connection conn = DB.DBConnection();
                PreparedStatement pst = conn.prepareStatement("SELECT * FROM Rooms WHERE availability = 'Y'");
                ResultSet rs = pst.executeQuery()) {

            // Clear the ComboBox before adding new items
            roomCombo.getItems().clear();

            // Iterate through the result set and add available rooms to the ComboBox
            while (rs.next()) {
                int roomNumber = rs.getInt("roomNumber");
                String roomType = rs.getString("roomType");
                double roomRate = rs.getDouble("roomRate");

                // Add room details to the ComboBox
                roomCombo.getItems().add(roomNumber + " - " + roomType + " - $" + roomRate);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            // Show an error message if something goes wrong
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error loading available rooms: " + ex.getMessage());
            alert.showAndWait();
        }
    }
}