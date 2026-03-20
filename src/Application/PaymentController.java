package Application;

import DataBase.DatabaseHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PaymentController {

    @FXML private Label adminDetailsLabel;
    @FXML private TextField transactionIdField;

    // Isme hum wo booking save rakhenge jiski payment ho rahi hai
    private BookingModel selectedBooking;

    @FXML
    public void initialize() {
        // Admin details jo aapne kaha tha ke show hon
        adminDetailsLabel.setText("Admin Account: 0123-456789-01 (Bank Alfalah)\nEasyPaisa: 0300-1234567");
    }

    // Booking ka data lene ke liye method
    public void setBookingData(BookingModel booking) {
        this.selectedBooking = booking;
    }

    @FXML
    private void handleConfirmPayment(ActionEvent event) {
        if (transactionIdField.getText().isEmpty()) {
            showAlert("Error", "Please enter Transaction ID provided by Admin.");
            return;
        }

        // Database Update Logic
        String query = "UPDATE BOOKINGS SET PAYMENT_STATUS = 'PAID' WHERE PASSENGER_CNIC = ? AND SEAT_NUMBER = ?";
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, selectedBooking.getPassengerCNIC());
            pstmt.setInt(2, selectedBooking.getSeatNumber());
            pstmt.executeUpdate();
            
            showAlert("Payment Successful", "Payment has been verified. Redirecting to My Bookings...");
            
            // Redirect back to My Bookings
            redirectToBookings(event);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void redirectToBookings(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BookTicket.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }

    @FXML
    private void handleCancel(ActionEvent event) throws IOException {
        redirectToBookings(event);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}