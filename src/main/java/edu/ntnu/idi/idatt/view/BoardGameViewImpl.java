package edu.ntnu.idi.idatt.view;

import edu.ntnu.idi.idatt.action.LadderAction;
import edu.ntnu.idi.idatt.action.TileAction;
import edu.ntnu.idi.idatt.model.Board;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.model.Player;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class BoardGameViewImpl implements BoardGameView {
  private BorderPane root;
  private BoardView boardView;
  private PlayerInfoView playerInfoView;
  private DiceView diceView;
  private Label statusLabel;
  private Button rollButton;
  private Button newGameButton;
  private Button loadButton;
  private Button settingsButton;
  private HBox controls; // Define the controls HBox as a field

  private Runnable rollDiceHandler;
  private Runnable newGameHandler;
  private Runnable loadGameHandler;
  private Runnable settingsHandler;
  private Runnable turnCompletionCallback;

  private BoardGame model;

  public BoardGameViewImpl(BoardGame model) {
    this.model = model;
    createUI();
  }

  private void createUI() {
    root = new BorderPane();
    root.setPadding(new Insets(20));

    // Status bar at top
    statusLabel = new Label("Game ready to start");
    statusLabel.setId("statusText");

    settingsButton = new Button("Settings");
    settingsButton.setOnAction(e -> {
      if (settingsHandler != null) {
        settingsHandler.run();
      }
    });

    HBox topBar = new HBox(10, statusLabel, settingsButton);
    topBar.setAlignment(Pos.CENTER);
    root.setTop(topBar);

    // Board view in center
    boardView = new BoardView(model.getBoard());
    root.setCenter(boardView);

    // Player info on right
    playerInfoView = new PlayerInfoView(model.getPlayers(), boardView);
    root.setRight(playerInfoView);

    // Game controls at bottom
    rollButton = new Button("Roll Dice");
    newGameButton = new Button("New Game");
    loadButton = new Button("Load Game");

    diceView = new DiceView(model.getDice().getNumberOfDice());

    rollButton.setOnAction(e -> {
      if (rollDiceHandler != null) {
        rollDiceHandler.run();
      }
    });

    newGameButton.setOnAction(e -> {
      if (newGameHandler != null) {
        newGameHandler.run();
      }
    });

    loadButton.setOnAction(e -> {
      if (loadGameHandler != null) {
        loadGameHandler.run();
      }
    });

    controls = new HBox(15, rollButton, diceView, newGameButton, loadButton);
    controls.setAlignment(Pos.CENTER);
    controls.setPadding(new Insets(15, 0, 0, 0));

    root.setBottom(controls);
  }

  @Override
  public void updateDiceView(int diceCount) {
    // Remove old dice view
    controls.getChildren().remove(diceView);

    // Create new dice view
    diceView = new DiceView(diceCount);

    // Add back at the same position (index 1)
    controls.getChildren().add(1, diceView);
  }

  @Override
  public void renderBoard(Board board) {
    boardView.getChildren().clear();
    boardView = new BoardView(board);
    root.setCenter(boardView);

    // Place all players at their current positions
    for (Player player : model.getPlayers()) {
      if (player.getCurrentTile() != null) {
        updatePlayerPos(player, player.getCurrentTile().getTileId());
      }
    }
  }

  @Override
  public void updatePlayersList(List<Player> players) {
    playerInfoView = new PlayerInfoView(players, boardView);
    root.setRight(playerInfoView);
  }

  @Override
  public void setSettingsHandler(Runnable handler) {
    this.settingsHandler = handler;
  }

  @Override
  public void movePlayer(Player player, int oldPosition, int newPosition) {
    boardView.updatePlayerPos(player, newPosition);
    playerInfoView.updatePlayerInfo(boardView);
  }

  @Override
  public void movePlayerWithAnimation(Player player, int oldPosition, int newPosition, Runnable onComplete) {
    // First disable the roll button to prevent further actions during animation
    rollButton.setDisable(true);

    // Update the player's visual position with animation
    boardView.animatePlayerMove(player, newPosition, () -> {
      // Update player info after animation
      playerInfoView.updatePlayerInfo(boardView);

      // Re-enable roll button and call completion callback
      rollButton.setDisable(false);

      if (onComplete != null) {
        onComplete.run();
      }
    });
  }

  public void updatePlayerPos(Player player, int position) {
    boardView.updatePlayerPos(player, position);
  }

  @Override
  public void showDiceRoll(Player player, int roll, int[] diceValues) {
    // Update dice visualization with all values
    if (diceValues != null) {
      diceView.setValues(diceValues);
    } else {
      // Fallback to just showing the total (for backward compatibility)
      diceView.setValues(new int[]{roll});
    }
    diceView.roll();

    // Update status text
    statusLabel.setText(player.getName() + " rolled a " + roll);
  }

  @Override
  public void showDiceRoll(Player player, int roll) {
    // Backward compatibility method
    showDiceRoll(player, roll, null);
  }

  @Override
  public void showAction(Player player, TileAction action) {
    String actionDesc = "special action";
    if (action != null) {
      if (action instanceof LadderAction) {
        LadderAction ladderAction = (LadderAction) action;

        actionDesc = ladderAction.isLadder() ? "climbs up a ladder" : "slides down a chute";
      } else if (action.getClass().getSimpleName().contains("Skip")) {
        actionDesc = "will skip next turn";
      }
    }

    statusLabel.setText(player.getName() + " " + actionDesc);
  }


  @Override
  public void showActionWithAnimation(Player player, TileAction action, int destinationTileId, Runnable onComplete) {
    // Disable the roll button during animation
    rollButton.setDisable(true);

    // Create action description
    String actionDesc = "special action";
    if (action != null) {
      if (action instanceof LadderAction) {
        LadderAction ladderAction = (LadderAction) action;

        actionDesc = ladderAction.isLadder() ? "climbs up a ladder" : "slides down a chute";
      } else if (action.getClass().getSimpleName().contains("Skip")) {
        actionDesc = "will skip next turn";
      }
    }

    // Update status immediately
    statusLabel.setText(player.getName() + " " + actionDesc);

    // First, pause to let the player see where they landed
    PauseTransition pause = new PauseTransition(Duration.millis(500));

    // After pause, animate the movement
    pause.setOnFinished(event -> {
      boardView.animatePlayerMove(player, destinationTileId, () -> {
        // Update player info panel
        playerInfoView.updatePlayerInfo(boardView);

        // Re-enable roll button and execute completion callback
        rollButton.setDisable(false);

        if (onComplete != null) {
          onComplete.run();
        }
      });
    });

    pause.play();
  }

  @Override
  public void setTurnCompletionCallback(Runnable callback) {
    this.turnCompletionCallback = callback;
  }

  @Override
  public void highlightCurrentPlayer(Player player) {
    statusLabel.setText(player.getName() + "'s turn");
    playerInfoView.updatePlayerInfo(boardView);
  }

  @Override
  public void showGameOver(Player winner) {
    statusLabel.setText("Game Over! " + winner.getName() + " wins!");

    // Disable roll button
    rollButton.setDisable(true);

    // Use Platform.runLater to ensure dialog is shown after animation processing
    Platform.runLater(() -> {
      // Create alert dialog
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Game Over");
      alert.setHeaderText("We have a winner!");
      alert.setContentText(winner.getName() + " has won the game!");

      // Use show() instead of showAndWait() to avoid blocking
      alert.show();
    });
  }

  @Override
  public void showError(String title, String message) {
    // Also fix this method to avoid potential issues during animations
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.show();
    });
  }

  @Override
  public void showMessage(String title, String message) {
    // Also fix this method to avoid potential issues during animations
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.show();
    });
  }

  @Override
  public String showLoadDialog() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Load Game");
    fileChooser.getExtensionFilters().add(
      new FileChooser.ExtensionFilter("JSON Files", "*.json")
    );
    fileChooser.setInitialDirectory(
      new File(System.getProperty("user.dir") + "/src/main/resources")
    );

    File file = fileChooser.showOpenDialog(root.getScene().getWindow());
    return file != null ? file.getAbsolutePath() : null;
  }

  @Override
  public void setRollDiceHandler(Runnable handler) {
    this.rollDiceHandler = handler;
  }

  @Override
  public void setNewGameHandler(Runnable handler) {
    this.newGameHandler = handler;
  }

  @Override
  public void setLoadGameHandler(Runnable handler) {
    this.loadGameHandler = handler;
  }

  public Parent getRoot() {
    return root;
  }
}