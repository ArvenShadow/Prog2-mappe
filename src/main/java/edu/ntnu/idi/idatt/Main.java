package edu.ntnu.idi.idatt;

import edu.ntnu.idi.idatt.navigation.NavTo;
import edu.ntnu.idi.idatt.navigation.NavigationManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
  @Override
  public void start(Stage primaryStage) {
    // Initialize navigationManager manager
    NavigationManager navManager = NavigationManager.getInstance();
    navManager.initialize(primaryStage, "Snakes and Ladders", 900, 700);

    // Set up CSS styling
    String cssPath = getClass().getResource("/styles/game.css").toExternalForm();
    primaryStage.getScene().getStylesheets().add(cssPath);

    // Navigate to start screen
    navManager.navigateTo(NavTo.START_SCREEN);

    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}