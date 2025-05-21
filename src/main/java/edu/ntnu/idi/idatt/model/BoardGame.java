package edu.ntnu.idi.idatt.model;

import edu.ntnu.idi.idatt.action.LadderAction;
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

  private List<BoardGameObserver> observers = new ArrayList<>();

  public void addPlayer(Player player) {
    players.add(player);
  }

  public void createBoard(int rows, int cols) {
    this.board = new Board(rows, cols);
    this.board.setupGameBoard();

    setupLaddersAndChutes();
  }

  private void setupLaddersAndChutes() {
    Tile ladder = board.getTile(4);
    if (ladder != null) {
      ladder.setTileAction(new LadderAction(14));
    }
    Tile chute = board.getTile(17);
    if (chute != null) {
      chute.setTileAction(new LadderAction(7));
    }
  }

  public void createDice(int numberOfDice) {
    this.dice = new Dice(numberOfDice);
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

  public void addObserver(BoardGameObserver observer) {
    observers.add(observer);
  }

  public void removeObserver(BoardGameObserver observer) {
    observers.remove(observer);
  }

  public void notifyObservers(GameEvent event) {
    for (BoardGameObserver observer : observers) {
      observer.update(event);
    }
  }

  public void createBoard() {
    this.board = BoardGameFactory.createBoard();
  }



  public void saveBoardToFile(String filename) throws BoardGameException {
    BoardJsonHandler handler = new BoardJsonHandler();
    handler.writeToFile(this.board, filename);
  }

  public void loadBoardFromFile(String filename) throws BoardGameException {
    BoardJsonHandler handler = new BoardJsonHandler();
    this.board = handler.readFromFile(filename);
  }

  public void loadPlayersFromFile(String filename) throws BoardGameException {
    PlayerCsvHandler handler = new PlayerCsvHandler(this);
    List<Player> loadedPlayers = handler.readFromFile(filename);

    // Clear existing players and add loaded ones
    players.clear();
    for (Player player : loadedPlayers) {
      addPlayer(player);
    }
  }

  public void savePlayersToFile(String filename) throws BoardGameException {
    PlayerCsvHandler handler = new PlayerCsvHandler(this);
    handler.writeToFile(players, filename);
  }
}
