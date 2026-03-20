package Application;

import DataBase.DatabaseHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class LoginController {

    @FXML private TextField emailField, passFieldVisible;
    @FXML private PasswordField passField;
    @FXML private CheckBox showPassTick;
    @FXML private Text loginTitle; 
    @FXML private VBox registerSection; 

    // --- INTEGRATED: Target role tracking ---
    private String currentTargetRole; // Isme "Passenger", "Manager", ya "Admin" set hota hai

    public void setupLoginType(String role) {
        this.currentTargetRole = role; // Role ko save karein taake login ke waqt check ho sake
        
        if (role.equalsIgnoreCase("Admin")) {
            loginTitle.setText("ADMIN LOGIN");
            registerSection.setVisible(false); 
            registerSection.setManaged(false);
        } else if (role.equalsIgnoreCase("Manager")) {
            loginTitle.setText("MANAGER LOGIN");
            registerSection.setVisible(false); 
            registerSection.setManaged(false);
        } else {
            loginTitle.setText("PASSENGER LOGIN");
            registerSection.setVisible(true);
            registerSection.setManaged(true);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = showPassTick.isSelected() ? passFieldVisible.getText() : passField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Login Error", "Please fill in all fields.");
            return;
        }

        // --- INTEGRATED ROLE VALIDATION: DatabaseHandler ka naya method use karein ---
        DatabaseHandler dbHandler = new DatabaseHandler();
        
        // Pehle check karein ke kya credentials aur role teeno sahi hain
        if (!dbHandler.validateLogin(email, password, currentTargetRole)) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "Aap is page se login nahi kar sakte! (Invalid credentials for " + currentTargetRole + ")");
            return;
        }

        // Agar validateLogin true hai, tabhi aage ka data fetch karein
        String query = "SELECT FULL_NAME, EMAIL, ROLE, ACCOUNT_STATUS, PROFILE_PIC FROM USERS WHERE EMAIL = ? AND ROLE = ?";
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, currentTargetRole);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("ACCOUNT_STATUS");
                String role = rs.getString("ROLE");
                String userName = rs.getString("FULL_NAME");
                String userEmail = rs.getString("EMAIL");
                String profilePic = rs.getString("PROFILE_PIC");

                if ("DELETED".equals(status) || "BLOCKED".equals(status)) {
                    showAlert(Alert.AlertType.ERROR, "Access Denied", "Your account is inactive. Contact Admin.");
                    return;
                }

                // --- UserModel aur Session setting ---
                UserModel loggedInUser = new UserModel(userName, userEmail, role);
                loggedInUser.setProfilePic(profilePic); 
                
                UserSession.getInstance().setUser(loggedInUser);
                UserSession.setSession(userEmail, userName, role);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                String fxmlFile = "";
                int width = 1100; 
                int height = 700;

                // Dashboard selection based on role
                if ("Admin".equalsIgnoreCase(role)) {
                    fxmlFile = "AdminDashboard.fxml";
                    width = 1300;  
                    height = 600;  
                } else if ("Manager".equalsIgnoreCase(role)) {
                    fxmlFile = "ManagerDashboard.fxml";
                    width = 1250;  
                    height = 600;
                } else {
                    fxmlFile = "Dashboard.fxml"; 
                    width = 1100;
                    height = 700;
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent root = loader.load();

                // Controller initialization
                if ("Manager".equalsIgnoreCase(role)) {
                    ManagerController mc = loader.getController();
                    mc.setUserInfo(userName, userEmail);
                }

                Scene scene = new Scene(root, width, height);
                stage.setScene(scene);
                stage.setWidth(width);
                stage.setHeight(height);
                stage.centerOnScreen();
                stage.show();
                
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "System Error", "Error loading dashboard.");
        }
    }

    @FXML
    private void togglePassword() {
        if (showPassTick.isSelected()) {
            passFieldVisible.setText(passField.getText());
            passFieldVisible.setVisible(true);
            passFieldVisible.setManaged(true);
            passField.setVisible(false);
            passField.setManaged(false);
        } else {
            passField.setText(passFieldVisible.getText());
            passField.setVisible(true);
            passField.setManaged(true);
            passFieldVisible.setVisible(false);
            passFieldVisible.setManaged(false);
        }
    }

    @FXML private void handleForgotPassword() {
        showAlert(Alert.AlertType.INFORMATION, "Notice", "Email service is being configured.");
    }

    @FXML private void goToRegister(ActionEvent event) { switchScene(event, "Register.fxml", 450, 625); }

    @FXML private void goBack(ActionEvent event) { switchScene(event, "RoleSelection.fxml", 800, 500); }

    private void switchScene(ActionEvent event, String fxml, int w, int h) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            Scene scene = new Scene(root, w, h);
            stage.setScene(scene);
            stage.setWidth(w);
            stage.setHeight(h);
            stage.sizeToScene(); 
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}