package Application;

import DataBase.DatabaseHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox; 
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class AdminController {
    // Sidebar Buttons
    @FXML private Button btnManageManagers, btnManageBuses, btnComplaints;
    
    // Manager FXML IDs
    @FXML private TextField mName, mEmail, mPass, mPhone, searchField;
    @FXML private TableView<UserModel> managerTable; 
    @FXML private TableColumn<UserModel, Void> colSNo; 
    @FXML private TableColumn<UserModel, String> colName, colEmail, colPhone, colStatus;
    @FXML private TableColumn<UserModel, Void> colAction;
    
    // Panels
    @FXML private VBox managerPanel, busPanel, complaintsPanel;   
    
    // Bus FXML IDs
    @FXML private TextField busNameField, sourceField, destField, priceField, busNumberField;
    @FXML private ComboBox<String> busTypeCombo; 
    @FXML private DatePicker datePicker; 
    @FXML private Button addBusBtn; 
    @FXML private Button updateBusBtn; 
    @FXML private Spinner<Integer> hourSpinner, minuteSpinner;
    @FXML private ComboBox<String> amPmCombo;
    @FXML private TableView<BusScheduleModel> adminBusTable;
    @FXML private TableColumn<BusScheduleModel, String> colBus, colRoute, colTime, colDate;
    @FXML private TableColumn<BusScheduleModel, Double> colPrice; 
    @FXML private TableColumn<BusScheduleModel, Void> colBusAction; 
    @FXML private TextField busSearchField; 

    private ObservableList<UserModel> masterData = FXCollections.observableArrayList();
    private ObservableList<BusScheduleModel> adminBusList = FXCollections.observableArrayList();
    private FilteredList<BusScheduleModel> filteredBuses; 
    private boolean isEditMode = false; 

    public void initialize() {
        if (hourSpinner != null && minuteSpinner != null && amPmCombo != null) {
            hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 12));
            minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
            amPmCombo.setItems(FXCollections.observableArrayList("AM", "PM"));
            amPmCombo.setValue("AM");
        }
        
        setupSerialColumn();
        setupTableMappings();
        setupActionButtons();
        loadManagers();
        setupBusColumns();
        loadBusData();
        setupBusTableSelection();
        setupBusActionButtons();

        if (busTypeCombo != null) busTypeCombo.setItems(FXCollections.observableArrayList("Normal", "Sleeper", "Luxury"));
        if (searchField != null) setupSearch();

        filteredBuses = new FilteredList<>(adminBusList, p -> true);
        if (busSearchField != null) {
            busSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredBuses.setPredicate(bus -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    return bus.getBusName().toLowerCase().contains(newValue.toLowerCase());
                });
            });
        }
        adminBusTable.setItems(filteredBuses);
        
        showManagerPanel(); 
    }

    // --- Duplicate Bus Check Logic ---
    private boolean isDuplicateBus(String num, String name, String city) {
        String query = "SELECT COUNT(*) FROM BUSES WHERE BUS_NUMBER=? AND BUS_NAME=? AND SOURCE=?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, num);
            pstmt.setString(2, name);
            pstmt.setString(3, city);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // --- Sidebar Style Logic ---
    private void updateSidebarStyles(Button activeBtn) {
        Button[] buttons = {btnManageManagers, btnManageBuses, btnComplaints};
        for (Button btn : buttons) {
            if (btn != null) {
                if (btn == activeBtn) {
                    btn.setStyle("-fx-background-color: #38bdf8; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 10;");
                } else {
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-padding: 10;");
                }
            }
        }
    }

    private void updateViewVisibility(VBox activePanel) {
        VBox[] allPanels = {managerPanel, busPanel, complaintsPanel};
        for (VBox p : allPanels) {
            if (p != null) {
                boolean isCurrent = (p == activePanel);
                p.setVisible(isCurrent); 
                p.setManaged(isCurrent); 
            }
        }
    }

    @FXML private void showManagerPanel() { updateViewVisibility(managerPanel); updateSidebarStyles(btnManageManagers); }
    @FXML private void showBusesPanel() { updateViewVisibility(busPanel); loadBusData(); updateSidebarStyles(btnManageBuses); }
    @FXML private void showComplaintsPanel() { updateViewVisibility(complaintsPanel); updateSidebarStyles(btnComplaints); showAlert("Info", "Complaints panel loaded."); }
    @FXML private void handleManageBuses(ActionEvent event) { showBusesPanel(); }

    private void setupBusColumns() {
        if (colBus != null) colBus.setCellValueFactory(new PropertyValueFactory<>("busName"));
        if (colRoute != null) colRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        if (colTime != null) colTime.setCellValueFactory(new PropertyValueFactory<>("timing"));
        if (colDate != null) colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        if (colPrice != null) colPrice.setCellValueFactory(new PropertyValueFactory<>("ticketPrice"));
    }

    private void setupBusTableSelection() {
        if (adminBusTable != null) {
            adminBusTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    isEditMode = true;
                    busNameField.setText(newSelection.getBusName());
                    busNumberField.setText(newSelection.getBusNumber());
                    String[] routes = newSelection.getRoute().split(" to ");
                    if (routes.length == 2) {
                        sourceField.setText(routes[0]);
                        destField.setText(routes[1]);
                    }
                    try {
                        String[] timeParts = newSelection.getTiming().split("[: ]");
                        hourSpinner.getValueFactory().setValue(Integer.parseInt(timeParts[0]));
                        minuteSpinner.getValueFactory().setValue(Integer.parseInt(timeParts[1]));
                        amPmCombo.setValue(timeParts[2]);
                    } catch (Exception e) {}
                    if (newSelection.getDate() != null && !newSelection.getDate().equals("N/A")) {
                        datePicker.setValue(LocalDate.parse(newSelection.getDate()));
                    }
                    if (priceField != null) priceField.setText(String.valueOf(newSelection.getTicketPrice()));
                }
            });
        }
    }

    private void setupBusActionButtons() {
        if (colBusAction == null) return;
        colBusAction.setCellFactory(param -> new TableCell<BusScheduleModel, Void>() {
            private final Button editBtn = new Button("Edit");
            {
                editBtn.setStyle("-fx-background-color: #38bdf8; -fx-text-fill: white; -fx-cursor: hand;");
                editBtn.setOnAction(event -> {
                    BusScheduleModel selectedBus = getTableView().getItems().get(getIndex());
                    isEditMode = true;
                    busNameField.setText(selectedBus.getBusName());
                    busNumberField.setText(selectedBus.getBusNumber());
                    String[] routes = selectedBus.getRoute().split(" to ");
                    if (routes.length == 2) {
                        sourceField.setText(routes[0]);
                        destField.setText(routes[1]);
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : editBtn); }
        });
    }

    @FXML
    private void handleAddBus() {
        if (busNameField == null || busNumberField == null) return;
        String busNum = busNumberField.getText().toUpperCase();
        String bName = busNameField.getText();
        String source = sourceField.getText();
        String bDate = (datePicker.getValue() != null) ? datePicker.getValue().toString() : "";
        String bTime = hourSpinner.getValue() + ":" + String.format("%02d", minuteSpinner.getValue()) + " " + amPmCombo.getValue();
        
        if (!busNum.matches(".*[A-Z].*")) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Bus Number mein shehar ka code hona lazmi hai!");
            return;
        }

        if (!isEditMode && isDuplicateBus(busNum, bName, source)) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Error", "Is shehar mein is company ki ye bus pehle se maujood hai!");
            return;
        }

        if (busNum.isEmpty() || bName.isEmpty() || bDate.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required!");
            return;
        }

        String sql = "INSERT INTO BUSES (BUS_NUMBER, BUS_NAME, BUS_TYPE, SOURCE, DESTINATION, DEPARTURE_TIME, TRAVEL_DATE, TICKET_PRICE, ADMIN_PRICE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseHandler.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            double price = Double.parseDouble(priceField.getText().isEmpty() ? "0" : priceField.getText());
            pstmt.setString(1, busNum);
            pstmt.setString(2, bName);
            pstmt.setString(3, busTypeCombo.getValue());
            pstmt.setString(4, source);
            pstmt.setString(5, destField.getText());
            pstmt.setString(6, bTime);
            pstmt.setString(7, bDate);
            pstmt.setDouble(8, price);
            pstmt.setDouble(9, price);
            
            pstmt.executeUpdate();
            loadBusData(); 
            clearBusFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Bus added successfully!");

        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleUpdateBus() {
        if (busNumberField.getText().isEmpty()) {
            showAlert("Selection Error", "Pehle table se bus select karein!");
            return;
        }

        String query = "UPDATE BUSES SET BUS_NAME=?, BUS_TYPE=?, SOURCE=?, DESTINATION=?, DEPARTURE_TIME=?, TICKET_PRICE=? WHERE BUS_NUMBER=?";
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, busNameField.getText());
            pstmt.setString(2, busTypeCombo.getValue());
            pstmt.setString(3, sourceField.getText());
            pstmt.setString(4, destField.getText());
            String fullTime = hourSpinner.getValue() + ":" + String.format("%02d", minuteSpinner.getValue()) + " " + amPmCombo.getValue();
            pstmt.setString(5, fullTime);
            pstmt.setDouble(6, Double.parseDouble(priceField.getText().isEmpty() ? "0" : priceField.getText()));
            pstmt.setString(7, busNumberField.getText());

            pstmt.executeUpdate();
            showAlert("Success", "Bus schedule update ho gaya!");
            loadBusData(); 
            clearBusFields();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- INTEGRATED: Handle Permanent Delete Bus Logic ---
    @FXML
    private void handleDeleteBus() {
        // 1. Table se selected row uthayein (Admin table ID: adminBusTable)
        BusScheduleModel selectedBus = adminBusTable.getSelectionModel().getSelectedItem();

        if (selectedBus == null) {
            showAlert("Selection Error", "Baraye meharbani pehle aik bus select karein!");
            return;
        }

        // 2. Confirmation Alert (Safety ke liye)
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Delete Bus: " + selectedBus.getBusNumber());
        alert.setContentText("Kya aap waqai is bus ko permanent delete karna chahte hain?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            // 3. Database se delete karne ki query
            String sql = "DELETE FROM BUSES WHERE BUS_NUMBER = ?";

            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, selectedBus.getBusNumber());
                int result = pstmt.executeUpdate();

                if (result > 0) {
                    // 4. Agar DB se delete ho jaye to UI List se bhi remove karein
                    adminBusList.remove(selectedBus);
                    clearBusFields(); // Input fields saaf karein
                    showAlert("Success", "Data database se permanently delete ho gaya hai!");
                } else {
                    showAlert("Error", "Database mein ye bus nahi mili!");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Database Error", "Delete karte waqt masla aya: " + e.getMessage());
            }
        }
    }

    private void loadBusesTable() { 
        if (adminBusTable == null) return;
        adminBusList.clear();
        String query = "SELECT * FROM BUSES"; 
        try (Connection conn = DatabaseHandler.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                BusScheduleModel model = new BusScheduleModel(
                    rs.getString("BUS_NAME"), 
                    rs.getString("SOURCE") + " to " + rs.getString("DESTINATION"), 
                    rs.getString("DEPARTURE_TIME"), 
                    rs.getString("TRAVEL_DATE"), 
                    rs.getString("BUS_NUMBER")
                );
                model.setTicketPrice(rs.getDouble("TICKET_PRICE"));
                adminBusList.add(model);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void clearBusFields() {
        busNameField.clear(); busNumberField.clear(); sourceField.clear();
        destField.clear(); priceField.clear(); datePicker.setValue(null);
        busTypeCombo.setValue(null); isEditMode = false;
    }

    private void setupSerialColumn() {
        if (colSNo != null) {
            colSNo.setCellFactory(column -> new TableCell<UserModel, Void>() {
                @Override protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : String.valueOf(getIndex() + 1));
                }
            });
        }
    }

    private void setupTableMappings() {
        if (colName != null) colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colPhone != null) colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadManagers() {
        masterData.clear();
        try (Connection conn = DatabaseHandler.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM USERS WHERE ROLE = 'Manager' AND ACCOUNT_STATUS != 'DELETED'")) {
            while (rs.next()) {
                masterData.add(new UserModel(rs.getString("FULL_NAME"), rs.getString("EMAIL"), rs.getString("PHONE"), rs.getString("ACCOUNT_STATUS")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupSearch() {
        FilteredList<UserModel> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) return true;
                return user.getFullName().toLowerCase().contains(newValue.toLowerCase()) || user.getEmail().toLowerCase().contains(newValue.toLowerCase());
            });
        });
        SortedList<UserModel> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(managerTable.comparatorProperty());
        managerTable.setItems(sortedData);
    }

    @FXML
    private void createManager() {
        if(mName.getText().isEmpty() || mEmail.getText().isEmpty()) return;
        String query = "INSERT INTO USERS (FULL_NAME, EMAIL, PASSWORD_HASH, PHONE, ROLE, ACCOUNT_STATUS) VALUES (?, ?, ?, ?, 'Manager', 'ACTIVE')";
        try (Connection conn = DatabaseHandler.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, mName.getText()); pstmt.setString(2, mEmail.getText());
            pstmt.setString(3, mPass.getText()); pstmt.setString(4, mPhone.getText());
            pstmt.executeUpdate(); loadManagers();
            mName.clear(); mEmail.clear(); mPass.clear(); mPhone.clear();
            showAlert("Success", "Manager Created!");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupActionButtons() {
        if (colAction == null) return;
        colAction.setCellFactory(param -> new TableCell<UserModel, Void>() {
            private final Button delBtn = new Button("🗑");
            private final Button viewBtn = new Button("👁");
            private final HBox pane = new HBox(delBtn, viewBtn);
            {
                pane.setSpacing(10); pane.setStyle("-fx-alignment: CENTER;");
                delBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                viewBtn.setStyle("-fx-background-color: #38bdf8; -fx-text-fill: white;");
                delBtn.setOnAction(e -> handleDeleteRequest(getTableView().getItems().get(getIndex()).getEmail()));
                viewBtn.setOnAction(e -> showPassword(getTableView().getItems().get(getIndex()).getEmail()));
            }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : pane); }
        });
    }

    private void showPassword(String email) {
        try (Connection conn = DatabaseHandler.getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT PASSWORD_HASH FROM USERS WHERE EMAIL = ?")) {
            pstmt.setString(1, email); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) showAlert("Security", "Password: " + rs.getString("PASSWORD_HASH"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void handleDeleteRequest(String email) {
        try (Connection conn = DatabaseHandler.getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE USERS SET ACCOUNT_STATUS = 'DELETED' WHERE EMAIL = ?")) {
            pstmt.setString(1, email); pstmt.executeUpdate(); loadManagers();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void switchScene(ActionEvent event, String fxmlFile, int width, int height) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleLogout(ActionEvent event) { switchScene(event, "RoleSelection.fxml", 800, 500); }
    private void loadBusData() { loadBusesTable(); }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content);
        alert.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content);
        alert.showAndWait();
    }
}