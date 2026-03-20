package DataBase;

import java.sql.*;

public class DatabaseHandler {
    private static final String URL = "jdbc:h2:C:/Users/Public/bus_system_db;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";
    
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASS);
        createTableIfNotExists(conn);
        
        try (Statement stmt = conn.createStatement()) {
            // --- USERS Table Patches ---
            stmt.execute("ALTER TABLE USERS ADD COLUMN IF NOT EXISTS PHONE VARCHAR(20) UNIQUE");
            stmt.execute("ALTER TABLE USERS ADD COLUMN IF NOT EXISTS ACCOUNT_STATUS VARCHAR(20) DEFAULT 'ACTIVE'");
            // NEW PATCH: Profile Picture path save karne ke liye
            stmt.execute("ALTER TABLE USERS ADD COLUMN IF NOT EXISTS PROFILE_PIC VARCHAR(500)");
            
            // --- BOOKINGS Table Patches ---
            stmt.execute("ALTER TABLE BOOKINGS ADD COLUMN IF NOT EXISTS PASSENGER_CNIC VARCHAR(30)");
            stmt.execute("ALTER TABLE BOOKINGS ADD COLUMN IF NOT EXISTS SEAT_NUMBER INT");
            stmt.execute("ALTER TABLE BOOKINGS ADD COLUMN IF NOT EXISTS PHONE VARCHAR(20)");
            stmt.execute("ALTER TABLE BOOKINGS ADD COLUMN IF NOT EXISTS TRAVEL_DATE VARCHAR(255)");
            stmt.execute("ALTER TABLE BOOKINGS ADD COLUMN IF NOT EXISTS PASSENGER_EMAIL VARCHAR(255)");
            stmt.execute("ALTER TABLE BOOKINGS ADD COLUMN IF NOT EXISTS ASSIGNED_MANAGER VARCHAR(255)");
            stmt.execute("ALTER TABLE BOOKINGS ADD COLUMN IF NOT EXISTS PAYMENT_PROOF_PATH VARCHAR(500)");
            stmt.execute("ALTER TABLE BOOKINGS ADD COLUMN IF NOT EXISTS PAYMENT_STATUS VARCHAR(20) DEFAULT 'PAID'");
            stmt.execute("ALTER TABLE BOOKINGS ADD COLUMN IF NOT EXISTS IS_REVIEWED BOOLEAN DEFAULT FALSE");

            // --- BUSES Table Patches ---
            stmt.execute("ALTER TABLE BUSES ADD COLUMN IF NOT EXISTS BUS_NUMBER VARCHAR(50) UNIQUE");
            stmt.execute("ALTER TABLE BUSES ADD COLUMN IF NOT EXISTS BUS_TYPE VARCHAR(50)"); 
            stmt.execute("ALTER TABLE BUSES ADD COLUMN IF NOT EXISTS TRAVEL_DATE VARCHAR(255)");
            stmt.execute("ALTER TABLE BUSES ADD COLUMN IF NOT EXISTS TICKET_PRICE DOUBLE");
            
            stmt.execute("ALTER TABLE BUSES ADD COLUMN IF NOT EXISTS ADMIN_PRICE DOUBLE");
            stmt.execute("UPDATE BUSES SET ADMIN_PRICE = TICKET_PRICE WHERE ADMIN_PRICE IS NULL OR ADMIN_PRICE = 0");

            try {
                stmt.execute("ALTER TABLE BOOKINGS ADD CONSTRAINT UNIQUE_BOOKING UNIQUE(BUS_NUMBER, SEAT_NUMBER, TRAVEL_DATE)");
            } catch (SQLException e) { }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return conn;
    }

    private static void createTableIfNotExists(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // 1. USERS Table
            stmt.execute("CREATE TABLE IF NOT EXISTS USERS (" +
                         "ID INT AUTO_INCREMENT PRIMARY KEY, FULL_NAME VARCHAR(255), " +
                         "EMAIL VARCHAR(255) UNIQUE, PASSWORD_HASH VARCHAR(255), " +
                         "PHONE VARCHAR(20) UNIQUE, ROLE VARCHAR(50), " +
                         "ACCOUNT_STATUS VARCHAR(20) DEFAULT 'ACTIVE', " +
                         "PROFILE_PIC VARCHAR(500))");

            // 2. BUSES Table
            stmt.execute("CREATE TABLE IF NOT EXISTS BUSES (" +
                         "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                         "BUS_NUMBER VARCHAR(50) UNIQUE, " +
                         "BUS_NAME VARCHAR(255), " +
                         "BUS_TYPE VARCHAR(50), " + 
                         "SOURCE VARCHAR(255), DESTINATION VARCHAR(255), " +
                         "DEPARTURE_TIME VARCHAR(255), " +
                         "TRAVEL_DATE VARCHAR(255), " + 
                         "TICKET_PRICE DOUBLE, " +
                         "ADMIN_PRICE DOUBLE)");

            // 3. BOOKINGS Table
            stmt.execute("CREATE TABLE IF NOT EXISTS BOOKINGS (" +
                         "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                         "PASSENGER_NAME VARCHAR(255), " +
                         "PASSENGER_EMAIL VARCHAR(255), " +
                         "PASSENGER_CNIC VARCHAR(30), " + 
                         "PHONE VARCHAR(20), " +
                         "BUS_NAME VARCHAR(255), " +
                         "BUS_NUMBER VARCHAR(50), " +
                         "SOURCE VARCHAR(255), " +
                         "DESTINATION VARCHAR(255), " +
                         "TRAVEL_DATE VARCHAR(255), " +
                         "PRICE DOUBLE, " +
                         "SEAT_NUMBER INT, " + 
                         "SEAT_TYPE VARCHAR(50), " +
                         "STATUS VARCHAR(50), " +
                         "ASSIGNED_MANAGER VARCHAR(255), " +
                         "PAYMENT_PROOF_PATH VARCHAR(500), " +
                         "PAYMENT_STATUS VARCHAR(20) DEFAULT 'PAID', " + 
                         "IS_REVIEWED BOOLEAN DEFAULT FALSE)"); 

            insertDefaultAdmin(conn);
            insertDefaultManager(conn);
            insertDummyData(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Role-Based Validation Method ---
    public boolean validateLogin(String email, String password, String expectedRole) {
        String query = "SELECT * FROM USERS WHERE EMAIL = ? AND PASSWORD_HASH = ? AND ROLE = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, email);
            ps.setString(2, password);
            ps.setString(3, expectedRole);
            
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- MERGED: Update Manager Profile Method ---
    public boolean updateManagerProfile(String newName, String newEmail, String oldEmail) {
        String query = "UPDATE USERS SET FULL_NAME = ?, EMAIL = ? WHERE EMAIL = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, newName);
            ps.setString(2, newEmail);
            ps.setString(3, oldEmail); 
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Profile updated successfully in Database.");
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Update Failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- Profile Picture Update Method ---
    public boolean updateProfilePic(String email, String imagePath) {
        String query = "UPDATE USERS SET PROFILE_PIC = ? WHERE EMAIL = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, imagePath);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static void insertDefaultAdmin(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM USERS WHERE ROLE = 'Admin'");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO USERS (FULL_NAME, EMAIL, PASSWORD_HASH, ROLE, ACCOUNT_STATUS) " +
                             "VALUES ('Super Admin', 'admin@bus.com', 'admin123', 'Admin', 'ACTIVE')");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void insertDefaultManager(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM USERS WHERE ROLE = 'Manager'");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO USERS (FULL_NAME, EMAIL, PASSWORD_HASH, PHONE, ROLE, ACCOUNT_STATUS) " +
                             "VALUES ('System Manager', 'manager@bus.com', 'manager123', '03001234567', 'Manager', 'ACTIVE')");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void insertDummyData(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM BUSES");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO BUSES (BUS_NUMBER, BUS_NAME, BUS_TYPE, SOURCE, DESTINATION, DEPARTURE_TIME, TRAVEL_DATE, TICKET_PRICE, ADMIN_PRICE) " +
                             "VALUES ('ABC-123', 'Faisal Movers', 'Normal', 'Lahore', 'Islamabad', '10:00 AM', '2026-02-25', 1500.0, 1500.0)");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}