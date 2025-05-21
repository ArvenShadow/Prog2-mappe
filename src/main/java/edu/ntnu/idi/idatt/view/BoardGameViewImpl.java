package edu.ntnu.idi.idatt.view;

import edu.ntnu.idi.idatt.action.TileAction;
import edu.ntnu.idi.idatt.model.Board;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.model.Player;
import edu.ntnu.idi.idatt.view.BoardView;
import edu.ntnu.idi.idatt.view.DiceView;
import edu.ntnu.idi.idatt.view.PlayerInfoView;

import javafx.animation.PauseTransition;
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
  private Button saveButton;
  private Button loadButton;

  private Runnable rollDiceHandler;
  private Runnable newGameHandler;
  private Runnable saveGameHandler;
  private Runnable loadGameHandler;

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

    HBox topBar = new HBox(10, statusLabel);
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
    saveButton = new Button("Save Game");
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

    saveButton.setOnAction(e -> {
      if (saveGameHandler != null) {
        saveGameHandler.run();
      }
    });

    loadButton.setOnAction(e -> {
      if (loadGameHandler != null) {
        loadGameHandler.run();
      }
    });

    HBox controls = new HBox(15, rollButton, diceView, newGameButton, saveButton, loadButton);
    controls.setAlignment(Pos.CENTER);
    controls.setPadding(new Insets(15, 0, 0, 0));

    root.setBottom(controls);
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
  public void movePlayer(Player player, int oldPosition, int newPosition) {
    boardView.updatePlayerPos(player, newPosition);
    playerInfoView.updatePlayerInfo(boardView);
  }

  public void updatePlayerPos(Player player, int position) {
    boardView.updatePlayerPos(player, position);
  }

  @Override
  public void showDiceRoll(Player player, int roll) {
    // Update dice visualization
    diceView.setValues(new int[]{roll});
    diceView.roll();

    // Update status text
    statusLabel.setText(player.getName() + " rolled a " + roll);
  }

  @Override
  public void showAction(Player player, TileAction action) {
    String actionDesc = "special action";
    if (action != null) {
      // Try to get more descriptive action text based on action type
      if (action.getClass().getSimpleName().contains("Ladder")) {
        boolean isUp = action.toString().contains("up") ||
          !action.toString().contains("down");
        actionDesc = isUp ? "climbs up a ladder" : "slides down a chute";
      } else if (action.getClass().getSimpleName().contains("Skip")) {
        actionDesc = "will skip next turn";
      }
    }

    statusLabel.setText(player.getName() + " " + actionDesc);
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

    // Create alert dialog
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Game Over");
    alert.setHeaderText("We have a winner!");
    alert.setContentText(winner.getName() + " has won the game!");

    // Show alert after a short delay
    PauseTransition delay = new PauseTransition(Duration.seconds(1));
    delay.setOnFinished(e -> alert.showAndWait());
    delay.play();
  }

  @Override
  public void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  @Override
  public void showMessage(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  @Override
  public String showSaveDialog() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Game");
    fileChooser.getExtensionFilters().add(
      new FileChooser.ExtensionFilter("JSON Files", "*.json")
    );
    fileChooser.setInitialDirectory(
      new File(System.getProperty("user.dir") + "/src/main/resources")
    );

    File file = fileChooser.showSaveDialog(root.getScene().getWindow());
    return file != null ? file.getAbsolutePath() : null;
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
  public void setSaveGameHandler(Runnable handler) {
    this.saveGameHandler = handler;
  }

  @Override
  public void setLoadGameHandler(Runnable handler) {
    this.loadGameHandler = handler;
  }

  public Parent getRoot() {
    return root;
  }
}