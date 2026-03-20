package Application;

import DataBase.DatabaseHandler;
import javafx.scene.control.Alert;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane; 
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private StackPane contentArea; 

    private String loggedInUserName;

    @FXML
    public void initialize() {
        // Date format set karke left menu label mein dalna
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (dateLabel != null) {
            dateLabel.setText("Today: " + dtf.format(LocalDateTime.now()));
        }

        // --- FIXED: DashboardHome ki jagah sahi file load karein ---
        // Agar aapki file ka naam 'PassengerDashboard.fxml' hai toh yahan "PassengerDashboard" likhein
        loadPage("PassengerDashboard"); 
    }

    public void setUserContext(String name) {
        this.loggedInUserName = name;
        if (welcomeLabel != null) welcomeLabel.setText("Welcome, " + name);
    }

    // --- UPDATED METHOD (Error Fix Logic) ---
    private void loadPage(String fxmlFile) {
        if (contentArea == null) {
            System.out.println("Error: contentArea (StackPane) null hai! FXML check karein.");
            return;
        }
        try {
            // Resource path handling with fallback
            URL fileUrl = getClass().getResource("/Application/" + fxmlFile + ".fxml");
            
            // Agar resource path work na kare toh direct string use karein
            if (fileUrl == null) {
                fileUrl = getClass().getResource(fxmlFile + ".fxml");
            }
            
            if (fileUrl == null) {
                System.err.println("Error: FXML file '" + fxmlFile + ".fxml' nahi mili! Check karein ke file folder mein hai ya nahi.");
                return;
            }

            Parent root = FXMLLoader.load(fileUrl);
            contentArea.getChildren().setAll(root); // Purani nodes remove karke nayi set karna
            
        } catch (IOException e) {
            System.err.println("FXML Load Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void sendBookingToManager(String busName, String route, String phone, String cnic) {
        String query = "INSERT INTO BOOKINGS (PASSENGER_NAME, PASSENGER_CNIC, BUS_NAME, ROUTE, PHONE, STATUS) VALUES (?, ?, ?, ?, ?, 'Pending')";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, loggedInUserName);
            pstmt.setString(2, cnic);
            pstmt.setString(3, busName);
            pstmt.setString(4, route);
            pstmt.setString(5, phone);

            pstmt.executeUpdate();
            System.out.println("Booking Request Sent to Manager!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBookTicket(ActionEvent event) {
        if (contentArea != null) {
            loadPage("BookTicket");
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("BookTicket.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (Exception e) {
                showError("Cannot load Booking Screen", e.getMessage());
            }
        }
    }

    @FXML private void handleLiveTracking(ActionEvent event) { System.out.println("Opening Map..."); }
    @FXML private void handleWallet(ActionEvent event) { System.out.println("Opening Wallet..."); }
    @FXML private void handleSupport(ActionEvent event) { System.out.println("Opening Support..."); }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 450, 625);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}