package Application;

import javafx.animation.Interpolator;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.util.Objects;

public class RoleController {

    @FXML
    private Label movingLabel;

    /**
     * Unified method to load Login.fxml and pass the specific role 
     * to the LoginController.
     */
    private void loadLoginWithRole(ActionEvent event, String role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            
            // Access the controller to set the role (Passenger, Admin, or Manager)
            LoginController loginCtrl = loader.getController();
            loginCtrl.setupLoginType(role); 

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();

            Scene scene = new Scene(root, 450, 625);
            stage.setScene(scene);

            stage.setMaximized(wasMaximized);
            if (!wasMaximized) {
                stage.setWidth(450);
                stage.setHeight(625);
                stage.centerOnScreen();
            }
            stage.show();
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
    }

    @FXML
    public void initialize() {
        // Marquee animation for the lyrics line
        double sceneWidth = 850;
        TranslateTransition transition = new TranslateTransition(Duration.seconds(15), movingLabel);
        transition.setFromX(sceneWidth);
        transition.setToX(-sceneWidth);
        transition.setCycleCount(Timeline.INDEFINITE);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.play();
    }

    @FXML
    private void handleAdmin(ActionEvent event) {
        System.out.println("Admin Access Initiated...");
        loadLoginWithRole(event, "Admin");
    }

    @FXML
    private void handleManager(ActionEvent event) {
        System.out.println("Manager Terminal Loading...");
        loadLoginWithRole(event, "Manager");
    }

    @FXML
    private void handleUser(ActionEvent event) {
        System.out.println("Passenger Dashboard Opening...");
        // Now using the unified role-based loader for consistency
        loadLoginWithRole(event, "Passenger");
    }

    // Keeping switchScene here in case you use it for other parts of the app
    private void switchScene(ActionEvent event, String fxml, int w, int h) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();

            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            Scene scene = new Scene(root, w, h);
            stage.setScene(scene);

            stage.setMaximized(wasMaximized);
            if (!wasMaximized) {
                stage.setWidth(w);
                stage.setHeight(h);
                stage.centerOnScreen();
            }
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}