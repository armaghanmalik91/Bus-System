package Application;

import DataBase.DatabaseHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class RegisterController {

    @FXML private TextField nameField, emailField, passFieldVisible, confirmPassFieldVisible;
    @FXML private PasswordField passField, confirmPassField;
    @FXML private CheckBox showPassTick;
    @FXML private Label errorLabel;
    @FXML private ComboBox<CountryData> countryCodePicker;
    @FXML private TextField phoneField;

    public static class CountryData {
        private final String flag, name, code, iso;
        private final int length;
        public CountryData(String flag, String iso, String name, String code, int length) {
            this.flag = flag; this.iso = iso; this.name = name; this.code = code; this.length = length;
        }
        @Override public String toString() { return iso + " " + code; }
        public String getFullCode() { return code; }
        public int getLength() { return length; }
        public String getName() { return name; }
    }

    @FXML
    public void initialize() {
        setupCountryPicker();
        countryCodePicker.setPrefWidth(100);
        countryCodePicker.setMinWidth(100);
        countryCodePicker.setMaxWidth(100);
        countryCodePicker.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) { phoneField.clear(); applyPhoneLimit(newVal.getLength()); }
        });
        applyPhoneLimit(10);
    }

    private void setupCountryPicker() {
        ObservableList<CountryData> countries = FXCollections.observableArrayList(
            new CountryData("🇵🇰", "PK", "Pakistan", "+92", 10),
            new CountryData("🇮🇳", "IN", "India", "+91", 10),
            new CountryData("🇺🇸", "US", "USA", "+1", 10),
            new CountryData("🇬🇧", "GB", "UK", "+44", 10),
            new CountryData("🇦🇪", "AE", "UAE", "+971", 9),
            new CountryData("🇸🇦", "SA", "Saudi Arabia", "+966", 9),
            new CountryData("🇨🇦", "CA", "Canada", "+1", 10),
            new CountryData("🇦🇺", "AU", "Australia", "+61", 9),
            new CountryData("🇩🇪", "DE", "Germany", "+49", 11),
            new CountryData("🇫🇷", "FR", "France", "+33", 9),
            new CountryData("🇹🇷", "TR", "Turkey", "+90", 10),
            new CountryData("🇨🇳", "CN", "China", "+86", 11),
            new CountryData("🇧🇩", "BD", "Bangladesh", "+880", 10),
            new CountryData("🇮🇩", "ID", "Indonesia", "+62", 11),
            new CountryData("🇲🇾", "MY", "Malaysia", "+60", 9),
            new CountryData("🇳🇬", "NG", "Nigeria", "+234", 10),
            new CountryData("🇧🇷", "BR", "Brazil", "+55", 11),
            new CountryData("🇷🇺", "RU", "Russia", "+7", 10),
            new CountryData("🇯🇵", "JP", "Japan", "+81", 10),
            new CountryData("🇮🇹", "IT", "Italy", "+39", 10),
            new CountryData("🇿🇦", "ZA", "South Africa", "+27", 9),
            new CountryData("🇲🇽", "MX", "Mexico", "+52", 10),
            new CountryData("🇪🇬", "EG", "Egypt", "+20", 10),
            new CountryData("🇮🇷", "IR", "Iran", "+98", 10)
        );
        countryCodePicker.setItems(countries);
        countryCodePicker.getSelectionModel().selectFirst();
        countryCodePicker.setCellFactory(new Callback<ListView<CountryData>, ListCell<CountryData>>() {
            @Override public ListCell<CountryData> call(ListView<CountryData> param) {
                return new ListCell<CountryData>() {
                    @Override protected void updateItem(CountryData item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) { setText(null); } 
                        else { setText(item.flag + " " + item.name + " (" + item.code + ")"); }
                    }
                };
            }
        });
    }

    private void applyPhoneLimit(int limit) {
        phoneField.setTextFormatter(new TextFormatter<>(change ->
            (change.getControlNewText().length() <= limit && change.getControlNewText().matches("\\d*")) ? change : null));
        phoneField.setPromptText(limit + " digits required");
    }

    // FIXED: Proper stage sizing and centering
    private void switchScene(ActionEvent event, String fxml, int w, int h) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            Scene scene = new Scene(root, w, h);
            
            stage.setScene(scene);
            stage.setWidth(w);
            stage.setHeight(h);
            stage.setResizable(false);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goToLogin(ActionEvent event) { switchScene(event, "Login.fxml", 450, 625); }
    @FXML private void goBack(ActionEvent event) { switchScene(event, "RoleSelection.fxml", 800, 500); }

    @FXML
    private void handleRegister(ActionEvent event) {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passField.isVisible() ? passField.getText() : passFieldVisible.getText();
        String confirm = confirmPassField.isVisible() ? confirmPassField.getText() : confirmPassFieldVisible.getText();
        CountryData selectedCountry = countryCodePicker.getValue();
        String phoneInput = phoneField.getText();
        String fullPhone = selectedCountry.getFullCode() + phoneInput;

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phoneInput.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Error", "All fields are required!");
            return;
        }
        if (phoneInput.length() != selectedCountry.getLength()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Number", "Requires exactly " + selectedCountry.getLength() + " digits.");
            return;
        }
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Email", "Enter a valid email.");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("❌ Passwords mismatch!");
            return;
        }
        if (isUserAlreadyRegistered(email, fullPhone)) return;

        String insertSQL = "INSERT INTO USERS (FULL_NAME, EMAIL, PASSWORD_HASH, PHONE, ROLE) VALUES (?, ?, ?, ?, 'Passenger')";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, fullPhone);
            pstmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Registration Success", "Account created! Please Login.");
            goToLogin(event);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not complete registration.");
        }
    }

    private boolean isUserAlreadyRegistered(String email, String phone) {
        String query = "SELECT EMAIL, PHONE FROM USERS WHERE EMAIL = ? OR PHONE = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email); pstmt.setString(2, phone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getString("EMAIL").equalsIgnoreCase(email)) showAlert(Alert.AlertType.ERROR, "Duplicate", "Email registered.");
                else showAlert(Alert.AlertType.ERROR, "Duplicate", "Phone registered.");
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private boolean isValidEmail(String email) { return email.matches("^[A-Za-z0-9+_.-]+@(.+)$"); }

    @FXML
    private void checkPasswords(KeyEvent event) {
        String p1 = passField.isVisible() ? passField.getText() : passFieldVisible.getText();
        String p2 = confirmPassField.isVisible() ? confirmPassField.getText() : confirmPassFieldVisible.getText();
        if (p2.isEmpty()) errorLabel.setText("");
        else if (!p1.equals(p2)) {
            errorLabel.setText("❌ Passwords do not match!");
            errorLabel.setStyle("-fx-text-fill: #ff4d4d; -fx-font-weight: bold;");
        } else {
            errorLabel.setText("✅ Passwords match");
            errorLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        }
    }

    @FXML private void togglePassword() {
        boolean show = showPassTick.isSelected();
        handleToggle(passField, passFieldVisible, show);
        handleToggle(confirmPassField, confirmPassFieldVisible, show);
    }

    private void handleToggle(PasswordField pf, TextField tf, boolean show) {
        if (show) {
            tf.setText(pf.getText()); tf.setVisible(true); tf.setManaged(true);
            pf.setVisible(false); pf.setManaged(false);
        } else {
            pf.setText(tf.getText()); pf.setVisible(true); pf.setManaged(true);
            tf.setVisible(false); tf.setManaged(false);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null);
        alert.setContentText(message); alert.showAndWait();
    }

    @FXML private void handleForgotPassword() {
        showAlert(Alert.AlertType.INFORMATION, "Notice", "Email service is being configured.");
    }
}