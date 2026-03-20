package Application;


import DataBase.DatabaseHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList; // Added for search
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text; 
import javafx.stage.Stage;
import javafx.stage.FileChooser; // Added for Profile Logic
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter; 
import java.awt.Desktop;
import java.sql.*;
import java.util.Optional; 

public class ManagerController {
	
	// --- FXML Variables (IDs matched with your FXML) ---
    @FXML private TableColumn<BusScheduleModel, Double> colPriceSched;
    
    
    // Baaki UI Elements
  
    // Data Lists
  @FXML
	private TextField searchBusField; // Sirf ye ek rakhein, duplicate delete kar dein
	// Agar TextField hai toh
	@FXML private ScrollPane scheduleScroll;
	@FXML private Label lblTotalBookings, lblRevenue, lblActiveBuses;
	// Database access ke liye
	private DatabaseHandler dbHandler = new DatabaseHandler(); 
	// Sidebar buttons ke liye (Inka fx:id FXML se match hona chahiye)
	@FXML private Button btnAttendance;
	@FXML private Button btnMaintenance;
	// ManagerController.java ke upar ye add karein:
	@FXML
	private VBox profileSection; // Image_57c1bb ka error theek karega
	@FXML
	private ImageView bigProfileImg; // Image_57c1f6 ka error theek karega
	@FXML
	private TextField nameField; // Image_57c270 aur Image_57c24f ka error theek karega
	@FXML
	private TextField emailField; // Email field ka error theek karega
	@FXML
	private PasswordField newPassField; // Password field ka error theek karega
	@FXML
	private PasswordField confirmPassField; // Confirm password ka error theek kareg
    // Variables for Search
    private FilteredList<BusScheduleModel> filteredData;
    @FXML private HBox searchContainer;

    @FXML private HBox bookingControls;
    @FXML private HBox scheduleControls;
	
    @FXML private TableView<BookingModel> bookingsTable;
    @FXML private TableColumn<BookingModel, String> colPassenger, colBus, colRoute, colPhone, colStatus, colCNIC;
    @FXML private TableColumn<BookingModel, Integer> colSeat; 
    @FXML private Label lblFullName, lblShortEmail;
    @FXML private TextField priceInput;
    @FXML private TableColumn<BookingModel, String> colPaymentProof;
    
    // UI components for Sidebar and Header
    @FXML private Button btnBookings, btnSchedules;
    @FXML private Text txtHeader;

    
    // --- NEW: Profile UI Components ---
    @FXML private ImageView profileImageView;
    
    // --- Bus Schedule Table UI Components ---
    @FXML private TableView<BusScheduleModel> scheduleTable;
    @FXML private TableColumn<BusScheduleModel, String> colBusSched, colRouteSched, colTimeSched, colDateSched;
    @FXML private TableColumn<BusScheduleModel, String> colBusPlate; 
    @FXML private TableColumn<BusScheduleModel, Double> colPrice;   

    @FXML private VBox seatMapContainer; 

    private ObservableList<BookingModel> bookingList = FXCollections.observableArrayList();
    private ObservableList<BusScheduleModel> busScheduleList = FXCollections.observableArrayList();

    public void initialize() {
        // --- 1. Column binding for ticketPrice ---
        // 'ticketPrice' wahi naam hai jo aapne BusScheduleModel mein rakha hai
        if (colPrice != null) {
            colPrice.setCellValueFactory(new PropertyValueFactory<>("ticketPrice"));
        }
        if (colPriceSched != null) {
            colPriceSched.setCellValueFactory(new PropertyValueFactory<>("ticketPrice"));
        }
        
        // Initial visibility state
        scheduleTable.setVisible(false);
        scheduleTable.setManaged(false);
        
        // Load User Session and Profile
        UserModel user = UserSession.getInstance().getUser();
        if (user != null) {
            loadProfilePicture(user.getProfilePic());
        }
        
        setupColumns();
        setupPaymentProofColumn();
        loadBookings();
        
        // --- 2. Initialize schedule with visibility fix & FilteredList ---
        if (scheduleTable != null) {
            setupScheduleColumns();
            loadBusSchedules();
            
            // Initialize FilteredList for Search
            filteredData = new FilteredList<>(busScheduleList, p -> true);
            scheduleTable.setItems(filteredData);
            
            // --- 3. FIXED: Search Listener using 'searchBusField' ---
            // Yahan 'txtSearch' ki jagah 'searchBusField' use kiya taake crash na ho
            if (searchBusField != null) {
                searchBusField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filteredData.setPredicate(schedule -> {
                        if (newValue == null || newValue.isEmpty()) {
                            return true;
                        }
                        String lowerCaseFilter = newValue.toLowerCase();
                        
                        // Search by Bus Name or Route
                        if (schedule.getBusName().toLowerCase().contains(lowerCaseFilter)) {
                            return true; 
                        } else if (schedule.getRoute().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        }
                        return false;
                    });
                });
            }
            
            // Shuru mein schedule table chupa hua ho
            scheduleTable.setVisible(false);
            scheduleTable.setManaged(false);
            
            // Search bar bhi shuru mein hide hoga kyunke Booking tab active hota hai
            if (searchContainer != null) {
                searchContainer.setVisible(false);
                searchContainer.setManaged(false);
            }
        }
    }
    public boolean updateManagerProfile(String name, String email, String oldEmail) {
        String query = "UPDATE users SET full_name = ?, email = ? WHERE email = ?";
        try (Connection conn = DatabaseHandler.getConnection();// Ya jo bhi aapka connection method hai
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, oldEmail);
            
            int rows = ps.executeUpdate();
            return rows > 0; // Agar update hua toh true return karega
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    private void updateSidebarHighlight(Button activeBtn) {
        Button[] sideButtons = {btnBookings, btnSchedules, btnAttendance, btnMaintenance};
        
        for (Button btn : sideButtons) {
            if (btn == null) continue; // Safety check
            
            if (btn == activeBtn) {
                // Clicked button turns Blue
                btn.setStyle("-fx-background-color: #38bdf8; -fx-text-fill: white; -fx-background-radius: 5;");
            } else {
                // Others stay Transparent
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8;");
            }
        }
    }
 // ManagerController.java

    private void loadProfilePicture(String path) {
    	
        if (path != null && !path.isEmpty()) {
            try {
                // Error fix: File path ko URL mein convert karein
                File file = new File(path);
                if (file.exists()) {
                    // file.toURI().toString() khud ba khud "file:/" prefix laga deta hai
                    Image img = new Image(file.toURI().toString());
                    
                    bigProfileImg.setImage(img);
                    profileImageView.setImage(img); // Top choti image
                    
                    // Fitting properties
                    bigProfileImg.setPreserveRatio(false);
                    profileImageView.setPreserveRatio(false);
                }
            } catch (Exception e) {
                System.out.println("Image load nahi ho saki: " + e.getMessage());
            }
        }
    }
    
    
    // --- Search Logic ---
    @FXML
    private void handleSearch() {
        if (searchBusField == null || filteredData == null) return;
        
        String searchText = searchBusField.getText().toLowerCase();
        filteredData.setPredicate(bus -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            return bus.getBusName().toLowerCase().contains(searchText);
        });
    }

 // --- Profile Section & Editing Logic ---

    @FXML
    private void handleProfileClick() {
    	
    	setActiveButton(null); // Sidebar se blue color hata dega
        
        // UI Toggles
        showSection(bookingsTable, false);
        showSection(scheduleTable, false);
        showSection(bookingControls, false);
        showSection(searchContainer, false);
        
        // Profile section show
        showSection(profileSection, true);
        
        txtHeader.setText("Manager Account Profile Settings");
    	// Profile click par kisi bhi sidebar button ko blue nahi rehna chahiye (ya agar koi specific hai to wo)
        setActiveButton(null); 
        
        profileSection.setVisible(true);
        profileSection.setManaged(true);
        bookingsTable.setVisible(false);
        bookingsTable.setManaged(false);
    	
        // 1. Session check
        UserModel current = UserSession.getInstance().getUser();
        if (current == null) {
            showAlert("Error", "Session not found. Please login again.");
            return;
        }

        // 2. Dashboard Cleaning (Hide everything else)
        if (bookingsTable != null) { bookingsTable.setVisible(false); bookingsTable.setManaged(false); }
        if (scheduleTable != null) { scheduleTable.setVisible(false); scheduleTable.setManaged(false); }
        if (bookingControls != null) { bookingControls.setVisible(false); bookingControls.setManaged(false); }
        if (searchContainer != null) { searchContainer.setVisible(false); searchContainer.setManaged(false); }
        if (scheduleControls != null) { scheduleControls.setVisible(false); scheduleControls.setManaged(false); }

        // 3. Show Profile Section
        if (profileSection != null) {
            profileSection.setVisible(true);
            profileSection.setManaged(true);
            // Note: FXML mein alignment TOP_CENTER rakhi gayi hai alignment issue fix karne ke liye
        }
        
        // 4. Update Header & Fields
        txtHeader.setText("Manager Account Profile");
        nameField.setText(current.getFullName());
        emailField.setText(current.getEmail());
        
        // Passwords clear karein safety ke liye
        if (newPassField != null) newPassField.clear();
        if (confirmPassField != null) confirmPassField.clear();

        // 5. Image Loading Logic (Fixing malformed URL error)
        if (current.getProfilePic() != null && !current.getProfilePic().isEmpty()) {
            try {
                File file = new File(current.getProfilePic());
                if (file.exists()) {
                    // toURI() use karne se URL protocol ka error nahi aata
                    bigProfileImg.setImage(new Image(file.toURI().toString()));
                } else {
                    bigProfileImg.setImage(new Image("file:" + current.getProfilePic()));
                }
                bigProfileImg.setPreserveRatio(false); 
                bigProfileImg.setSmooth(true);
            } catch (Exception e) {
                System.err.println("Profile Image Load Error: " + e.getMessage());
            }
        }
    }    // --- Save Button Logic for Profile Section ---
    @FXML
    private void handleSaveProfile() {
        String newName = nameField.getText();
        String newEmail = emailField.getText();
        String pass = newPassField.getText();
        String confirm = confirmPassField.getText();

        // Password matching check
        if (!pass.isEmpty() && !pass.equals(confirm)) {
            showAlert("Error", "Passwords do not match!");
            return;
        }

        // Database Update
        DatabaseHandler db = new DatabaseHandler();
        String oldEmail = UserSession.getInstance().getUser().getEmail();
        
        // Is method ko DatabaseHandler mein hona chahiye
        boolean isUpdated = db.updateManagerProfile(newName, newEmail, oldEmail);
        
        if (isUpdated) {
            // Memory/Session update
            UserModel current = UserSession.getInstance().getUser();
            current.setFullName(newName);
            current.setEmail(newEmail);
            
            // Password update logic agar zaruri ho (Optional depending on your DB logic)
            if (!pass.isEmpty()) {
                updateProfileInDB(newName, newEmail, pass);
            }
            
            // Header UI update
            setUserInfo(newName, newEmail); 
            showAlert("Success", "Profile updated successfully!");
            
            // Wapis bookings par jane ke liye
            handleBookingsTab(); 
        } else {
            showAlert("Error", "Database update failed!");
        }
    }
    private void setActiveButton(Button activeBtn) {
        // Sirf wahi buttons list mein rakhen jo aapne upar declare kiye hain
        Button[] allButtons = {btnBookings, btnSchedules, btnAttendance, btnMaintenance};
        
        for (Button btn : allButtons) {
            if (btn == null) continue; // Safety check
            
            if (btn == activeBtn) {
                btn.setStyle("-fx-background-color: #38bdf8; -fx-text-fill: white; -fx-cursor: hand;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand;");
            }
        }
    }
    
    
    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        
        if (selectedFile != null) {
            // Path ko URI format mein lein UI display ke liye
            String imageURI = selectedFile.toURI().toString();
            Image img = new Image(imageURI);
            
            // --- UI Update (Dono images: Bari aur Choti) ---
            if (profileImageView != null) {
                profileImageView.setImage(img);
            }
            
            if (bigProfileImg != null) {
                bigProfileImg.setImage(img);
                
                // Scaling set karein taake circle pora bhary (From old logic)
                bigProfileImg.setFitWidth(140);
                bigProfileImg.setFitHeight(140);
                bigProfileImg.setPreserveRatio(false); 
            }

            // --- Data & Session Update ---
            // Absolute path save karein database ke liye
            String absolutePath = selectedFile.getAbsolutePath();
            
            // Session mein update karein taake refresh par image na urey
            if (UserSession.getInstance().getUser() != null) {
                UserSession.getInstance().getUser().setProfilePic(absolutePath);
            }

            // Database mein save karein (Donon methods ka combine logic)
            saveImagePathToDB(absolutePath); 
            // Agar aapke database handler ka naam alag hai toh:
            // db.updateProfilePic(absolutePath, UserSession.getInstance().getUser().getEmail());
        }
    }
    
    

    private void saveImagePathToDB(String path) {
        String sql = "UPDATE USERS SET PROFILE_PIC = ? WHERE EMAIL = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, path);
            pstmt.setString(2, UserSession.getInstance().getUser().getEmail());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateProfileInDB(String name, String email, String newPass) {
        String sql;
        boolean updatePass = !newPass.isEmpty();
        
        // Purana email (jo session mein hai) as a condition use hoga search ke liye
        String currentEmail = UserSession.getInstance().getUser().getEmail();

        if (updatePass) {
            sql = "UPDATE USERS SET FULL_NAME = ?, EMAIL = ?, PASSWORD_HASH = ? WHERE EMAIL = ?";
        } else {
            sql = "UPDATE USERS SET FULL_NAME = ?, EMAIL = ? WHERE EMAIL = ?";
        }

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            if (updatePass) {
                pstmt.setString(3, newPass);
                pstmt.setString(4, currentEmail);
            } else {
                pstmt.setString(3, currentEmail);
            }
            
            pstmt.executeUpdate();
            showAlert("Success", "Profile updated! Changes will be visible to Admin.");
            
        } catch (SQLException e) { e.printStackTrace(); }
    }

    
 // 3. showSection method agar missing hai to:
 	private void showSection(Node section, boolean visible) {
 	    if (section != null) {
 	        section.setVisible(visible);
 	        section.setManaged(visible);
 	    }
 	}
 	
    
 	
 	@FXML
 	private void handleBusSchedulesTab() {
 	    // Button state update
 	    setActiveButton(btnSchedules); // Updated to btnSchedules as per your note
 	    
 	    // 1. Tables toggle karein
 	    showSection(bookingsTable, false);
 	    showSection(scheduleTable, true);
 	    
 	    // ScrollPane ko bhi handle karein jo FXML mein hai
 	    if (scheduleScroll != null) {
 	        showSection(scheduleScroll, true);
 	    }

 	    // 2. Profile section hide karein
 	    showSection(profileSection, false);

 	    // 3. UI Controls switch karein
 	    showSection(bookingControls, false);
 	    showSection(scheduleControls, true);
 	    showSection(searchContainer, true);
 	    
 	    // Header text update
 	    txtHeader.setText("Bus Schedules (Admin Data)");

 	    // --- SABSE ZAROORI LINE ---
 	    loadBusSchedules(); 
 	}    @FXML
    private void handleBookingsTab() {
        // 1. Tables toggle karein
        bookingsTable.setVisible(true);
        bookingsTable.setManaged(true);
        scheduleTable.setVisible(false);
        scheduleTable.setManaged(false);

        // 2. Profile section ko mukammal hide karein (YEH ZAROORI HAI)
        profileSection.setVisible(false);
        profileSection.setManaged(false);

        // 3. UI Controls
        bookingControls.setVisible(true);
        bookingControls.setManaged(true);
        scheduleControls.setVisible(false);
        scheduleControls.setManaged(false);
        searchContainer.setVisible(true);
        searchContainer.setManaged(true);
        
        txtHeader.setText("Passenger Booking Requests");
    }
    
    
 	private void loadBusSchedules() {
 		scheduleTable.refresh();
 	    // Purana logic barkarar hai
 	    scheduleTable.setItems(busScheduleList);
 	    busScheduleList.clear();
 	    // Double check clear (jaisa aapne rakha tha)
 	    busScheduleList.clear(); 
 	    
 	    // TICKET_PRICE column query mein shamil hai
 	    String query = "SELECT BUS_NAME, BUS_NUMBER, SOURCE, DESTINATION, DEPARTURE_TIME, TRAVEL_DATE, TICKET_PRICE FROM BUSES ORDER BY TRAVEL_DATE ASC"; 
 	    
 	    try (Connection conn = DatabaseHandler.getConnection();
 	         Statement stmt = conn.createStatement();
 	         ResultSet rs = stmt.executeQuery(query)) {
 	        
 	        while (rs.next()) {
 	            // BusScheduleModel ka object purane parameters ke saath
 	            BusScheduleModel bsm = new BusScheduleModel(
 	                rs.getString("BUS_NAME"),
 	                rs.getString("SOURCE") + " to " + rs.getString("DESTINATION"),
 	                rs.getString("DEPARTURE_TIME"),
 	                rs.getString("TRAVEL_DATE")
 	            );
 	            
 	            // Bus Number set ho raha hai
 	            bsm.setBusNumber(rs.getString("BUS_NUMBER"));
 	            
 	            // --- NAYA ADDITION ---
 	            // Database se Ticket Price nikaal kar model mein set kar rahe hain
 	            double price = rs.getDouble("TICKET_PRICE");
 	            bsm.setTicketPrice(price);
 	            // ---------------------
 	            
 	            busScheduleList.add(bsm);
 	        }
 	    } catch (Exception e) { 
 	        System.out.println("Schedule Load Error: " + e.getMessage());
 	        e.printStackTrace(); 
 	    }
 	}
    private void setupScheduleColumns() {
        if (colBusSched != null) colBusSched.setCellValueFactory(new PropertyValueFactory<>("busName"));
        if (colRouteSched != null) colRouteSched.setCellValueFactory(new PropertyValueFactory<>("route"));
        if (colTimeSched != null) colTimeSched.setCellValueFactory(new PropertyValueFactory<>("timing"));
        if (colDateSched != null) colDateSched.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        if (colBusPlate != null) colBusPlate.setCellValueFactory(new PropertyValueFactory<>("busNumber"));
        if (colPrice != null) colPrice.setCellValueFactory(new PropertyValueFactory<>("ticketPrice"));
    }

    private void openProofWindow(BookingModel selected) {
        if (selected == null || selected.getPaymentProofPath() == null) {
            showAlert("Error", "Is booking ka koi payment proof maujood nahi hai!");
            return;
        }

        try {
            File file = new File(selected.getPaymentProofPath());
            if (file.exists()) {
                Stage proofStage = new Stage();
                proofStage.setTitle("Payment Proof - " + selected.getPassengerName());

                Image image = new Image(file.toURI().toString());
                ImageView imageView = new ImageView(image);
                
                imageView.setFitWidth(500);
                imageView.setPreserveRatio(true);

                StackPane layout = new StackPane(imageView);
                layout.setStyle("-fx-background-color: #0f172a; -fx-padding: 20;");
                
                Scene scene = new Scene(layout);
                proofStage.setScene(scene);
                proofStage.show();
            } else {
                showAlert("File Not Found", "Screenshot file system mein nahi mili: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Image load karne mein masla aya.");
        }
    }

    @FXML
    private void handleViewProof(ActionEvent event) {
        BookingModel selected = bookingsTable.getSelectionModel().getSelectedItem();
        openProofWindow(selected);
    }

    @FXML
    private void handlePrintTicket() {
        BookingModel selected = bookingsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Selection Error", "Pehle list se aik booking select karein!");
            return;
        }

        if (!"Approved".equalsIgnoreCase(selected.getStatus()) && !"CONFIRMED".equalsIgnoreCase(selected.getStatus())) {
            showAlert("Access Denied", "Sirf Approved bookings ka ticket print kiya ja sakta hai.");
            return;
        }

        try {
            String fileName = "Ticket_" + selected.getPassengerName().replace(" ", "_") + ".txt";
            File file = new File(fileName);
            PrintWriter writer = new PrintWriter(file);

            writer.println("========================================");
            writer.println("         BUS MANAGEMENT SYSTEM          ");
            writer.println("========================================");
            writer.println("Passenger: " + selected.getPassengerName());
            writer.println("CNIC:      " + selected.getPassengerCNIC());
            writer.println("Bus:       " + selected.getBusName());
            writer.println("Route:     " + selected.getRoute());
            writer.println("Seat #:    " + selected.getSeatNumber());
            writer.println("Status:    " + selected.getStatus());
            writer.println("========================================");
            writer.println("   Wish you a happy and safe journey!   ");
            writer.println("========================================");
            writer.close();

            showAlert("Success", "Ticket Print ho gaya: " + fileName);
        } catch (Exception e) {
            showAlert("Error", "Ticket banane mein masla aya.");
        }
    }

    @FXML
    private void handleDeleteBooking() {
        BookingModel selected = bookingsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Selection Error", "Delete karne ke liye booking select karein!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Booking");
        alert.setHeaderText("Kya aap waqai ye booking delete karna chahte hain?");
        alert.setContentText("Passenger: " + selected.getPassengerName());

        if (alert.showAndWait().get() == ButtonType.OK) {
            deleteFromDatabase(selected);
        }
    }

    private void deleteFromDatabase(BookingModel booking) {
        String query = "DELETE FROM BOOKINGS WHERE PASSENGER_CNIC = ? AND BUS_NAME = ?";
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, booking.getPassengerCNIC());
            pstmt.setString(2, booking.getBusName());
            
            int result = pstmt.executeUpdate();
            if (result > 0) {
                bookingsTable.getItems().remove(booking); 
                showAlert("Deleted", "Booking record kamyabi se delete ho gaya.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void showSeatMap(String busNum, String date) {
        if (seatMapContainer == null) return;
        seatMapContainer.getChildren().clear();
        
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(10));

        ObservableList<Integer> bookedSeats = getBookedSeatsList(busNum, date);

        for (int i = 1; i <= 45; i++) {
            CheckBox seatBox = new CheckBox(String.valueOf(i));
            seatBox.setPrefSize(50, 30);
            
            if (bookedSeats.contains(i)) {
                seatBox.setSelected(true);
                seatBox.setDisable(true);
                seatBox.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 5;"); 
            } else {
                seatBox.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 5;");
            }
            
            int row = (i - 1) / 5;
            int col = (i - 1) % 5; grid.add(seatBox, col, row); } seatMapContainer.getChildren().add(grid); }

    private ObservableList<Integer> getBookedSeatsList(String busNum, String date) {
        ObservableList<Integer> seats = FXCollections.observableArrayList();
        String query = "SELECT SEAT_NUMBER FROM BOOKINGS WHERE (BUS_NUMBER = ? OR BUS_NAME = ?) AND TRAVEL_DATE = ? AND STATUS != 'Rejected'";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, busNum);
            pstmt.setString(2, busNum);
            pstmt.setString(3, date);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) seats.add(rs.getInt("SEAT_NUMBER"));
        } catch (SQLException e) { e.printStackTrace(); }
        return seats;
    }

    private void setupColumns() {
        colPassenger.setCellValueFactory(new PropertyValueFactory<>("passengerName"));
        colBus.setCellValueFactory(new PropertyValueFactory<>("busName"));
        colRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (colCNIC != null) colCNIC.setCellValueFactory(new PropertyValueFactory<>("passengerCNIC"));
        if (colSeat != null) colSeat.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
    }

    private void setupPaymentProofColumn() {
        if (colPaymentProof == null) return;
        colPaymentProof.setCellFactory(column -> new TableCell<BookingModel, String>() {
            private final Button viewBtn = new Button("View");
            private final CheckBox tickBox = new CheckBox();
            private final HBox layout = new HBox(10, viewBtn, tickBox);
            {
                layout.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color: #38bdf8; -fx-text-fill: white; -fx-cursor: hand;");
            }

            @Override
            protected void updateItem(String filePath, boolean empty) {
                super.updateItem(filePath, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(layout);
                    BookingModel current = getTableRow().getItem();
                    
                    boolean isVerified = "VERIFIED".equalsIgnoreCase(current.getPaymentStatus()) || "PAID".equalsIgnoreCase(current.getPaymentStatus());
                    tickBox.setSelected(isVerified);
                    tickBox.setDisable(isVerified);

                    viewBtn.setOnAction(e -> openProofWindow(current));

                    tickBox.setOnAction(e -> {
                        if (tickBox.isSelected()) {
                            updatePaymentInDB(current);
                            tickBox.setDisable(true);
                        }
                    });
                }
            }
        });
    }

    private void updatePaymentInDB(BookingModel booking) {
        TextInputDialog priceDialog = new TextInputDialog("0");
        priceDialog.setTitle("Final Price Confirmation");
        priceDialog.setHeaderText("Passenger: " + booking.getPassengerName());
        priceDialog.setContentText("Enter Ticket Price for Passenger:");

        priceDialog.showAndWait().ifPresent(priceStr -> {
            try {
                double finalPrice = Double.parseDouble(priceStr);
                String sql = "UPDATE BOOKINGS SET PAYMENT_STATUS = 'VERIFIED', STATUS = 'CONFIRMED', FINAL_PRICE = ? WHERE PASSENGER_EMAIL = ? AND SEAT_NUMBER = ?";
                
                try (Connection conn = DatabaseHandler.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setDouble(1, finalPrice);
                    pstmt.setString(2, booking.getPassengerEmail());
                    pstmt.setInt(3, booking.getSeatNumber());
                    pstmt.executeUpdate();
                    
                    showAlert("Success", "Booking confirmed with Price: " + finalPrice);
                    loadBookings(); 
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid numeric price.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadBookings() {
        bookingList.clear();
        String loggedInManager = UserSession.getName(); 
        String query = "SELECT * FROM BOOKINGS WHERE ASSIGNED_MANAGER = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, loggedInManager);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String src = rs.getString("SOURCE");
                String dest = rs.getString("DESTINATION");
                BookingModel bm = new BookingModel(
                    rs.getString("PASSENGER_NAME"), rs.getString("BUS_NAME"),
                    (src + " to " + dest), rs.getString("PHONE"),
                    rs.getString("STATUS"), rs.getString("PASSENGER_CNIC"),
                    rs.getInt("SEAT_NUMBER"), rs.getString("PAYMENT_STATUS")
                );
                bm.setPassengerEmail(rs.getString("PASSENGER_EMAIL"));
                bm.setPaymentProofPath(rs.getString("PAYMENT_PROOF_PATH"));
                bookingList.add(bm);
            }
            bookingsTable.setItems(bookingList);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleApprove() {
        BookingModel selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a booking to approve!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getSeatNumber()));
        dialog.setTitle("Confirm Seat");
        dialog.setHeaderText("Passenger: " + selected.getPassengerName());
        dialog.setContentText("Verify or Change Seat Number (1-45):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int finalSeat = Integer.parseInt(result.get());
                updateBookingStatus(selected, "Approved", finalSeat);
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid seat number.");
            }
        }
    }

    @FXML private void handleReject() { updateStatus("Rejected"); }

    private void updateBookingStatus(BookingModel booking, String status, int seatNum) {
        String query = "UPDATE BOOKINGS SET STATUS = ?, SEAT_NUMBER = ? WHERE PASSENGER_CNIC = ? AND BUS_NAME = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, seatNum);
            pstmt.setString(3, booking.getPassengerCNIC()); 
            pstmt.setString(4, booking.getBusName());
            pstmt.executeUpdate();
            showAlert("Success", "Booking " + status + " with Seat #" + seatNum);
            loadBookings(); 
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateStatus(String status) {
        BookingModel selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "UPDATE BOOKINGS SET STATUS = ? WHERE PASSENGER_NAME = ? AND PASSENGER_CNIC = ?";
            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, status);
                pstmt.setString(2, selected.getPassengerName());
                pstmt.setString(3, selected.getPassengerCNIC());
                pstmt.executeUpdate();
                loadBookings();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML 
    private void handlePrint(ActionEvent event) {
        BookingModel selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected != null && ("VERIFIED".equalsIgnoreCase(selected.getPaymentStatus()) || "PAID".equalsIgnoreCase(selected.getPaymentStatus()))) {
            String fileName = "Ticket_" + selected.getPassengerName().replace(" ", "_") + ".txt";
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write("Passenger: " + selected.getPassengerName() + "\nBus: " + selected.getBusName() + "\nSeat: " + selected.getSeatNumber());
                showAlert("Success", "Ticket Printed: " + fileName);
            } catch (IOException e) { e.printStackTrace(); }
        } else {
            showAlert("Error", "Payment not verified yet.");
        }
    }

    public void setUserInfo(String fullName, String email) {
        if (lblFullName != null) lblFullName.setText("Welcome, " + fullName); 
        if (lblShortEmail != null && email != null) lblShortEmail.setText(email);
    }

    @FXML private void handleLogout(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("RoleSelection.fxml"));
            stage.setScene(new Scene(root, 800, 500));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleUpdatePayment() {
        BusScheduleModel selectedBus = scheduleTable.getSelectionModel().getSelectedItem();
        if (selectedBus == null) {
            showAlert("Selection Required", "Pehle table se wo bus select karein jiski price update karni hai.");
            return;
        }

        double originalAdminPrice = 0;
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT ADMIN_PRICE FROM BUSES WHERE BUS_NUMBER = ?")) {
            pstmt.setString(1, selectedBus.getBusNumber());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                originalAdminPrice = rs.getDouble("ADMIN_PRICE");
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedBus.getTicketPrice()));
        dialog.setTitle("Update Ticket Price");
        dialog.setHeaderText("Original Admin Price: " + originalAdminPrice + "\nBus: " + selectedBus.getBusName());
        dialog.setContentText("Enter New Final Price:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double newPrice = Double.parseDouble(result.get());

                if (newPrice < originalAdminPrice) {
                    showAlert("Error", "Aap asli price (" + originalAdminPrice + ") se kam price nahi rakh sakte!");
                } else {
                    updateBusPriceInDB(selectedBus.getBusNumber(), newPrice);
                    selectedBus.setTicketPrice(newPrice);
                    scheduleTable.refresh();
                    showAlert("Success", "Price kamyabi se update ho gayi!");
                }
            } catch (NumberFormatException e) {
                showAlert("Input Error", "Baraye meharbani sirf valid numbers enter karein.");
            }
        }
    }

    private void updateBusPriceInDB(String busNumber, double price) {
        String sql = "UPDATE BUSES SET TICKET_PRICE = ? WHERE BUS_NUMBER = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, price);
            pstmt.setString(2, busNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}