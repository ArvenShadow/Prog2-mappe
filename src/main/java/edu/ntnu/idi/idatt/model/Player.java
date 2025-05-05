package edu.ntnu.idi.idatt.model;

import edu.ntnu.idi.idatt.event.GameEvent;

public class Player {
  private String name;
  private Tile currentTile;
  private BoardGame game;
  private String tokenType; // TopHat, RaceCar etc..

  public Player(String name, BoardGame game, String tokenType) {
    this.name = name;
    this.game = game;
    this.tokenType = tokenType;
  }

  public void placeOnTile(Tile tile) {
    this.currentTile = tile;
    System.out.println(name + " is place on tile " + tile.getTileId());
  }

  public void move(int steps) {
    if (currentTile == null) {
      throw new IllegalStateException("Player not on board");
    }

    System.out.println(name + " moving " + steps + " steps from tile " + currentTile.getTileId());

    // Find target tile by following next tile links
    Tile targetTile = currentTile;
    for (int i = 0; i < steps; i++) {
      if (targetTile.getNextTile() != null) {
        targetTile = targetTile.getNextTile();
      } else {
        // Reached the end of the board
        break;
      }
    }

    // Store old tile for event notification
    Tile oldTile = currentTile;

    // Move to the new tile
    placeOnTile(targetTile);

    // Notify that player has moved (needed for animation)
    game.notifyObservers(new GameEvent(GameEvent.EventType.PLAYER_MOVED, this, oldTile, targetTile));

    // After movement is complete, perform any tile actions
    targetTile.landAction(this);
  }

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

}
