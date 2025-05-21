package edu.ntnu.idi.idatt.navigation;

import edu.ntnu.idi.idatt.controller.BoardGameController;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.view.BoardGameView;
import edu.ntnu.idi.idatt.view.CharacterSelectionView;
import edu.ntnu.idi.idatt.view.MainMenuView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigationManager {
  private static NavigationManager instance;

  private Stage primaryStage;
  private Scene scene;

  // Game components
  private BoardGame boardGame;

  private NavigationManager() {
    // Private constructor for singleton
  }

  public static NavigationManager getInstance() {
    if (instance == null) {
      instance = new NavigationManager();
    }
    return instance;
  }

  public void initialize(Stage stage) {
    this.primaryStage = stage;
    this.scene = new Scene(new javafx.scene.layout.StackPane(), 800, 600);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Board Game");
  }

  public void navigateTo(NavigationTarget target) {
    switch (target) {
      case MAIN_MENU:
        showMainMenu();
        break;

      case CHARACTER_SELECTION:
        showCharacterSelection();
        break;

      case GAME_SCREEN:
        showGameScreen();
        break;

      case LOAD_GAME:
        showLoadGameScreen();
        break;
    }
  }

  private void showMainMenu() {
    MainMenuView mainMenuView = new MainMenuView();
    mainMenuView.setNewGameHandler(() -> navigateTo(NavigationTarget.CHARACTER_SELECTION));
    mainMenuView.setLoadGameHandler(() -> navigateTo(NavigationTarget.LOAD_GAME));
    mainMenuView.setExitHandler(() -> primaryStage.close());

    setRoot(mainMenuView.getRoot());
  }

  private void showCharacterSelection() {
    // Initialize a new game
    boardGame = new BoardGame();
    boardGame.createBoard(10, 10);
    boardGame.createDice(2);

    CharacterSelectionView characterSelectionView = new CharacterSelectionView();
    characterSelectionView.setStartGameHandler(() -> {
      // Add selected players to the game
      List<PlayerData> selectedPlayers = characterSelectionView.getSelectedPlayers();
      for (PlayerData data : selectedPlayers) {
        Player player = new Player(data.getName(), boardGame);
        player.setTokenType(data.getToken());
        boardGame.addPlayer(player);
        player.placeOnTile(boardGame.getBoard().getTile(1));
      }

      navigateTo(NavigationTarget.GAME_SCREEN);
    });
    characterSelectionView.setBackHandler(() -> navigateTo(NavigationTarget.MAIN_MENU));

    setRoot(characterSelectionView.getRoot());
  }

  private void showGameScreen() {
    BoardGameView gameView = new BoardGameViewImpl();
    BoardGameController controller = new BoardGameController(boardGame, gameView);

    setRoot(gameView.getRoot());
  }

  private void showLoadGameScreen() {
    // Implementation for load game screen
  }

  private void setRoot(Parent root) {
    scene.setRoot(root);
  }

  public Stage getPrimaryStage() {
    return primaryStage;
  }
}