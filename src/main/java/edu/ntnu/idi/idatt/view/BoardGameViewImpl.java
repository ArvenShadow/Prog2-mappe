package edu.ntnu.idi.idatt.view;

import edu.ntnu.idi.idatt.action.LadderAction;
import edu.ntnu.idi.idatt.action.TileAction;
import edu.ntnu.idi.idatt.model.Board;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.model.Player;

import edu.ntnu.idi.idatt.view.ingame.BoardView;
import edu.ntnu.idi.idatt.view.ingame.DiceView;
import edu.ntnu.idi.idatt.view.ingame.PlayerInfoView;
import edu.ntnu.idi.idatt.view.ingame.SettingsPanel;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class BoardGameViewImpl implements BoardGameView {
  private BorderPane root;
  private StackPane centerStack; // Stack to overlay settings on board
  private BoardView boardView;
  private PlayerInfoView playerInfoView;
  private DiceView diceView;
  private Label statusLabel;
  private Button rollButton;
  private Button newGameButton;
  private Button loadButton;
  private Button settingsButton;
  private HBox controls;

  private SettingsPanel settingsPanel;
  private boolean settingsVisible = false;

  private Runnable rollDiceHandler;
  private Runnable newGameHandler;
  private Runnable loadGameHandler;
  private Consumer<Integer> diceCountChangeHandler;
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
    settingsButton.setOnAction(e -> toggleSettings());

    HBox topBar = new HBox(10, statusLabel, settingsButton);
    topBar.setAlignment(Pos.CENTER);
    root.setTop(topBar);

    // Center stack for board and settings overlay
    centerStack = new StackPane();

    // Board view
    boardView = new BoardView(model.getBoard());
    centerStack.getChildren().add(boardView);

    // Settings panel (initially hidden)
    settingsPanel = new SettingsPanel(model.getDice().getNumberOfDice());
    settingsPanel.setVisible(false);
    settingsPanel.setManaged(false);

    settingsPanel.setOnDiceCountChanged(count -> {
      if (diceCountChangeHandler != null) {
        diceCountChangeHandler.accept(count);
      }
    });

    settingsPanel.setOnClose(() -> hideSettings());

    centerStack.getChildren().add(settingsPanel);
    StackPane.setAlignment(settingsPanel, Pos.TOP_CENTER);

    root.setCenter(centerStack);

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

  private void toggleSettings() {
    if (settingsVisible) {
      hideSettings();
    } else {
      showSettings();
    }
  }

  private void showSettings() {
    settingsPanel.updateDiceCount(model.getDice().getNumberOfDice());
    settingsPanel.setVisible(true);
    settingsPanel.setManaged(true);
    settingsVisible = true;
    settingsButton.setText("Hide Settings");

    // Disable game controls while settings are open
    rollButton.setDisable(true);
    newGameButton.setDisable(true);
    loadButton.setDisable(true);
  }

  private void hideSettings() {
    settingsPanel.setVisible(false);
    settingsPanel.setManaged(false);
    settingsVisible = false;
    settingsButton.setText("Settings");

    // Re-enable game controls
    rollButton.setDisable(false);
    newGameButton.setDisable(false);
    loadButton.setDisable(false);
  }

  @Override
  public void updateDiceView(int diceCount) {
    // Remove old dice view
    controls.getChildren().remove(diceView);

    // Create new dice view
    diceView = new DiceView(diceCount);

    // Add back at the same position (index 1)
    controls.getChildren().add(1, diceView);

    // Update settings panel if visible
    if (settingsVisible) {
      settingsPanel.updateDiceCount(diceCount);
    }
  }

  @Override
  public void renderBoard(Board board) {
    centerStack.getChildren().remove(boardView);
    boardView = new BoardView(board);
    centerStack.getChildren().add(0, boardView); // Add at index 0 to keep it behind settings

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

  public void setDiceCountChangeHandler(Consumer<Integer> handler) {
    this.diceCountChangeHandler = handler;
  }

  @Override
  public void movePlayer(Player player, int oldPosition, int newPosition) {
    boardView.updatePlayerPos(player, newPosition);
    playerInfoView.updatePlayerInfo(boardView);
  }

  @Override
  public void movePlayerWithAnimation(Player player, int oldPosition, int newPosition, Runnable onComplete) {
    rollButton.setDisable(true);

    boardView.animatePlayerMove(player, newPosition, () -> {
      playerInfoView.updatePlayerInfo(boardView);
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
    if (diceValues != null) {
      diceView.setValues(diceValues);
    } else {
      diceView.setValues(new int[]{roll});
    }
    diceView.roll();

    statusLabel.setText(player.getName() + " rolled a " + roll);
  }

  @Override
  public void showDiceRoll(Player player, int roll) {
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
    rollButton.setDisable(true);

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

    PauseTransition pause = new PauseTransition(Duration.millis(500));
    pause.setOnFinished(event -> {
      boardView.animatePlayerMove(player, destinationTileId, () -> {
        playerInfoView.updatePlayerInfo(boardView);
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
    rollButton.setDisable(true);

    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Game Over");
      alert.setHeaderText("We have a winner!");
      alert.setContentText(winner.getName() + " has won the game!");
      alert.show();
    });
  }

  @Override
  public void showError(String title, String message) {
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

  @Override
  public void setSettingsHandler(Runnable handler) {
    // No longer needed as settings are handled internally
  }

  public Parent getRoot() {
    return root;
  }
}