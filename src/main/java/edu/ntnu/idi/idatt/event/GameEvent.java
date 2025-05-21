package edu.ntnu.idi.idatt.event;

import edu.ntnu.idi.idatt.model.Player;
import edu.ntnu.idi.idatt.action.TileAction;

public class GameEvent {
  private GameEventType type;
  private Player player;
  private int oldPosition;
  private int newPosition;
  private int diceRoll;
  private int[] diceValues;
  private TileAction action;

  // Constructor for general events
  public GameEvent(GameEventType type, Player player) {
    this.type = type;
    this.player = player;
  }

  // Constructor for dice roll events
  public GameEvent(GameEventType type, Player player, int diceRoll) {
    this(type, player);
    this.diceRoll = diceRoll;
  }

  // New constructor for dice roll events with individual values
  public GameEvent(GameEventType type, Player player, int diceRoll, int[] diceValues) {
    this(type, player);
    this.diceRoll = diceRoll;
    this.diceValues = diceValues;
  }

  // Constructor for player movement events
  public GameEvent(GameEventType type, Player player, int oldPosition, int newPosition) {
    this(type, player);
    this.oldPosition = oldPosition;
    this.newPosition = newPosition;
  }

  // Constructor for action events
  public GameEvent(GameEventType type, Player player, TileAction action) {
    this(type, player);
    this.action = action;
  }

  public GameEventType getType() {
    return type;
  }

  public Player getPlayer() {
    return player;
  }

  public int getOldPosition() {
    return oldPosition;
  }

  public int getNewPosition() {
    return newPosition;
  }

  public int getDiceRoll() {
    return diceRoll;
  }

  public int[] getDiceValues() {
    return diceValues;
  }

  public TileAction getAction() {
    return action;
  }
}