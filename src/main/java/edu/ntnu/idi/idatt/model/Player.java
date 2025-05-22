package edu.ntnu.idi.idatt.model;

import edu.ntnu.idi.idatt.event.GameEvent;
import edu.ntnu.idi.idatt.event.GameEventType;
import edu.ntnu.idi.idatt.event.ObservableGame;

public class Player {
  private String name;
  private Tile currentTile;
  private BoardGame game;
  private String tokenType; // TopHat, RaceCar etc..
  private boolean skipsNextTurn = false;

  public Player(String name, BoardGame game, String tokenType) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Player name cannot be null or empty");
    }
    if (game == null) {
      throw new IllegalArgumentException("BoardGame cannot be null");
    }
    if (tokenType == null || tokenType.trim().isEmpty()) {
      throw new IllegalArgumentException("Token type cannot be null or empty");
    }

    this.name = name.trim();
    this.game = game;
    this.tokenType = tokenType;
  }

  public void placeOnTile(Tile tile) {
    this.currentTile = tile;
    System.out.println(name + " is place on tile " + tile.getTileId());
  }

  /**
   * Moves the player forward by a specified number of steps.
   * @param steps The number of steps to move
   * @throws IllegalStateException if player is not on board or target tile not found
   */
  public void move(int steps) {
    if (currentTile == null) {
      throw new IllegalStateException("Player not on board");
    }

    int currentPosition = currentTile.getTileId();
    int newPosition = currentPosition + steps;

    // Get the board's maximum tile id to check for victory condition
    int maxTileId = game.getBoard().getFinalTileId();

    // Handle case where player would move beyond the board
    if (newPosition > maxTileId) {
      newPosition = maxTileId; // Move to final tile if would go beyond
    }

    Tile targetTile = game.getBoard().getTile(newPosition);

    if (targetTile != null) {
      System.out.println(name + " moves " + steps + " steps to tile " + targetTile.getTileId());

      // Track old position for event notification
      Tile oldTile = currentTile;

      // Place player on new tile
      placeOnTile(targetTile);

      // If game implements ObservableGame, notify about the move
      if (game instanceof ObservableGame) {
        ((ObservableGame) game).notifyObservers(
          new GameEvent(GameEventType.PLAYER_MOVED, this, oldTile.getTileId(), targetTile.getTileId())
        );
      }

      // Execute tile action if present
      targetTile.landAction(this);
    } else {
      throw new IllegalStateException("No tile at position " + newPosition);
    }
  }

  /**
   * Checks if player has won.
   * @param finalTileId The ID of the final tile
   * @return true if player has reached or passed the final tile
   */
  public boolean hasWon(int finalTileId) {
    return currentTile != null && currentTile.getTileId() >= finalTileId;
  }
  public String getName() {
    return name;
  }

  public Tile getCurrentTile() {
    return currentTile;
  }

  public BoardGame getGame() {
    return game;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setSkipsNextTurn(boolean skipsNextTurn) {
    this.skipsNextTurn = skipsNextTurn;
  }
  public boolean getSkipsNextTurn() {
    return skipsNextTurn;
  }

}
