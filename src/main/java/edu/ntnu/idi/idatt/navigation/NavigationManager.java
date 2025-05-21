package edu.ntnu.idi.idatt.navigation;

import edu.ntnu.idi.idatt.controller.BoardGameController;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.model.Player;
import edu.ntnu.idi.idatt.model.PlayerData;
import edu.ntnu.idi.idatt.view.BoardGameViewImpl;
import edu.ntnu.idi.idatt.view.CharacterSelectionView;
import edu.ntnu.idi.idatt.view.MainMenuView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;
import java.util.Stack;

public class NavigationManager implements NavigationHandler {
  private static NavigationManager instance;

  private Stage primaryStage;
  private Scene scene;
  private Stack<NavTo> navigationHistory;

  // Game components
  private BoardGame boardGame;

  private NavigationManager() {
    navigationHistory = new Stack<>();
  }

  public static NavigationManager getInstance() {
    if (instance == null) {
      instance = new NavigationManager();
    }
    return instance;
  }

  public void initialize(Stage stage, String title, int width, int height) {
    this.primaryStage = stage;
    this.scene = new Scene(new javafx.scene.layout.StackPane(), width, height);
    primaryStage.setScene(scene);
    primaryStage.setTitle(title);
    primaryStage.setMinWidth(800);
    primaryStage.setMinHeight(600);

    // Create initial game model
    this.boardGame = new BoardGame();
  }

  @Override
  public void navigateTo(NavTo target) {
    navigationHistory.push(target);

    switch (target) {
      case START_SCREEN:
        showMainMenu();
        break;
      case CHARACTER_SELECTION:
        showCharacterSelection();
        break;
      case GAME_SCREEN:
        showGameScreen();
        break;
      case LOAD_GAME_SCREEN:
        showLoadGameScreen();
        break;
      case SETTINGS_SCREEN:
        showSettingsScreen();
        break;
    }
  }

  @Override
  public void navigateBack() {
    if (navigationHistory.size() > 1) {
      navigationHistory.pop(); // Remove current
      NavTo previous = navigationHistory.pop(); // Get previous
      navigateTo(previous); // Navigate to previous
    }
  }

  @Override
  public void setRoot(Parent root) {
    scene.setRoot(root);
  }

  private void showMainMenu() {
    MainMenuView mainMenuView = new MainMenuView();
    mainMenuView.setNewGameHandler(() -> navigateTo(NavTo.CHARACTER_SELECTION));
    mainMenuView.setLoadGameHandler(() -> navigateTo(NavTo.LOAD_GAME_SCREEN));
    mainMenuView.setExitHandler(() -> primaryStage.close());

    setRoot(mainMenuView.getRoot());
  }

  private void showCharacterSelection() {
    // Initialize a new game
    boardGame = new BoardGame();
    boardGame.createBoard();
    boardGame.createDice(2);

    CharacterSelectionView characterSelectionView = new CharacterSelectionView();
    characterSelectionView.setStartGameHandler(() -> {
      // Add selected players to the game
      List<PlayerData> selectedPlayers = characterSelectionView.getSelectedPlayers();
      for (PlayerData data : selectedPlayers) {
        Player player = new Player(data.getName(), boardGame, data.getToken());
        boardGame.addPlayer(player);
        player.placeOnTile(boardGame.getBoard().getTile(1));
      }

      navigateTo(NavTo.GAME_SCREEN);
    });
    characterSelectionView.setBackHandler(() -> navigateBack());

    setRoot(characterSelectionView.getRoot());
  }

  private void showGameScreen() {
    // Create the actual implementation of BoardGameView
    BoardGameViewImpl gameView = new BoardGameViewImpl(boardGame);

    // Connect view with controller
    BoardGameController controller = new BoardGameController(boardGame, gameView);

    // Set the view as root
    setRoot(gameView.getRoot());
  }

  private void showLoadGameScreen() {
    // Implementation for load game screen
    // For now, we'll create a simple placeholder
    javafx.scene.layout.VBox loadGameView = new javafx.scene.layout.VBox();
    loadGameView.getChildren().add(new javafx.scene.control.Label("Load Game Screen - To be implemented"));

    javafx.scene.control.Button backButton = new javafx.scene.control.Button("Back");
    backButton.setOnAction(e -> navigateBack());
    loadGameView.getChildren().add(backButton);

    setRoot(loadGameView);
  }

  private void showSettingsScreen() {
    // Implementation for settings screen
    // Simple placeholder
    javafx.scene.layout.VBox settingsView = new javafx.scene.layout.VBox();
    settingsView.getChildren().add(new javafx.scene.control.Label("Settings Screen - To be implemented"));

    javafx.scene.control.Button backButton = new javafx.scene.control.Button("Back");
    backButton.setOnAction(e -> navigateBack());
    settingsView.getChildren().add(backButton);

    setRoot(settingsView);
  }

  public Stage getPrimaryStage() {
    return primaryStage;
  }

  public BoardGame getBoardGame() {
    return boardGame;
  }
}