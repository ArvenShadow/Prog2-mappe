package edu.ntnu.idi.idatt.controller;

import edu.ntnu.idi.idatt.event.BoardGameObserver;
import edu.ntnu.idi.idatt.event.GameEvent;
import edu.ntnu.idi.idatt.exception.BoardGameException;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.model.Dice;
import edu.ntnu.idi.idatt.model.Player;
import edu.ntnu.idi.idatt.model.Tile;
import edu.ntnu.idi.idatt.view.BoardView;
import edu.ntnu.idi.idatt.view.DiceView;
import edu.ntnu.idi.idatt.view.PlayerInfoView;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameController implements BoardGameObserver {
  private final BoardGame game;
  private final Stage primaryStage;
  private final BorderPane rootPane;

  private BoardView boardView;
  private PlayerInfoView playerInfoView;
  private Text statusText;
  private Button rollButton;
  private Button newGameButton;
  private DiceView diceView;

  private boolean animationInProgress = false;

  public GameController(BoardGame game, BorderPane rootPane, Stage primaryStage) {
    this.game = game;
    this.rootPane = rootPane;
    this.primaryStage = primaryStage;

    // Register as observer for game events
    game.addObserver(this);

    // Create UI components
    setupUI();

    // Initialize player positions if any players exist
    if (!game.getPlayers().isEmpty()) {
      updateAllPlayerPositions();
    }
  }

  private void setupUI() {
    // Create the board view
    boardView = new BoardView(game.getBoard());

    // Create status display
    statusText = new Text("Welcome to Snakes and Ladders!");
    statusText.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    // Create dice view
    diceView = new DiceView();

    // Create game controls
    rollButton = new Button("Roll Dice");
    rollButton.setOnAction(e -> handleRollDice());
    rollButton.setDisable(game.getPlayers().isEmpty());

    newGameButton = new Button("New Game");
    newGameButton.setOnAction(e -> showNewGameDialog());

    Button loadGameButton = new Button("Load Game");
    loadGameButton.setOnAction(e -> handleLoadGame());

    Button saveGameButton = new Button("Save Game");
    saveGameButton.setOnAction(e -> handleSaveGame());

    // Create controls box
    HBox controlsBox = new HBox(10, rollButton, newGameButton, loadGameButton, saveGameButton);
    controlsBox.setAlignment(Pos.CENTER);
    controlsBox.setPadding(new Insets(10));

    // Create top bar with status and dice
    HBox topBar = new HBox(20, statusText, diceView);
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setPadding(new Insets(10));

    // Create player info view
    playerInfoView = new PlayerInfoView(game.getPlayers(), boardView);

    // Add components to the main layout
    rootPane.setCenter(boardView);
    rootPane.setTop(topBar);
    rootPane.setBottom(controlsBox);
    rootPane.setRight(playerInfoView);

    // Set padding for main layout
    rootPane.setPadding(new Insets(10));
  }


  private static class NewGameSettings {
    final int diceCount;
    final List<Player> players;

    NewGameSettings(int diceCount, List<Player> players) {
      this.diceCount = diceCount;
      this.players = players;
    }
  }

  private void showNewGameDialog() {
    // Create a dialog for new game setup
    Dialog<NewGameSettings> dialog = new Dialog<>();
    dialog.setTitle("New Game");
    dialog.setHeaderText("Set up a new game");

    // Set the button types
    ButtonType startButtonType = new ButtonType("Start Game", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(startButtonType, ButtonType.CANCEL);

    // Create the grid for inputs
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    // Dice count selection
    Label diceLabel = new Label("Number of Dice:");
    Spinner<Integer> diceSpinner = new Spinner<>(1, Dice.getMaxDice(), 2);
    diceSpinner.setEditable(true);

    grid.add(diceLabel, 0, 0);
    grid.add(diceSpinner, 1, 0);

    // Player count spinner
    Label playerLabel = new Label("Number of Players:");
    Spinner<Integer> playerCount = new Spinner<>(2, 4, 2);
    playerCount.setEditable(true);

    grid.add(playerLabel, 0, 1);
    grid.add(playerCount, 1, 1);

    // Create a section for player inputs
    VBox playersBox = new VBox(10);
    List<TextField> nameFields = new ArrayList<>();
    List<ComboBox<String>> tokenFields = new ArrayList<>();

    // Initialize for 2 players
    for (int i = 0; i < 4; i++) {
      HBox playerRow = new HBox(10);

      TextField nameField = new TextField("Player " + (i + 1));
      nameFields.add(nameField);

      ComboBox<String> tokenBox = new ComboBox<>();
      tokenBox.getItems().addAll("TopHat", "RaceCar", "Shoe", "Thimble");
      tokenBox.setValue(tokenBox.getItems().get(i % tokenBox.getItems().size()));
      tokenFields.add(tokenBox);

      playerRow.getChildren().addAll(new Label("Player " + (i + 1) + ":"), nameField, new Label("Token:"), tokenBox);

      // Only show first 2 rows initially
      playerRow.setVisible(i < 2);
      playerRow.setManaged(i < 2);

      playersBox.getChildren().add(playerRow);
    }

    // Listen for player count changes
    playerCount.valueProperty().addListener((obs, oldVal, newVal) -> {
      for (int i = 0; i < 4; i++) {
        HBox playerRow = (HBox) playersBox.getChildren().get(i);
        boolean visible = i < newVal;
        playerRow.setVisible(visible);
        playerRow.setManaged(visible);
      }
    });

    grid.add(playersBox, 0, 2, 2, 1);

    dialog.getDialogPane().setContent(grid);

    // Request focus on the first field
    Platform.runLater(() -> nameFields.get(0).requestFocus());

    // Create a class to hold the settings
    class NewGameSettings {
      final int diceCount;
      final List<Player> players;

      NewGameSettings(int diceCount, List<Player> players) {
        this.diceCount = diceCount;
        this.players = players;
      }
    }

    // Convert the result to game settings when dialog is confirmed
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == startButtonType) {
        int diceCount = diceSpinner.getValue();
        List<Player> newPlayers = new ArrayList<>();
        int count = playerCount.getValue();

        for (int i = 0; i < count; i++) {
          String name = nameFields.get(i).getText();
          String token = tokenFields.get(i).getValue();

          if (name.isEmpty()) {
            name = "Player " + (i + 1);
          }

          newPlayers.add(new Player(name, game, token));
        }

        return new NewGameSettings(diceCount, newPlayers);
      }
      return null;
    });

    Optional<GameController.NewGameSettings> result = dialog.showAndWait();

    result.ifPresent(settings -> {
      // Clear existing players
      game.getPlayers().clear();

      // Add new players
      for (Player player : settings.players) {
        game.addPlayer(player);
      }

      // Set dice count
      if (game.getDice() == null) {
        game.createDice(settings.diceCount);
      } else {
        game.getDice().setNumberOfDice(settings.diceCount);
      }

      // Update dice view
      diceView.setDiceCount(settings.diceCount);

      // Start new game
      handleNewGame();
    });
  }

  private void handleNewGame() {
    try {
      // Reset game state
      game.reset();

      // Create a new board if one doesn't exist
      if (game.getBoard() == null) {
        game.createBoard();
      }

      // Create dice if they don't exist
      if (game.getDice() == null)

      // Update the board view
      boardView.setBoard(game.getBoard());

      // Clear and redraw the board
      boardView.clearPlayers();

      // Place all players on the starting tile
      for (Player player : game.getPlayers()) {
        Tile startTile = game.getBoard().getTile(1);
        player.placeOnTile(startTile);
      }

      // Update player positions on the board
      updateAllPlayerPositions();

      // Update player info panel
      playerInfoView = new PlayerInfoView(game.getPlayers(), boardView);
      rootPane.setRight(playerInfoView);

      // Update status
      statusText.setText("New game started! " +
        (game.getPlayers().isEmpty() ? "Add players to begin." : "Roll the dice to start."));

      // Enable/disable roll button
      rollButton.setDisable(game.getPlayers().isEmpty());

    } catch (Exception e) {
      statusText.setText("Error starting new game: " + e.getMessage());
    }
  }

  private void addDiceControl(VBox controlsBox) {
    HBox diceControlBox = new HBox(10);
    diceControlBox.setAlignment(Pos.CENTER);

    Label diceLabel = new Label("Number of Dice:");

    Spinner<Integer> diceSpinner = new Spinner<>(1, Dice.getMaxDice(), game.getDice().getNumberOfDice());
    diceSpinner.setEditable(true);
    diceSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
      // Update model
      game.getDice().setNumberOfDice(newVal);

      // Update view
      diceView.setDiceCount(newVal);

      statusText.setText("Number of dice changed to " + newVal);
    });

    diceControlBox.getChildren().addAll(diceLabel, diceSpinner);
    controlsBox.getChildren().add(diceControlBox);
  }

  private void handleRollDice() {
    // Debug output to console
    System.out.println("Roll button clicked!");

    // Check game state conditions
    if (game.isFinished() || animationInProgress || game.getPlayers().isEmpty()) {
      System.out.println("Cannot roll: " +
        (game.isFinished() ? "game finished" :
          animationInProgress ? "animation in progress" : "no players"));
      return;
    }

    // Disable roll button during turn
    rollButton.setDisable(true);
    animationInProgress = true;

    // Make sure there's a current player
    Player currentPlayer = game.getCurrentPlayer();
    if (currentPlayer == null && !game.getPlayers().isEmpty()) {
      // If no current player is set, use the first player
      currentPlayer = game.getPlayers().get(0);
      System.out.println("No current player set, using: " + currentPlayer.getName());
    }

    final Player player = currentPlayer;
    System.out.println("Current player: " + player.getName() +
      " at tile " + (player.getCurrentTile() != null ? player.getCurrentTile().getTileId() : "start"));

    // Roll dice and get values
    int rollValue = game.getDice().Roll();
    int[] diceValues = new int[game.getDice().getNumberOfDice()];
    for (int i = 0; i < diceValues.length; i++) {
      diceValues[i] = game.getDice().getDieValue(i);
    }
    System.out.println("Rolled total: " + rollValue + " (" +
      Arrays.toString(diceValues) + ")");

    // Update dice view with individual die values
    diceView.setValues(diceValues);

    // Update status text
    statusText.setText(player.getName() + " rolled a total of " + rollValue);

    // Animate dice rolling
    diceView.roll();

    // Wait before moving player
    PauseTransition pause = new PauseTransition(Duration.seconds(1));
    pause.setOnFinished(e -> {
      try {
        // Get current tile
        Tile oldTile = player.getCurrentTile();
        System.out.println("Moving player from " +
          (oldTile != null ? oldTile.getTileId() : "start"));

        // Move player (player.move will generate events that our observer handles)
        player.move(rollValue);

        // NOTE: Don't reset animationInProgress here
        // Observer will handle this after all animations (including ladders/chutes)

        // If the game is over, don't play next round
        if (!game.isFinished()) {
          // Advance to next player
          game.moveToNextPlayer();
        }
      } catch (Exception ex) {
        System.err.println("Error in player move: " + ex.getMessage());
        ex.printStackTrace();
        statusText.setText("Error: " + ex.getMessage());

        // Enable roll button and reset animation state on error
        animationInProgress = false;
        rollButton.setDisable(false);
      }
    });

    System.out.println("Starting animation pause");
    pause.play();
  }

  private void updateAllPlayerPositions() {
    // Clear all player tokens
    boardView.clearPlayers();

    // Update each player's position
    for (Player player : game.getPlayers()) {
      Tile currentTile = player.getCurrentTile();
      if (currentTile != null) {
        boardView.updatePlayerPosition(player, currentTile.getTileId());
      }
    }
  }

  private void showGameOverDialog(Player winner) {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Game Over");
      alert.setHeaderText("We have a winner!");
      alert.setContentText(winner.getName() + " has won the game!");

      ButtonType newGameBtn = new ButtonType("Start New Game");
      ButtonType closeBtn = new ButtonType("Close");

      alert.getButtonTypes().setAll(newGameBtn, closeBtn);

      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == newGameBtn) {
        showNewGameDialog();
      }
    });
  }

  private void showErrorAlert(String title, String header, String content) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }

  // BoardGameObserver implementation
  @Override
  public void update(GameEvent event) {
    // Handle different game events
    switch (event.getType()) {
      case PLAYER_MOVED:
        // Player has moved normally - update view
        Player player = event.getPlayer();
        Tile destination = event.getToTile();

        // Update player position on board with animation
        Platform.runLater(() -> {
          boardView.updatePlayerPosition(player, destination.getTileId());
          playerInfoView.updatePlayerInfo(boardView);

          // Don't enable buttons yet - might be followed by ladder/chute event
        });
        break;

      case LADDER_CLIMBED:
        // Player climbed a ladder - update with animation after pause
        Platform.runLater(() -> {
          Player lPlayer = event.getPlayer();
          Tile lDestination = event.getToTile();

          // Update status text
          statusText.setText(lPlayer.getName() +
            " climbed a ladder to " + lDestination.getTileId() + "!");

          // Add slight delay before showing ladder movement
          PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
          pause.setOnFinished(e -> {
            // Show the climb animation
            boardView.updatePlayerPosition(lPlayer, lDestination.getTileId());
            playerInfoView.updatePlayerInfo(boardView);

            // Now we can enable the controls
            animationInProgress = false;
            updateButtonState();
          });
          pause.play();
        });
        break;

      case CHUTE_SLID:
        // Player slid down a chute
        Platform.runLater(() -> {
          Player cPlayer = event.getPlayer();
          Tile cDestination = event.getToTile();

          // Update status text
          statusText.setText(cPlayer.getName() +
            " slid down a chute to " + cDestination.getTileId() + "!");

          // Add slight delay before showing chute movement
          PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
          pause.setOnFinished(e -> {
            // Show the slide animation
            boardView.updatePlayerPosition(cPlayer, cDestination.getTileId());
            playerInfoView.updatePlayerInfo(boardView);

            // Now we can enable the controls
            animationInProgress = false;
            updateButtonState();
          });
          pause.play();
        });
        break;

      case GAME_OVER:
        // Game is over - show dialog
        Platform.runLater(() -> {
          // Update button state first
          animationInProgress = false;
          updateButtonState();

          // Show game over dialog
          showGameOverDialog(event.getPlayer());
        });
        break;

      case DICE_ROLLED:
        // Dice were rolled
        Platform.runLater(() -> {
          int[] diceValues = new int[game.getDice().getNumberOfDice()];
          for (int i = 0; i < diceValues.length; i++) {
            diceValues[i] = game.getDice().getDieValue(i);
          }
          diceView.setValues(diceValues);

          statusText.setText(event.getPlayer().getName() +
            " rolled a " + event.getDiceValue());
        });
        break;

      case GAME_RESET:
        // Game was reset
        Platform.runLater(() -> {
          updateAllPlayerPositions();
          playerInfoView.updatePlayerInfo(boardView);
          statusText.setText("Game reset! Ready to play.");
          animationInProgress = false;
          updateButtonState();
        });
        break;
    }
  }
}