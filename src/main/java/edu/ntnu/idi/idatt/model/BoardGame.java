package edu.ntnu.idi.idatt.model;

import edu.ntnu.idi.idatt.event.GameEvent;
import edu.ntnu.idi.idatt.event.GameEventType;
import edu.ntnu.idi.idatt.event.GameObserver;
import edu.ntnu.idi.idatt.exception.BoardGameException;
import edu.ntnu.idi.idatt.action.TileAction;

import java.util.ArrayList;
import java.util.List;

public class BoardGame {
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

  public void createBoard(int rows, int cols) {
    this.board = new Board(rows, cols);
    this.board.setupGameBoard();
    notifyObservers(new GameEvent(GameEventType.BOARD_CREATED, null));
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

    int roll = dice.Roll();
    notifyObservers(new GameEvent(GameEventType.DICE_ROLLED, player, roll));

    int oldPosition = player.getCurrentTile().getTileId();

    // Calculate new position
    int newPosition = oldPosition + roll;
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

  // Observer pattern methods
  public void addObserver(GameObserver observer) {
    observers.add(observer);
  }

  public void removeObserver(GameObserver observer) {
    observers.remove(observer);
  }

  private void notifyObservers(GameEvent event) {
    for (GameObserver observer : observers) {
      observer.onGameEvent(event);
    }
  }

  // File handling methods
  public void saveGame(String filename) throws BoardGameException {
    // Implementation will be added later
  }

  public void loadGame(String filename) throws BoardGameException {
    // Implementation will be added later
  }
}