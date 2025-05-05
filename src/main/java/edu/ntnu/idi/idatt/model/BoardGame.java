package edu.ntnu.idi.idatt.model;

import edu.ntnu.idi.idatt.event.BoardGameObserver;
import edu.ntnu.idi.idatt.event.GameEvent;
import edu.ntnu.idi.idatt.event.ObservableGame;
import edu.ntnu.idi.idatt.exception.BoardGameException;
import edu.ntnu.idi.idatt.factory.BoardGameFactory;
import edu.ntnu.idi.idatt.io.BoardJsonHandler;
import edu.ntnu.idi.idatt.io.PlayerCsvHandler;

import java.util.ArrayList;
import java.util.List;

public class BoardGame implements ObservableGame {
  private Board board;
  private Player currentPlayer;
  private final List<Player> players = new ArrayList<>();
  private Dice dice;
  private boolean gameFinished = false;
  private Player winner = null;
  private final int finalTileId = 100;
  private int currentPlayerIndex = 0;

  private final List<BoardGameObserver> observers = new ArrayList<>();

  public void addPlayer(Player player) {
    players.add(player);
  }

  public void createBoard() {
    this.board = BoardGameFactory.createDefaultBoard();

    // Place players on starting position if any exist
    if (!players.isEmpty()) {
      Tile startTile = board.getTile(1);
      for (Player player : players) {
        player.placeOnTile(startTile);
      }
    }
  }

  // In BoardGame.java
  public void createDice(int numberOfDice) {
    this.dice = new Dice(numberOfDice);
  }

  public void reset() {
    gameFinished = false;
    winner = null;
    currentPlayerIndex = 0;

    // Reset player positions
    if (board != null) {
      Tile startTile = board.getTile(1);
      for (Player player : players) {
        player.placeOnTile(startTile);
      }
    }

    // Notify that game has been reset
    notifyObservers(new GameEvent(GameEvent.EventType.GAME_RESET, null, null, null));
  }

  public void playOneRound() {
    if (gameFinished || players.isEmpty()) {
      return;
    }

    // Set current player
    currentPlayer = players.get(currentPlayerIndex);

    // Roll the dice
    int roll = dice.Roll();

    // Notify that dice have been rolled
    notifyObservers(new GameEvent(GameEvent.EventType.DICE_ROLLED, currentPlayer, null, null, roll));

    try {
      // Store current tile before moving
      Tile oldTile = currentPlayer.getCurrentTile();

      // Move player
      currentPlayer.move(roll);

      // Check if player won
      if (currentPlayer.hasWon(finalTileId)) {
        gameFinished = true;
        winner = currentPlayer;

        // Notify of game over
        notifyObservers(new GameEvent(GameEvent.EventType.GAME_OVER, currentPlayer, oldTile, currentPlayer.getCurrentTile()));
      }

      // Move to next player
      currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

    } catch (Exception e) {
      System.err.println("Error during player move: " + e.getMessage());

      // Move to next player even if there was an error
      currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
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

  public Player getCurrentPlayer() {
    return currentPlayer;
  }

  public List<Player> getPlayers() {
    return players;
  }

  public Dice getDice() {
    return dice;
  }

  public int getFinalTileId() {
    return finalTileId;
  }

  @Override
  public void addObserver(BoardGameObserver observer) {
    observers.add(observer);
  }

  @Override
  public void removeObserver(BoardGameObserver observer) {
    observers.remove(observer);
  }

  @Override
  public void notifyObservers(GameEvent event) {
    List<BoardGameObserver> observersCopy = new ArrayList<>(observers);
    for (BoardGameObserver observer : observersCopy) {
      observer.update(event);
    }
  }

  public void saveBoardToFile(String filename) throws BoardGameException {
    BoardJsonHandler handler = new BoardJsonHandler();
    handler.writeToFile(this.board, filename);
  }

  public void loadBoardFromFile(String filename) throws BoardGameException {
    BoardJsonHandler handler = new BoardJsonHandler();
    this.board = handler.readFromFile(filename);

    // Reset player positions after loading new board
    reset();
  }

  public void loadPlayersFromFile(String filename) throws BoardGameException {
    PlayerCsvHandler handler = new PlayerCsvHandler(this);
    List<Player> loadedPlayers = handler.readFromFile(filename);

    // Clear existing players and add loaded ones
    players.clear();
    for (Player player : loadedPlayers) {
      addPlayer(player);
    }

    // Reset player positions
    reset();
  }

  public void savePlayersToFile(String filename) throws BoardGameException {
    PlayerCsvHandler handler = new PlayerCsvHandler(this);
    handler.writeToFile(players, filename);
  }
}