package Application;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;

public class BusScheduleModel {
    private final StringProperty busName;
    private final StringProperty route;
    private final StringProperty timing;
    private final StringProperty date;
    private final StringProperty busNumber; // Plate Number field
    
    // --- Naya DoubleProperty variable ---
    private final DoubleProperty ticketPrice = new SimpleDoubleProperty(0.0);

    public BusScheduleModel(String busName, String route, String timing, String date) {
        this.busName = new SimpleStringProperty(busName);
        this.route = new SimpleStringProperty(route);
        this.timing = new SimpleStringProperty(timing);
        this.date = new SimpleStringProperty(date);
        this.busNumber = new SimpleStringProperty(""); // Default empty
    }

    // Overloaded Constructor (for Database with Plate Number)
    public BusScheduleModel(String busName, String route, String timing, String date, String busNumber) {
        this.busName = new SimpleStringProperty(busName);
        this.route = new SimpleStringProperty(route);
        this.timing = new SimpleStringProperty(timing);
        this.date = new SimpleStringProperty(date);
        this.busNumber = new SimpleStringProperty(busNumber);
    }

    // --- Added as requested (Maintaining Property Logic) ---
    public void setBusNumber(String busNumber) { 
        this.busNumber.set(busNumber); 
    }

    // --- Ticket Price Methods (Updated to Property logic) ---
    public void setTicketPrice(double price) { 
        this.ticketPrice.set(price); 
    }

    public double getTicketPrice() {
        return ticketPrice.get();
    }

    public DoubleProperty ticketPriceProperty() { 
        return ticketPrice; 
    }

    // --- LAZMI GETTER methods ---
    public String getBusNumber() { 
        return busNumber.get(); 
    }

    // Standard Getters (Purani logic ke liye)
    public String getBusName() { return busName.get(); }
    public String getRoute() { return route.get(); }
    public String getTiming() { return timing.get(); }
    public String getDate() { return date.get(); }

    // Property Getters (JavaFX TableView binding ke liye zaroori)
    public StringProperty busNameProperty() { return busName; }
    public StringProperty routeProperty() { return route; }
    public StringProperty timingProperty() { return timing; }
    public StringProperty dateProperty() { return date; }
    public StringProperty busNumberProperty() { return busNumber; }
}