package Application;

public class UserSession {
    // Singleton Instance
    private static UserSession instance;
    private UserModel user; // For UserModel object handling

    // Static Variables (Compatibility ke liye)
    private static String loggedInUserEmail;
    private static String loggedInUserName;
    private static String userRole; // Manager or Passenger

    // Private Constructor for Singleton
    private UserSession() {}

    // Method to get Singleton Instance
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // --- New Object Oriented Methods ---
    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
        // Syncing with static variables for backward compatibility
        if (user != null) {
            loggedInUserEmail = user.getEmail();
            loggedInUserName = user.getFullName();
            userRole = user.getRole();
        }
    }

    public void cleanUserSession() {
        user = null;
        cleanSession(); // Purane static fields ko bhi saaf karega
    }

    // --- Static Methods (Merged & Updated for compatibility) ---
    
    public static void setSession(String email, String name, String role) {
        loggedInUserEmail = email;
        loggedInUserName = name;
        userRole = role;
    }

    public static String getEmail() {
        if (instance != null && instance.user != null) {
            return instance.user.getEmail();
        }
        return loggedInUserEmail;
    }

    public static String getName() {
        // Pehle instance se check karega, agar null ho to static variable se
        if (instance != null && instance.user != null) {
            return instance.user.getFullName();
        }
        return (loggedInUserName != null) ? loggedInUserName : "User";
    }

    public static String getRole() {
        if (instance != null && instance.user != null) {
            return instance.user.getRole();
        }
        return userRole;
    }

    public static void cleanSession() {
        loggedInUserEmail = null;
        loggedInUserName = null;
        userRole = null;
        if (instance != null) {
            instance.user = null;
        }
    }
}