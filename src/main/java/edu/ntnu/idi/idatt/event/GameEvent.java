package edu.ntnu.idi.idatt.event;

import edu.ntnu.idi.idatt.model.Player;
import edu.ntnu.idi.idatt.model.Tile;

public class GameEvent {
  private EventType type;
  private Player player;
  private Tile fromTile;
  private Tile toTile;
  private int diceValue; // Added for dice roll events

  public GameEvent(EventType type, Player player, Tile fromTile, Tile toTile) {
    this.type = type;
    this.player = player;
    this.fromTile = fromTile;
    this.toTile = toTile;
    this.diceValue = 0;
  }

  public GameEvent(EventType type, Player player, Tile fromTile, Tile toTile, int diceValue) {
    this.type = type;
    this.player = player;
    this.fromTile = fromTile;
    this.toTile = toTile;
    this.diceValue = diceValue;
  }

  public enum EventType {
    PLAYER_MOVED,
    LADDER_CLIMBED,
    CHUTE_SLID,
    GAME_OVER,
    DICE_ROLLED,
    GAME_RESET
  }

  // Getters
  public EventType getType() {
    return type;
  }

  public Player getPlayer() {
    return player;
  }

  public Tile getFromTile() {
    return fromTile;
  }

  public Tile getToTile() {
    return toTile;
  }

  public int getDiceValue() {
    return diceValue;
  }
}