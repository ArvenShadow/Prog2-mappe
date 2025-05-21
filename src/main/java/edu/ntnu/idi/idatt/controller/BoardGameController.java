package edu.ntnu.idi.idatt.controller;

import edu.ntnu.idi.idatt.event.BoardGameObserver;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.view.BoardView;
import edu.ntnu.idi.idatt.view.DiceView;
import edu.ntnu.idi.idatt.view.PlayerInfoView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class BoardGameController {  // implements BoardGameObserver
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


  public BoardGameController(BoardGame game, BorderPane rootPane, Stage primaryStage) {
    this.game = game;
    this.primaryStage = primaryStage;
    this.rootPane = rootPane;

    //game.addObserver(this);

    setupUI();
  }

  private void setupUI() {
    boardView = new BoardView(game.getBoard());

    statusText = new Text("Welcome to Snakes and Ladders!");
    statusText.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    diceView = new DiceView();

    rollButton = new Button("Roll Dice");
    //rollButton.setOnAction(e -> handleRollDice());
    rollButton.setDisable(game.getPlayers().isEmpty() || animationInProgress);

    newGameButton = new Button("New Game");
    //newGameButton.setOnAction(e -> newGameDialog());

    Button settingsButton = new Button("Settings");
    //settingsButton.setOnAction(e -> showSettings());

    Button loadGameButton = new Button("Load Game");
    //loadGameButton.setOnAction(e -> handleLoadGame());

    HBox controlsBox = new HBox(rollButton, newGameButton, settingsButton, loadGameButton);
    controlsBox.setAlignment(Pos.CENTER);
    controlsBox.setPadding(new Insets(10));

    HBox topBar = new HBox(20, statusText, diceView);
    topBar.setAlignment(Pos.CENTER);
    topBar.setPadding(new Insets(10));

    playerInfoView = new PlayerInfoView(game.getPlayers(), boardView);

    rootPane.setCenter(boardView);
    rootPane.setTop(topBar);
    rootPane.setBottom(controlsBox);
    rootPane.setRight(playerInfoView);

    rootPane.setPadding(new Insets(10));
  }
}
