package edu.ntnu.idi.idatt;

import edu.ntnu.idi.idatt.controller.BoardGameController;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.model.Player;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
  @Override
  public void start(Stage primaryStage) {
    // Create the model
    BoardGame game = new BoardGame();
    game.createBoard();
    game.createDice(1);


    // Create default players for testing
    Player player1 = new Player("Player 1", game, "TopHat");
    Player player2 = new Player("Player 2", game, "RaceCar");

    game.addPlayer(player1);
    game.addPlayer(player2);

    // Place players on starting position
    for (Player player : game.getPlayers()) {
      player.placeOnTile(game.getBoard().getTile(1));
    }

    // Create the layout
    BorderPane root = new BorderPane();
    Scene scene = new Scene(root, 900, 700);

    // Set up CSS styling
    scene.getStylesheets().add(getClass().getResource("/styles/game.css").toExternalForm());

    // Create the controller
    BoardGameController controller = new BoardGameController(game, root, primaryStage);
    // Set up the stage
    primaryStage.setTitle("Snakes and Ladders");
    primaryStage.setScene(scene);
    primaryStage.setMinWidth(800);
    primaryStage.setMinHeight(600);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}