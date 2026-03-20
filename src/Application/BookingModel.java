package Application;

public class BookingModel {
    private String passengerName;
    private String busName;
    private String route;
    private String phone;
    private String status;
    private String passengerCNIC;
    private int seatNumber;
    private String paymentStatus; 
    
    // Naye fields jo pehli file mein the
    private String passengerEmail; 
    private String paymentProofPath;
    private String assignedManager;
    private String busNumber;
    private String seatType;

    // Empty constructor for JavaFX mapping
    public BookingModel() {}

    // --- CONSTRUCTORS (SAB PURANE WALE RAKHE GAYE HAIN) ---

    // Constructor 1: 6 parameters (Purana logic)
    public BookingModel(String passengerName, String busName, String route, String phone, String status, String passengerCNIC) {
        this.passengerName = passengerName;
        this.busName = busName;
        this.route = route;
        this.phone = phone;
        this.status = status;
        this.passengerCNIC = passengerCNIC;
    }

    // Constructor 2: 7 parameters (Purana logic with seat)
    public BookingModel(String passengerName, String busName, String route, String phone, String status, String passengerCNIC, int seatNumber) {
        this.passengerName = passengerName;
        this.busName = busName;
        this.route = route;
        this.phone = phone;
        this.status = status;
        this.passengerCNIC = passengerCNIC;
        this.seatNumber = seatNumber;
    }

    // Constructor 3: 8 parameters (With Payment Status)
    public BookingModel(String passengerName, String busName, String route, String phone, String status, String passengerCNIC, int seatNumber, String paymentStatus) {
        this.passengerName = passengerName;
        this.busName = busName;
        this.route = route;
        this.phone = phone;
        this.status = status;
        this.passengerCNIC = passengerCNIC;
        this.seatNumber = seatNumber;
        this.paymentStatus = paymentStatus;
    }

    // --- GETTERS & SETTERS (PREVIOUS + NEW) ---

    public String getPassengerName() { return passengerName; }
    public String getBusName() { return busName; }
    public String getRoute() { return route; }
    public String getPhone() { return phone; }
    public String getStatus() { return status; }
    public String getPassengerCNIC() { return passengerCNIC; }
    public int getSeatNumber() { return seatNumber; }
    public String getPaymentStatus() { return paymentStatus; }

    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public void setStatus(String status) { this.status = status; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPassengerEmail() { return passengerEmail; }
    public void setPassengerEmail(String passengerEmail) { this.passengerEmail = passengerEmail; }

    public String getPaymentProofPath() { return paymentProofPath; }
    public void setPaymentProofPath(String paymentProofPath) { this.paymentProofPath = paymentProofPath; }

    public String getAssignedManager() { return assignedManager; }
    public void setAssignedManager(String assignedManager) { this.assignedManager = assignedManager; }

    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

    public String getSeatType() { return seatType; }
    public void setSeatType(String seatType) { this.seatType = seatType; }
}