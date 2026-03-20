package Application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import DataBase.DatabaseHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.util.Pair;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.LinkedHashSet; 

public class BookingController {

    @FXML private VBox searchSection;
    @FXML private VBox myBookingsSection;
    @FXML private ComboBox<String> fromCity, toCity;
    @FXML private DatePicker travelDate;
    @FXML private VBox busListContainer;
    @FXML private VBox busResultsContainer; // Naya container ScrollPane ke liye
    @FXML private TableView<BookingModel> passengerTable;
    @FXML private TableColumn<BookingModel, String> colBus, colRoute, colStatus, colPayment;
    @FXML private TableColumn<BookingModel, Integer> colSeat;
    @FXML private Button confirmPaymentBtn; 
    @FXML private Button chooseFileBtn;
    @FXML private Label fileNameLabel;
    @FXML private GridPane seatGrid; 
    @FXML private VBox dataEntrySection; 
    @FXML private TextField cnicInput, phoneInput;
    @FXML private ComboBox<String> managerDropdown;
    @FXML private Label selectedSeatLabel;
    @FXML private HBox bookingArea; 

    private int selectedSeatNumber = -1;
    private String selectedBusNum = "";
    private String selectedFilePath = ""; 
    private String selectedBusName = ""; 
    private double selectedPrice = 0.0;  

    private String currentPassengerName = UserSession.getName(); 
    private ObservableList<BookingModel> passengerBookings = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // --- COLOR FIX START ---
        fromCity.setButtonCell(new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setText(null);
                else { setText(item); setStyle("-fx-text-fill: black; -fx-font-weight: bold;"); }
            }
        });

        toCity.setButtonCell(new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setText(null);
                else { setText(item); setStyle("-fx-text-fill: black; -fx-font-weight: bold;"); }
            }
        });
        // --- COLOR FIX END ---

        if (searchSection != null) searchSection.setVisible(true);
        if (myBookingsSection != null) myBookingsSection.setVisible(false);
        if (confirmPaymentBtn != null) confirmPaymentBtn.setDisable(true);
        
        loadAvailableRoutes();
        
        if (passengerTable != null) {
            setupTableColumns();
            loadPassengerBookings();
        }
        
        if (dataEntrySection != null) dataEntrySection.setVisible(false);
        if (bookingArea != null) bookingArea.setVisible(false);
    }

    private void loadAvailableRoutes() {
        if (fromCity == null || toCity == null) return;
        
        ObservableList<String> sourceCities = FXCollections.observableArrayList();
        ObservableList<String> destCities = FXCollections.observableArrayList();
        
        String querySource = "SELECT DISTINCT SOURCE FROM BUSES";
        String queryDest = "SELECT DISTINCT DESTINATION FROM BUSES";
        
        try (Connection conn = DatabaseHandler.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs1 = stmt.executeQuery(querySource);
            while (rs1.next()) {
                String s = rs1.getString("SOURCE");
                if (s != null) sourceCities.add(s);
            }
            fromCity.setItems(sourceCities); 

            ResultSet rs2 = stmt.executeQuery(queryDest);
            while (rs2.next()) {
                String d = rs2.getString("DESTINATION");
                if (d != null) destCities.add(d);
            }
            toCity.setItems(destCities);
            
        } catch (SQLException e) { 
            e.printStackTrace(); 
            showAlert("Database Error", "Cities load karne mein masla aya.");
        }
    }

    @FXML
    private void showSearchSection() {
        if (searchSection != null) searchSection.setVisible(true);
        if (myBookingsSection != null) myBookingsSection.setVisible(false);
    }

    @FXML
    private void showMyBookingsSection() {
        if (searchSection != null) searchSection.setVisible(false);
        if (myBookingsSection != null) {
            myBookingsSection.setVisible(true);
            loadPassengerBookings(); 
        }
    }

    private void loadSeatGrid(String busNum, String date) {
        seatGrid.getChildren().clear();
        selectedSeatNumber = -1; 
        selectedSeatLabel.setText("Selected Seat: None");
        ObservableList<Integer> bookedSeats = getBookedSeatsFromDB(busNum, date);
        for (int i = 1; i <= 30; i++) {
            Button seatBtn = new Button(String.valueOf(i));
            seatBtn.setPrefSize(45, 40);
            if (bookedSeats.contains(i)) {
                seatBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white;"); 
                seatBtn.setDisable(true);
            } else {
                seatBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-cursor: hand;"); 
                int finalI = i;
                seatBtn.setOnAction(e -> {
                    selectedSeatNumber = finalI;
                    selectedSeatLabel.setText("Selected Seat: " + finalI);
                });
            }
            int row = (i - 1) / 5;
            int col = (i - 1) % 5;
            seatGrid.add(seatBtn, col, row);
        }
    }

    private void showDataEntrySection() {
        dataEntrySection.setVisible(true);
        managerDropdown.getItems().clear();
        try (Connection conn = DatabaseHandler.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT FULL_NAME FROM USERS WHERE ROLE = 'Manager'")) {
            while(rs.next()) managerDropdown.getItems().add(rs.getString("FULL_NAME"));
        } catch(Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleSearchBuses(ActionEvent event) {
        String from = fromCity.getValue();
        String to = toCity.getValue();
        if (from == null || to == null || travelDate.getValue() == null) {
            showAlert("Error", "Please fill all fields!");
            return;
        }
        
        if (busListContainer != null) busListContainer.getChildren().clear();
        if (busResultsContainer != null) busResultsContainer.getChildren().clear();

        String query = "SELECT * FROM BUSES WHERE SOURCE = ? AND DESTINATION = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, from);
            pstmt.setString(2, to);
            ResultSet rs = pstmt.executeQuery();
            boolean found = false;
            while (rs.next()) {
                found = true;
                
                // --- INTEGRATED: Bus Type Logic ---
                String busType = rs.getString("BUS_TYPE");
                if (busType == null) busType = "Standard"; 

                addBusRow(
                    rs.getString("BUS_NAME"), 
                    rs.getString("BUS_NUMBER"), 
                    busType, 
                    rs.getString("DEPARTURE_TIME"), 
                    rs.getDouble("TICKET_PRICE")
                );
            }
            if (!found) {
                if (busResultsContainer != null) busResultsContainer.getChildren().add(new Label("No buses found for this route."));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addBusRow(String name, String busNum, String type, String time, double price) {
        HBox row = new HBox(15);
        row.setStyle("-fx-background-color: #1e293b; -fx-padding: 12; -fx-background-radius: 10; -fx-alignment: CENTER_LEFT;");
        VBox details = new VBox(5);
        Label busTitle = new Label(name + " [" + busNum + "]");
        busTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Label subInfo = new Label(type + " | " + time);
        subInfo.setStyle("-fx-text-fill: #94a3b8;");
        details.getChildren().addAll(busTitle, subInfo);
        Label priceLbl = new Label("Rs. " + price);
        priceLbl.setStyle("-fx-text-fill: #38bdf8;");
        Button bookBtn = new Button("Select Seat");
        bookBtn.setStyle("-fx-background-color: #38bdf8; -fx-text-fill: white; -fx-cursor: hand;");
        bookBtn.setOnAction(e -> {
            this.selectedBusNum = busNum;
            this.selectedBusName = name;
            this.selectedPrice = price;
            if (bookingArea != null) bookingArea.setVisible(true); 
            loadSeatGrid(busNum, travelDate.getValue().toString());
            showDataEntrySection();
        });
        row.getChildren().addAll(details, priceLbl, bookBtn);
        
        // Dono containers mein add kar rahe hain for safety
        if (busListContainer != null) busListContainer.getChildren().add(row);
        if (busResultsContainer != null) busResultsContainer.getChildren().add(row);
    }

    @FXML
    private void handleFinalBooking() {
        if (selectedSeatNumber == -1) {
            showAlert("Error", "Please select a seat from the grid!");
            return;
        }
        if (cnicInput.getText().isEmpty() || phoneInput.getText().isEmpty() || managerDropdown.getValue() == null) {
            showAlert("Error", "Please fill all passenger details!");
            return;
        }
        if (selectedFilePath.isEmpty()) {
            showAlert("Error", "Please upload payment screenshot!");
            return;
        }
        String dateStr = travelDate.getValue().toString();
        if (checkBookingConstraint(cnicInput.getText(), dateStr, selectedBusName)) {
            finalizeBooking(selectedBusName, selectedPrice, phoneInput.getText(), cnicInput.getText(), managerDropdown.getValue(), selectedSeatNumber);
        }
    }

    private void finalizeBooking(String busName, double price, String phone, String cnic, String managerName, int seatNum) {
        String query = "INSERT INTO BOOKINGS (PASSENGER_NAME, PASSENGER_EMAIL, PASSENGER_CNIC, PHONE, BUS_NAME, BUS_NUMBER, SOURCE, DESTINATION, TRAVEL_DATE, PRICE, SEAT_NUMBER, STATUS, PAYMENT_STATUS, ASSIGNED_MANAGER, PAYMENT_PROOF_PATH) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', 'UNPAID', ?, ?)";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, currentPassengerName); 
            pstmt.setString(2, UserSession.getEmail()); 
            pstmt.setString(3, cnic);
            pstmt.setString(4, phone);
            pstmt.setString(5, busName);
            pstmt.setString(6, selectedBusNum);
            pstmt.setString(7, fromCity.getValue());
            pstmt.setString(8, toCity.getValue());
            pstmt.setString(9, travelDate.getValue().toString());
            pstmt.setDouble(10, price);
            pstmt.setInt(11, seatNum);
            pstmt.setString(12, managerName); 
            pstmt.setString(13, selectedFilePath); 
            pstmt.executeUpdate();
            showAlert("Success", "Booking request sent to " + managerName + " for approval.");
            dataEntrySection.setVisible(false);
            if (bookingArea != null) bookingArea.setVisible(false);
            loadPassengerBookings(); 
        } catch (Exception e) { e.printStackTrace(); }
    }

    private ObservableList<Integer> getBookedSeatsFromDB(String busNum, String date) {
        ObservableList<Integer> booked = FXCollections.observableArrayList();
        String query = "SELECT SEAT_NUMBER FROM BOOKINGS WHERE BUS_NUMBER = ? AND TRAVEL_DATE = ? AND STATUS != 'Rejected'";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, busNum);
            pstmt.setString(2, date);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) booked.add(rs.getInt("SEAT_NUMBER"));
        } catch (SQLException e) { e.printStackTrace(); }
        return booked;
    }

    private boolean checkBookingConstraint(String cnic, String date, String bus) {
        String query = "SELECT STATUS FROM BOOKINGS WHERE PASSENGER_CNIC = ? AND TRAVEL_DATE = ? AND BUS_NAME = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, cnic);
            pstmt.setString(2, date);
            pstmt.setString(3, bus);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("STATUS");
                if ("Approved".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status)) {
                    showAlert("Duplicate", "You already have an approved booking for this day.");
                    return false;
                } else if ("Rejected".equalsIgnoreCase(status)) {
                    showAlert("Already Rejected", "Already rejected. Plzz change your date and timings to request again.");
                    return false;
                } else if ("PENDING".equalsIgnoreCase(status)) {
                    showAlert("Pending", "Your previous request is still pending. Please wait for Manager approval.");
                    return false;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return true; 
    }

    @FXML
    private void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Payment Screenshot");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            this.selectedFilePath = selectedFile.getAbsolutePath();
            if (fileNameLabel != null) fileNameLabel.setText(selectedFile.getName());
            showAlert("File Uploaded", "Screenshot attached successfully!");
        }
    }

    private void setupTableColumns() {
        if (colBus != null) colBus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("busName"));
        if (colRoute != null) colRoute.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("route"));
        if (colSeat != null) colSeat.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("seatNumber"));
        if (colStatus != null) colStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        if (colPayment != null) colPayment.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("paymentStatus"));
    }

    private void loadPassengerBookings() {
        passengerBookings.clear();
        String query = "SELECT * FROM BOOKINGS WHERE PASSENGER_EMAIL = ?"; 
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, UserSession.getEmail());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String src = rs.getString("SOURCE");
                String dest = rs.getString("DESTINATION");
                String route = (src != null && dest != null) ? src + " to " + dest : "N/A";
                passengerBookings.add(new BookingModel(
                    rs.getString("PASSENGER_NAME"), rs.getString("BUS_NAME"), route,
                    rs.getString("PHONE"), rs.getString("STATUS"), rs.getString("PASSENGER_CNIC"),
                    rs.getInt("SEAT_NUMBER"), rs.getString("PAYMENT_STATUS")
                ));
            }
            if (passengerTable != null) passengerTable.setItems(passengerBookings);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handlePayment(ActionEvent event) {
        if (passengerTable == null) return;
        BookingModel selected = passengerTable.getSelectionModel().getSelectedItem();
        if (selected != null && "Approved".equalsIgnoreCase(selected.getStatus())) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Payment Portal");
            DialogPane dp = dialog.getDialogPane();
            dp.setStyle("-fx-background-color: #0f172a; -fx-border-color: #22c55e; -fx-border-width: 2;");
            VBox mainBox = new VBox(20);
            mainBox.setPadding(new Insets(25));
            mainBox.setAlignment(Pos.CENTER);
            Label title = new Label("Complete Your Payment");
            title.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 22px; -fx-font-weight: bold;");
            VBox card = new VBox(12);
            card.setStyle("-fx-background-color: #1e293b; -fx-padding: 20; -fx-background-radius: 15;");
            Label bName = new Label("Bank: Allied Bank Limited (ABL)");
            Label bAcc = new Label("Account Number: 5566-7788-9900-11");
            bName.setStyle("-fx-text-fill: white;");
            bAcc.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 18px; -fx-font-weight: bold;");
            card.getChildren().addAll(bName, bAcc);
            mainBox.getChildren().addAll(title, card);
            dp.setContent(mainBox);
            ButtonType payBtnType = new ButtonType("Confirm Payment", ButtonBar.ButtonData.OK_DONE);
            dp.getButtonTypes().addAll(payBtnType, ButtonType.CLOSE);
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == payBtnType) updatePaymentStatusInDB(selected);
        } else {
            showAlert("Wait", "Manager ki approval ka intezar karein.");
        }
    }

    private void updatePaymentStatusInDB(BookingModel booking) {
        String query = "UPDATE BOOKINGS SET PAYMENT_STATUS = 'PAID' WHERE PASSENGER_NAME = ? AND BUS_NAME = ? AND SEAT_NUMBER = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, booking.getPassengerName());
            pstmt.setString(2, booking.getBusName());
            pstmt.setInt(3, booking.getSeatNumber());
            pstmt.executeUpdate();
            showAlert("Success", "Payment updated to PAID.");
            loadPassengerBookings();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void handleLogout(ActionEvent event) {
        try { switchScene(event, "/Application/RoleSelection.fxml", 800, 500); } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void goBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Application/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    private void switchScene(ActionEvent event, String fxml, int w, int h) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Scene scene = new Scene(root, w, h);
        stage.setScene(scene);
        stage.show();
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}