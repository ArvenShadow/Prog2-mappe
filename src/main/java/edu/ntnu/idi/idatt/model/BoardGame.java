package edu.ntnu.idi.idatt.model;

import edu.ntnu.idi.idatt.event.GameEvent;
import edu.ntnu.idi.idatt.event.GameEventType;
import edu.ntnu.idi.idatt.event.GameObserver;
import edu.ntnu.idi.idatt.event.ObservableGame;
import edu.ntnu.idi.idatt.exception.BoardGameException;
import edu.ntnu.idi.idatt.action.TileAction;
import edu.ntnu.idi.idatt.io.BoardJsonHandler;

import java.util.ArrayList;
import java.util.List;

public class BoardGame implements ObservableGame {
  private Board board;
  private Dice dice;
  private List<Player> players;
  private int currentPlayerIndex;
  private boolean gameFinished;
  private Player winner;

  private List<GameObserver> observers = new ArrayList<>();

  public BoardGame() {
    this.players = new ArrayList<>();
    this.currentPlayerIndex = 0;
    this.gameFinished = false;
  }

  public void createBoard() {
    try {
      this.board = edu.ntnu.idi.idatt.factory.BoardGameFactory.createBoard();
      notifyObservers(new GameEvent(GameEventType.BOARD_CREATED, null));
    } catch (RuntimeException e) {
      // Log error or handle exception
      System.err.println("Error creating board: " + e.getMessage());
      throw e;
    }
  }

  public void createDice(int numberOfDice) {
    this.dice = new Dice(numberOfDice);
    notifyObservers(new GameEvent(GameEventType.DICE_CREATED, null));
  }

  public void addPlayer(Player player) {
    if (player == null) {
      throw new IllegalArgumentException("Player cannot be null");
    }
    players.add(player);
    notifyObservers(new GameEvent(GameEventType.PLAYER_ADDED, player));
  }

  public void playOneRound() {
    if (gameFinished) {
      return;
    }

    for (Player player : players) {
      if (gameFinished) {
        break;
      }
      playTurn(player);
    }
  }

  public void playTurn(Player player) {
    if (gameFinished) {
      return;
    }

    int[] diceValues = dice.rollAllDice();
    int totalRoll = dice.getTotal();
    notifyObservers(new GameEvent(GameEventType.DICE_ROLLED, player, totalRoll, diceValues));

    int oldPosition = player.getCurrentTile().getTileId();

    // Calculate new position
    int newPosition = oldPosition + totalRoll;
    Tile targetTile = board.getTile(newPosition);

    if (targetTile == null) {
      // Handle case where player would move beyond the board
      targetTile = board.getFinalTile();
    }

    player.placeOnTile(targetTile);
    notifyObservers(new GameEvent(GameEventType.PLAYER_MOVED, player, oldPosition, targetTile.getTileId()));

    // Apply tile action if any
    if (targetTile.getTileAction() != null) {
      TileAction action = targetTile.getTileAction();
      action.perform(player);
      notifyObservers(new GameEvent(GameEventType.ACTION_PERFORMED, player, action));
    }

    // Check if player has won
    if (player.hasWon(board.getFinalTileId())) {
      winner = player;
      gameFinished = true;
      notifyObservers(new GameEvent(GameEventType.GAME_OVER, winner));
    }

    // Move to next player
    currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    notifyObservers(new GameEvent(GameEventType.TURN_CHANGED, players.get(currentPlayerIndex)));
  }

  /**
   * Moves a player to a specific tile by ID
   */
  public void movePlayerToTile(Player player, int tileId) {
    Tile tile = board.getTile(tileId);
    if (tile != null) {
      player.placeOnTile(tile);
    }
  }

  /**
   * Advances to the next player
   */
  public void advanceToNextPlayer() {
    currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    notifyObservers(new GameEvent(GameEventType.TURN_CHANGED, getCurrentPlayer()));
  }

  /**
   * Sets the game's winner
   */
  public void setWinner(Player player) {
    this.winner = player;
  }

  /**
   * Sets the game's finished state
   */
  public void setGameFinished(boolean finished) {
    this.gameFinished = finished;
    if (finished && winner != null) {
      notifyObservers(new GameEvent(GameEventType.GAME_OVER, winner));
    }
  }

  public boolean isFinished() {
    return gameFinished;
  }

  public Player getWinner() {
    return winner;
  }

  public Board getBoard() {
    return board;
  }

  public Dice getDice() {
    return dice;
  }

  public List<Player> getPlayers() {
    return players;
  }

  public Player getCurrentPlayer() {
    return players.get(currentPlayerIndex);
  }

  @Override
  public void addObserver(GameObserver observer) {
    observers.add(observer);
  }

  @Override
  public void removeObserver(GameObserver observer) {
    observers.remove(observer);
  }

  @Override
  public void notifyObservers(GameEvent event) {
    for (GameObserver observer : observers) {
      observer.onGameEvent(event);
    }
  }


  public void loadGame(String filename) throws BoardGameException {
    try {
      BoardJsonHandler boardHandler = new BoardJsonHandler();
      this.board = boardHandler.readFromFile(filename);

      // Reset game state
      this.gameFinished = false;
      this.winner = null;
      this.currentPlayerIndex = 0;

      // Place players at start position or restore their positions
      // For simplicity in this implementation, just place them at start
      for (Player player : players) {
        player.placeOnTile(board.getTile(1));
      }

      notifyObservers(new GameEvent(GameEventType.BOARD_CREATED, null));
    } catch (Exception e) {
      throw new BoardGameException("Failed to load game: " + e.getMessage(), e);
    }
  }
}