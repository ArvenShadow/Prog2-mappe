package edu.ntnu.idi.idatt.view.ingame;

import edu.ntnu.idi.idatt.model.Player;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class PlayerInfoView extends VBox {
  private List<Player> players;

  public PlayerInfoView(List<Player> players, BoardView boardView) {
    this.players = players;

    setPadding(new Insets(10));
    setSpacing(10);
    setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");

    Label titleLabel = new Label("Players");
    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    getChildren().add(titleLabel);

    updatePlayerInfo(boardView);
  }

  public void updatePlayerInfo(BoardView boardView) {
    while(getChildren().size() > 1) {
      getChildren().remove(1);
    }

    for (int i = 0; i < players.size(); i++) {
      Player player = players.get(i);

      VBox playerBox = new VBox(5);
      playerBox.setPadding(new Insets(5));
      playerBox.setStyle("-fx-background-color: #cccccc; -fx-border-radius: 5;");

      Label nameLabel = new Label(player.getName());
      nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

      Circle colorIndicator = new Circle(8);
      colorIndicator.setFill(boardView.getPlayerColor(i));
      colorIndicator.setStroke(Color.BLACK);

      int position = (player.getCurrentTile() != null) ? player.getCurrentTile().getTileId() : 0;
      Label positionLabel = new Label("Position: " + position);

      Label tokenLabel = new Label("Token: " + player.getTokenType());

      playerBox.getChildren().addAll(nameLabel, colorIndicator, positionLabel, tokenLabel);
      getChildren().add(playerBox);
    }
  }
}