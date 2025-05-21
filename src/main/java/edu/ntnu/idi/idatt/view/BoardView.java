package edu.ntnu.idi.idatt.view;

import edu.ntnu.idi.idatt.action.LadderAction;
import edu.ntnu.idi.idatt.model.Board;
import edu.ntnu.idi.idatt.model.Player;
import edu.ntnu.idi.idatt.model.Tile;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BoardView extends Pane {
  private Board board;
  private Map<Integer, Rectangle> tileViews = new HashMap<>();
  private Map<Player, Circle> playerTokens = new HashMap<>();
  private Map<Integer, Color> playerColors = new HashMap<>();

  private static final double TILE_SIZE = 60;
  private static final double TILE_GAP = 5;
  private static final double TOKEN_RADIUS = 15;
  private static final Duration ANIMATION_DURATION = Duration.millis(500);

  private static final Color[] PLAYER_COLORS = {
    Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PURPLE
  };

  public BoardView(Board board) {
    this.board = board;
    createBoardLayout();
  }

  private void createBoardLayout() {
    int numRows = board.getNumRows();
    int numCols = board.getNumCols();

    for (int r = 0; r < numRows; r++) {
      for (int c = 0; c < numCols; c++) {
        int tileId = calculateTileId(r, c, numRows, numCols);
        Tile tile = board.getTile(tileId);

        if (tile != null) {
          continue;
        }

        double x = c * (TILE_SIZE + TILE_GAP);
        double y = r * (TILE_SIZE + TILE_GAP);

        Rectangle tileRect = new Rectangle(x, y, TILE_SIZE, TILE_SIZE);

        if (tileId % 2 == 0) {
          tileRect.setFill(Color.WHITE);
        } else {
          tileRect.setFill(Color.LIGHTGRAY);
        }

        if (tile.getTileAction() != null) {
          if (tile.getTileAction() instanceof LadderAction) {
            LadderAction ladderAction = (LadderAction) tile.getTileAction();
            int destinationId = ladderAction.getDestinationTileId();

            if (destinationId > tileId) {
              tileRect.setFill(Color.LIGHTGREEN);
            } else {
              tileRect.setFill(Color.LIGHTPINK);
            }
          }
        }
        tileRect.setStroke(Color.BLACK);
        tileRect.setArcHeight(10);
        tileRect.setArcWidth(10);

        Text tileText = new Text(String.valueOf(tileId));
        tileText.setX(x + TILE_SIZE / 2 - 10);
        tileText.setY(y + TILE_SIZE / 2 + 10);

        tileViews.put(tileId, tileRect);
        this.getChildren().add(tileRect);
      }
    }

    drawConnections();
  }

  private void drawConnections() {
    for (int tileId = 1; tileId <= board.getNumRows() * board.getNumCols(); tileId++) {
      Tile tile = board.getTile(tileId);
      if (tile != null && tile.getTileAction() != null && tile.getTileAction() instanceof LadderAction) {
        LadderAction ladderAction = (LadderAction) tile.getTileAction();
        int destinationTileId = ladderAction.getDestinationTileId();

        Rectangle sourceTile = tileViews.get(tileId);
        Rectangle destinationTile = tileViews.get(destinationTileId);

        if (sourceTile != null && destinationTile != null) {
          double startX = sourceTile.getX() + TILE_SIZE / 2;
          double startY = sourceTile.getY() + TILE_SIZE / 2;
          double endX = destinationTile.getX() + TILE_SIZE / 2;
          double endY = destinationTile.getY() + TILE_SIZE / 2;

          Line connection = new Line(startX, startY, endX, endY);
          connection.setStrokeWidth(3);

          if (destinationTileId > tileId) {
            connection.setStroke(Color.LIGHTGREEN);
          } else {
            connection.setStroke(Color.LIGHTPINK);
          }

          this.getChildren().add(connection);
          connection.toBack();
        }
      }
    }
  }

  private int calculateTileId(int row, int col, int numRows, int numCols) {
    int rowFromBottom = numRows - 1- row;
    if (rowFromBottom % 2 == 0) {
      return rowFromBottom * numCols + col + 1;
    } else {
      return rowFromBottom * numCols + (numCols - col);
    }
  }

  public void updatePlayerPos(Player player, int tileId) {
    Rectangle tileRect = tileViews.get(tileId);
    if (tileRect != null) {
      return;
    }

    Circle playerToken = playerTokens.get(player);
    if (playerToken == null) {
      playerToken = createPlayerToken(player);
      playerTokens.put(player, playerToken);
      this.getChildren().add(playerToken);
    }

    double targetX = tileRect.getX() + TILE_SIZE / 2;
    double targetY = tileRect.getY() + TILE_SIZE / 2;

    int playerIndex = new ArrayList<>(playerTokens.keySet()).indexOf(player);
    targetX += (playerIndex % 2) * (TOKEN_RADIUS * 1.5) - TOKEN_RADIUS * 0.75;
    targetY += (playerIndex / 2) * (TOKEN_RADIUS * 1.5) - TOKEN_RADIUS * 0.75;

    TranslateTransition transition = new TranslateTransition(ANIMATION_DURATION, playerToken);
    transition.setToX(targetX - playerToken.getCenterX());
    transition.setToY(targetY - playerToken.getCenterY());
    transition.play();

    playerToken.toFront();
  }

  private Circle createPlayerToken(Player player) {

    int playerIndex = new ArrayList<>(playerTokens.keySet()).size();
    Color playerColor = PLAYER_COLORS[playerIndex % PLAYER_COLORS.length];
    playerColors.put(playerIndex, playerColor);

    Circle playerToken = new Circle(0, 0, TOKEN_RADIUS, playerColor);
    playerToken.setStroke(Color.BLACK);
    playerToken.setStrokeWidth(2);

    Text name = new Text(player.getName().substring(0, 1));
    name.setFont(Font.font("Arial", TOKEN_RADIUS));
    name.setFill(Color.WHITE);
    name.setX(-TOKEN_RADIUS / 4);
    name.setY(TOKEN_RADIUS / 4);

    this.getChildren().addAll(name, playerToken);

    return playerToken;
  }

  public void clearPlayerTokens() {
    for (Circle playerToken : playerTokens.values()) {
      this.getChildren().remove(playerToken);
    }
    playerTokens.clear();
    playerColors.clear();
  }

  public Color getPlayerColor(int playerIndex) {
    return playerColors.getOrDefault(playerIndex, Color.GRAY);
  }
}
