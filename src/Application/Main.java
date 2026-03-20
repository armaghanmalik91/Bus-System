package Application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			// This line loads your FXML design
			Parent root = FXMLLoader.load(getClass().getResource("RoleSelection.fxml"));
			Scene scene = new Scene(root, 800, 500);
			
			primaryStage.setTitle("Bus Management System - Welcome");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}