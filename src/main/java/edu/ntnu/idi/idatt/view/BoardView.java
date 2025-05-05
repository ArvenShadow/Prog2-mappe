package edu.ntnu.idi.idatt.view;

import edu.ntnu.idi.idatt.action.LadderAction;
import edu.ntnu.idi.idatt.model.Board;
import edu.ntnu.idi.idatt.model.Player;
import edu.ntnu.idi.idatt.model.Tile;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.util.Duration;

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

  // Array of colors for players
  private static final Color[] PLAYER_COLORS = {
    Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PURPLE
  };

  public BoardView(Board board) {
    this.board = board;
    createBoardLayout();
  }

  public void setBoard(Board board) {
    this.board = board;
    getChildren().clear();
    tileViews.clear();
    createBoardLayout();

    // Reposition existing player tokens
    for (Player player : playerTokens.keySet()) {
      if (player.getCurrentTile() != null) {
        updatePlayerPosition(player, player.getCurrentTile().getTileId());
      }
    }
  }

  private void createBoardLayout() {
    int numRows = board.getNumRows();
    int numCols = board.getNumCols();

    // Draw the tiles
    for (int r = 0; r < numRows; r++) {
      for (int c = 0; c < numCols; c++) {
        int tileId = calculateTileId(r, c, numRows, numCols);
        Tile tile = board.getTile(tileId);

        if (tile == null) {
          continue;
        }

        double x = c * (TILE_SIZE + TILE_GAP);
        double y = r * (TILE_SIZE + TILE_GAP);

        Rectangle tileRect = new Rectangle(x, y, TILE_SIZE, TILE_SIZE);

        // Color tiles based on action
        if (tile.getTileAction() != null) {
          if (tile.getTileAction() instanceof LadderAction) {
            LadderAction action = (LadderAction) tile.getTileAction();
            int destId = action.getDestinationTileId();

            // Green for up ladders, red for down chutes
            if (destId > tileId) {
              tileRect.setFill(Color.LIGHTGREEN);
            } else {
              tileRect.setFill(Color.LIGHTPINK);
            }
          }
        } else {
          tileRect.setFill(Color.WHITE);
        }

        tileRect.setStroke(Color.BLACK);
        tileRect.setArcHeight(10);
        tileRect.setArcWidth(10);

        // Add tile number
        Text tileText = new Text(String.valueOf(tileId));
        tileText.setX(x + TILE_SIZE / 2 - 5);
        tileText.setY(y + TILE_SIZE / 2 + 5);

        tileViews.put(tileId, tileRect);
        getChildren().addAll(tileRect, tileText);
      }
    }

    // Draw the connections (ladders and chutes)
    drawConnections();
  }

  private void drawConnections() {
    // Loop through all tiles
    for (int tileId = 1; tileId <= board.getNumRows() * board.getNumCols(); tileId++) {
      Tile tile = board.getTile(tileId);
      if (tile != null && tile.getTileAction() != null && tile.getTileAction() instanceof LadderAction) {
        LadderAction action = (LadderAction) tile.getTileAction();
        int destId = action.getDestinationTileId();

        Rectangle sourceTile = tileViews.get(tileId);
        Rectangle destTile = tileViews.get(destId);

        if (sourceTile != null && destTile != null) {
          // Draw the connection
          double startX = sourceTile.getX() + TILE_SIZE / 2;
          double startY = sourceTile.getY() + TILE_SIZE / 2;
          double endX = destTile.getX() + TILE_SIZE / 2;
          double endY = destTile.getY() + TILE_SIZE / 2;

          Line connection = new Line(startX, startY, endX, endY);
          connection.setStrokeWidth(3);

          // Green for ladders (going up), red for chutes (going down)
          if (destId > tileId) {
            connection.setStroke(Color.GREEN);
          } else {
            connection.setStroke(Color.RED);
          }

          getChildren().add(connection);
          connection.toBack(); // Ensure lines are behind tiles
        }
      }
    }
  }

  private int calculateTileId(int row, int col, int numRows, int numCols) {
    int rowFromBottom = numRows - 1 - row;
    if (rowFromBottom % 2 == 0) {
      // Even rows (from bottom) go left to right
      return rowFromBottom * numCols + col + 1;
    } else {
      // Odd rows (from bottom) go right to left
      return rowFromBottom * numCols + (numCols - col);
    }
  }

  public void updatePlayerPosition(Player player, int tileId) {
    Rectangle tileRect = tileViews.get(tileId);
    if (tileRect == null) {
      return;
    }

    // Get or create player token
    Circle playerToken = playerTokens.get(player);
    if (playerToken == null) {
      // First time positioning this player
      playerToken = createPlayerToken(player);
      playerTokens.put(player, playerToken);
      getChildren().add(playerToken);
    }

    // Calculate position
    double targetX = tileRect.getX() + TILE_SIZE / 2;
    double targetY = tileRect.getY() + TILE_SIZE / 2;

    // Offset multiple players on the same tile
    int playerIndex = new java.util.ArrayList<>(playerTokens.keySet()).indexOf(player);
    targetX += (playerIndex % 2) * (TOKEN_RADIUS * 1.5) - TOKEN_RADIUS * 0.75;
    targetY += (playerIndex / 2) * (TOKEN_RADIUS * 1.5) - TOKEN_RADIUS * 0.75;

    // Animate movement
    TranslateTransition transition = new TranslateTransition(ANIMATION_DURATION, playerToken);
    transition.setToX(targetX - playerToken.getCenterX());
    transition.setToY(targetY - playerToken.getCenterY());
    transition.play();

    // Bring to front
    playerToken.toFront();
  }

  private Circle createPlayerToken(Player player) {
    // Assign a color based on the player index
    int playerIndex = new java.util.ArrayList<>(playerTokens.keySet()).size();
    Color playerColor = PLAYER_COLORS[playerIndex % PLAYER_COLORS.length];
    playerColors.put(playerIndex, playerColor);

    // Create the token
    Circle token = new Circle(0, 0, TOKEN_RADIUS, playerColor);
    token.setStroke(Color.BLACK);
    token.setStrokeWidth(2);

    // Add player name label
    Text nameText = new Text(player.getName().substring(0, 1));
    nameText.setFont(Font.font("Arial", TOKEN_RADIUS));
    nameText.setFill(Color.WHITE);
    nameText.setX(-TOKEN_RADIUS/4);
    nameText.setY(TOKEN_RADIUS/4);

    return token;
  }

  public void clearPlayers() {
    for (Circle token : playerTokens.values()) {
      getChildren().remove(token);
    }
    playerTokens.clear();
  }

  public Map<Player, Circle> getPlayerTokens() {
    return playerTokens;
  }

  public Color getPlayerColor(int playerIndex) {
    return playerColors.getOrDefault(playerIndex, Color.GRAY);
  }
}