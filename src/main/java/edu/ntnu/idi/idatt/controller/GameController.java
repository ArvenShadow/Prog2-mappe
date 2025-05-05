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
import java.util.Arrays;
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

    Button settingsButton = new Button("Settings");
    settingsButton.setOnAction(e -> showSettingsDialog());

    Button loadGameButton = new Button("Load Game");
    loadGameButton.setOnAction(e -> handleLoadGame());

    Button saveGameButton = new Button("Save Game");
    saveGameButton.setOnAction(e -> handleSaveGame());

    // Create controls box
    HBox controlsBox = new HBox(10, rollButton, newGameButton, settingsButton, loadGameButton, saveGameButton);
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

  private void showSettingsDialog() {
    // Create a dialog for game settings
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Game Settings");
    dialog.setHeaderText("Adjust game settings");

    // Set the button types
    ButtonType applyButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

    // Create the settings grid
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    // Dice count selector
    Label diceLabel = new Label("Number of Dice:");
    int currentDiceCount = game.getDice() != null ? game.getDice().getNumberOfDice() : 2;
    Spinner<Integer> diceSpinner = new Spinner<>(1, Dice.getMaxDice(), currentDiceCount);
    diceSpinner.setEditable(true);

    grid.add(diceLabel, 0, 0);
    grid.add(diceSpinner, 1, 0);

    // Player token display (read-only)
    Label playerLabel = new Label("Current Players:");
    grid.add(playerLabel, 0, 1);

    VBox playersBox = new VBox(5);
    for (Player player : game.getPlayers()) {
      HBox playerInfo = new HBox(10);
      playerInfo.getChildren().addAll(
        new Label(player.getName() + ":"),
        new Label(player.getTokenType())
      );
      playersBox.getChildren().add(playerInfo);
    }
    grid.add(playersBox, 0, 2, 2, 1);

    dialog.getDialogPane().setContent(grid);

    // Show dialog and handle result
    Optional<ButtonType> result = dialog.showAndWait();
    if (result.isPresent() && result.get() == applyButtonType) {
      // Apply settings changes
      int newDiceCount = diceSpinner.getValue();

      // Only apply changes if dice count is different
      if (game.getDice() != null && newDiceCount != game.getDice().getNumberOfDice()) {
        try {
          // Update the model
          game.getDice().setNumberOfDice(newDiceCount);

          // Update the view
          diceView.setDiceCount(newDiceCount);

          // Update status
          statusText.setText("Number of dice changed to " + newDiceCount);
        } catch (Exception e) {
          showErrorAlert("Settings Error", "Error changing dice count", e.getMessage());
        }
      }
    }
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
      if (game.getPlayers().isEmpty()) {
        statusText.setText("New game started! Add players to begin.");
      } else {
        Player currentPlayer = game.getCurrentPlayer();
        if (currentPlayer != null) {
          statusText.setText("New game started! " + currentPlayer.getName() + " goes first.");
        } else {
          statusText.setText("New game started! Roll the dice to start.");
        }
      }

      // Enable/disable roll button
      rollButton.setDisable(game.getPlayers().isEmpty());

    } catch (Exception e) {
      statusText.setText("Error starting new game: " + e.getMessage());
      e.printStackTrace(); // Add this to see the full stack trace
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
    // Debug output
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

    // Debug player sequence
    game.debugPlayerSequence();

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

        // IMPORTANT: Do NOT call moveToNextPlayer() here!
        // It's already called in game.playOneRound()

        // If the game is over, don't play next round
        if (!game.isFinished()) {
          // Show who's next
          Player nextPlayer = game.getCurrentPlayer();
          if (nextPlayer != null) {
            System.out.println("Next player will be: " + nextPlayer.getName());
          }
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

  private void updateButtonState() {
    rollButton.setDisable(animationInProgress ||
      game.isFinished() ||
      game.getPlayers().isEmpty());
  }

  // BoardGameObserver implementation
  @Override
  public void update(GameEvent event) {
    if (event == null) {
      System.err.println("Received null event!");
      return;
    }

    System.out.println("Received game event: " + event.getType());

    // Handle different game events
    switch (event.getType()) {
      case PLAYER_MOVED:
        // Player has moved normally - update view
        handlePlayerMoved(event);
        break;

      case LADDER_CLIMBED:
        // Player climbed a ladder
        handleLadderClimbed(event);
        break;

      case CHUTE_SLID:
        // Player slid down a chute
        handleChuteSlid(event);
        break;

      case GAME_OVER:
        // Game is over - show dialog
        handleGameOver(event);
        break;

      case DICE_ROLLED:
        // Dice were rolled
        handleDiceRolled(event);
        break;

      case GAME_RESET:
        // Game was reset
        handleGameReset();
        break;

      default:
        System.err.println("Unknown event type: " + event.getType());
    }
  }

  private void handlePlayerMoved(GameEvent event) {
    Player player = event.getPlayer();
    Tile destination = event.getToTile();

    if (player == null || destination == null) {
      System.err.println("Invalid player movement event: player or destination is null");
      return;
    }

    // Update player position on board with animation
    Platform.runLater(() -> {
      boardView.updatePlayerPosition(player, destination.getTileId());
      playerInfoView.updatePlayerInfo(boardView);

      // Check if the destination tile has an action
      if (destination.getTileAction() == null) {
        // No further animations expected, reset state
        animationInProgress = false;
        updateButtonState();

        // Update status for next player
        if (!game.isFinished()) {
          Player nextPlayer = game.getCurrentPlayer();
          if (nextPlayer != null) {
            statusText.setText(nextPlayer.getName() + "'s turn to roll");
          }
        }
      }
    });
  }

  private void handleLadderClimbed(GameEvent event) {
    Platform.runLater(() -> {
      Player player = event.getPlayer();
      Tile destination = event.getToTile();

      if (player == null || destination == null) {
        System.err.println("Invalid ladder event: player or destination is null");
        return;
      }

      // Update status text
      statusText.setText(player.getName() +
        " climbed a ladder to " + destination.getTileId() + "!");

      // Add slight delay before showing ladder movement
      PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
      pause.setOnFinished(e -> {
        // Show the climb animation
        boardView.updatePlayerPosition(player, destination.getTileId());
        playerInfoView.updatePlayerInfo(boardView);

        // Now we can enable the controls
        animationInProgress = false;
        updateButtonState();

        // Update status for next player
        if (!game.isFinished()) {
          Player nextPlayer = game.getCurrentPlayer();
          if (nextPlayer != null) {
            statusText.setText(nextPlayer.getName() + "'s turn to roll");
          }
        }
      });
      pause.play();
    });
  }

  private void handleChuteSlid(GameEvent event) {
    Platform.runLater(() -> {
      Player player = event.getPlayer();
      Tile destination = event.getToTile();

      if (player == null || destination == null) {
        System.err.println("Invalid chute event: player or destination is null");
        return;
      }

      // Update status text
      statusText.setText(player.getName() +
        " slid down a chute to " + destination.getTileId() + "!");

      // Add slight delay before showing chute movement
      PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
      pause.setOnFinished(e -> {
        // Show the slide animation
        boardView.updatePlayerPosition(player, destination.getTileId());
        playerInfoView.updatePlayerInfo(boardView);

        // Now we can enable the controls
        animationInProgress = false;
        updateButtonState();

        // Update status for next player
        if (!game.isFinished()) {
          Player nextPlayer = game.getCurrentPlayer();
          if (nextPlayer != null) {
            statusText.setText(nextPlayer.getName() + "'s turn to roll");
          }
        }
      });
      pause.play();
    });
  }

  private void handleGameOver(GameEvent event) {
    Platform.runLater(() -> {
      // Update button state first
      animationInProgress = false;
      updateButtonState();

      // Show game over dialog with winner
      Player winner = event.getPlayer();
      if (winner != null) {
        showGameOverDialog(winner);
      } else {
        System.err.println("Game over event with null player!");
        statusText.setText("Game over!");
      }
    });
  }

  private void handleDiceRolled(GameEvent event) {
    Platform.runLater(() -> {
      if (event.getPlayer() == null) {
        System.err.println("Dice rolled event with null player!");
        return;
      }

      int[] diceValues = new int[game.getDice().getNumberOfDice()];
      for (int i = 0; i < diceValues.length; i++) {
        diceValues[i] = game.getDice().getDieValue(i);
      }
      diceView.setValues(diceValues);

      statusText.setText(event.getPlayer().getName() +
        " rolled a " + event.getDiceValue());
    });
  }

  private void handleGameReset() {
    Platform.runLater(() -> {
      updateAllPlayerPositions();
      playerInfoView.updatePlayerInfo(boardView);

      if (game.getPlayers().isEmpty()) {
        statusText.setText("Game reset! Add players to begin.");
      } else {
        statusText.setText("Game reset! " + game.getCurrentPlayer().getName() + " starts.");
      }

      animationInProgress = false;
      updateButtonState();
    });
  }

  private void handleSaveGame() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Game Board");
    fileChooser.getExtensionFilters().add(
      new FileChooser.ExtensionFilter("JSON Files", "*.json")
    );

    File file = fileChooser.showSaveDialog(primaryStage);
    if (file != null) {
      try {
        game.saveBoardToFile(file.getAbsolutePath());

        // Prompt to save players if desired
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Save Players");
        alert.setHeaderText("Would you like to save the player list as well?");
        alert.setContentText("Players will be saved to a separate CSV file.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
          // Create default player file name based on board file
          String playerFilePath = file.getAbsolutePath().replace(".json", "_players.csv");
          game.savePlayersToFile(playerFilePath);
          statusText.setText("Game board and players saved successfully!");
        } else {
          statusText.setText("Game board saved successfully!");
        }
      } catch (BoardGameException e) {
        showErrorAlert("Save Error", "Could not save game", e.getMessage());
      }
    }
  }

  private void handleLoadGame() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Load Game Board");
    fileChooser.getExtensionFilters().add(
      new FileChooser.ExtensionFilter("JSON Files", "*.json")
    );

    File file = fileChooser.showOpenDialog(primaryStage);
    if (file != null) {
      try {
        // Load the board
        game.loadBoardFromFile(file.getAbsolutePath());

        // Update board view
        boardView.setBoard(game.getBoard());

        // Prompt to load players if desired
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Load Players");
        alert.setHeaderText("Would you like to load players as well?");
        alert.setContentText("Players should be in a CSV file.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
          FileChooser playerChooser = new FileChooser();
          playerChooser.setTitle("Load Players File");
          playerChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
          );

          // Try to set initial directory to the same as the board file
          playerChooser.setInitialDirectory(file.getParentFile());

          File playerFile = playerChooser.showOpenDialog(primaryStage);
          if (playerFile != null) {
            game.loadPlayersFromFile(playerFile.getAbsolutePath());

            // Update player view
            playerInfoView = new PlayerInfoView(game.getPlayers(), boardView);
            rootPane.setRight(playerInfoView);
          }
        }

        // Reset game and update UI
        game.reset();
        updateAllPlayerPositions();
        statusText.setText("Game loaded successfully!");
        rollButton.setDisable(game.getPlayers().isEmpty());

      } catch (BoardGameException e) {
        showErrorAlert("Load Error", "Could not load game", e.getMessage());
      }
    }
  }
}