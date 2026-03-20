package Application;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserModel {
    private final StringProperty fullName;
    private final StringProperty email;
    private final StringProperty phone;
    private final StringProperty status;
    private final StringProperty role;
    private final StringProperty profilePic;
    

    // Constructor for Login/Session (3 parameters)
    public UserModel(String fullName, String email, String role) {
        this.fullName = new SimpleStringProperty(fullName);
        this.email = new SimpleStringProperty(email);
        this.role = new SimpleStringProperty(role);
        this.phone = new SimpleStringProperty("");
        this.status = new SimpleStringProperty("Active");
        this.profilePic = new SimpleStringProperty("");
        
    }

    
    // Constructor for TableView/Admin (4 parameters)
    public UserModel(String fullName, String email, String phone, String status) {
        this.fullName = new SimpleStringProperty(fullName);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone);
        this.status = new SimpleStringProperty(status);
        this.role = new SimpleStringProperty("");
        this.profilePic = new SimpleStringProperty("");
    }

    // --- Getters for Properties (Required for TableView binding) ---
    public StringProperty fullNameProperty() { return fullName; }
    public StringProperty emailProperty() { return email; }
    public StringProperty phoneProperty() { return phone; }
    public StringProperty statusProperty() { return status; }
    public StringProperty roleProperty() { return role; }
    public StringProperty profilePicProperty() { return profilePic; }

    // --- Standard Getters ---
    public String getFullName() { return fullName.get(); }
    public String getEmail() { return email.get(); }
    public String getPhone() { return phone.get(); }
    public String getStatus() { return status.get(); }
    public String getRole() { return role.get(); }
    public String getProfilePic() { return profilePic.get(); }

    // --- Setters (Errors khatam karne ke liye) ---
    public void setFullName(String fullName) { this.fullName.set(fullName); }
    public void setEmail(String email) { this.email.set(email); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public void setStatus(String status) { this.status.set(status); }
    public void setRole(String role) { this.role.set(role); }
    public void setProfilePic(String path) { this.profilePic.set(path); }
}