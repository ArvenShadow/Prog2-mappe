package edu.ntnu.idi.idatt.model;

public class Player {
  private String name;
  private Tile currentTile;
  private BoardGame game;
  private String tokenType; // TopHat, RaceCar etc..
  private boolean skipsNextTurn = false;

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

    int currentPosition = currentTile.getTileId();
    int newPosition = currentPosition + steps;

    Tile targetTile = game.getBoard().getTile(newPosition);

    if (targetTile != null) {
      System.out.println(name + " moves " + steps + " steps to tile " + targetTile.getTileId());
      placeOnTile(targetTile);
      targetTile.landAction(this);

    } else {
      throw new IllegalStateException("No tile at position " + newPosition);
    }
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

  public void setSkipsNextTurn(boolean skipsNextTurn) {
    this.skipsNextTurn = skipsNextTurn;
  }
  public boolean getSkipsNextTurn() {
    return skipsNextTurn;
  }

}
