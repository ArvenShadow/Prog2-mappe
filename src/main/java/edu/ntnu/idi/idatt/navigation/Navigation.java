package edu.ntnu.idi.idatt.navigation;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayDeque;
import java.util.Deque;

public class Navigation implements NavigationHandler {
  private static Navigation instance;
  private Stage primaryStage;
  private Scene mainScene;
  private Deque<Parent> navigationStack = new ArrayDeque<>();

  private Navigation() {}

  public static synchronized Navigation getInstance() {
    if (instance == null) {
      instance = new Navigation();
    }
    return instance;
  }

  public void init(Stage stage) {
    this.primaryStage = stage;
    this.mainScene = new Scene(new javafx.scene.layout.StackPane());
    primaryStage.setScene(mainScene);
  }

  @Override
  public void navigateTo(NavTo target) {
    switch (target) {
      case START_SCREEN:
        setRoot(createStartScene());
        break;

      case CHARACTER_SELECTION:
        setRoot(createCharacterSelectionScreen());
        break;

      case GAME_SCREEN:
        setRoot(createGameScreen());
        break;

      case LOAD_GAME_SCREEN:
        setRoot(createLoadGameScreen());
        break;

      case SETTINGS_SCREEN:
        setRoot(createSettingScreen());
        break;
    }
  }

  @Override
  public void navigateBack() {
    if (!navigationStack.isEmpty()) {
      navigationStack.pop();
      if(!navigationStack.isEmpty()) {
        setRoot(navigationStack.peek());
      }
    }
  }

  @Override
  public void setRoot(Parent root) {
    navigationStack.push(root);
    mainScene.setRoot(root);
  }

  private Parent createStartScene() {

    return new javafx.scene.layout.StackPane();
  }

  private Parent createCharacterSelectionScreen() {

    return new javafx.scene.layout.StackPane();
  }

  private Parent createGameScreen() {

    return new javafx.scene.layout.StackPane();
  }

  private Parent createLoadGameScreen() {

    return new javafx.scene.layout.StackPane();
  }

  private Parent createSettingScreen() {

    return new javafx.scene.layout.StackPane();
  }

  public Stage getPrimaryStage() {
    return primaryStage;
  }



}
