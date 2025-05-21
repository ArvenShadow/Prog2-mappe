package edu.ntnu.idi.idatt.controller;

import edu.ntnu.idi.idatt.event.GameEvent;
import edu.ntnu.idi.idatt.event.GameEventType;
import edu.ntnu.idi.idatt.event.GameObserver;
import edu.ntnu.idi.idatt.exception.BoardGameException;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.model.Player;
import edu.ntnu.idi.idatt.view.BoardGameView;

import java.util.List;

public class BoardGameController implements GameObserver {
  private BoardGame model;
  private BoardGameView view;

  public BoardGameController(BoardGame model, BoardGameView view) {
    this.model = model;
    this.view = view;

    // Register as observer
    model.addObserver(this);

    // Set up view event handlers
    view.setRollDiceHandler(this::handleRollDice);
    view.setNewGameHandler(this::handleNewGame);
    view.setSaveGameHandler(this::handleSaveGame);
    view.setLoadGameHandler(this::handleLoadGame);
  }

  public void initGame() {
    // Initialize a new game
    model.createBoard(10, 10); // Standard 10x10 board
    model.createDice(2); // Two dice

    // Update the view with initial state
    view.renderBoard(model.getBoard());
  }

  public void addPlayer(String name, String token) {
    try {
      Player player = new Player(name, model);
      player.setTokenType(token);
      model.addPlayer(player);

      // Place player at start position
      player.placeOnTile(model.getBoard().getTile(1));

      view.updatePlayersList(model.getPlayers());
    } catch (IllegalArgumentException e) {
      view.showError("Error adding player", e.getMessage());
    }
  }

  public void handleRollDice() {
    try {
      Player currentPlayer = model.getCurrentPlayer();
      model.playTurn(currentPlayer);

      // View is updated via observer notifications
    } catch (Exception e) {
      view.showError("Error during turn", e.getMessage());
    }
  }

  public void handleNewGame() {
    try {
      // Clear existing game
      List<Player> players = model.getPlayers();

      // Reinitialize
      initGame();

      // Re-add players
      for (Player p : players) {
        addPlayer(p.getName(), p.getTokenType());
      }
    } catch (Exception e) {
      view.showError("Error creating new game", e.getMessage());
    }
  }

  public void handleSaveGame() {
    try {
      String filename = view.showSaveDialog();
      if (filename != null && !filename.isEmpty()) {
        model.saveGame(filename);
        view.showMessage("Game saved", "Game successfully saved to " + filename);
      }
    } catch (BoardGameException e) {
      view.showError("Error saving game", e.getMessage());
    }
  }

  public void handleLoadGame() {
    try {
      String filename = view.showLoadDialog();
      if (filename != null && !filename.isEmpty()) {
        model.loadGame(filename);
        view.renderBoard(model.getBoard());
        view.updatePlayersList(model.getPlayers());
        view.showMessage("Game loaded", "Game successfully loaded from " + filename);
      }
    } catch (BoardGameException e) {
      view.showError("Error loading game", e.getMessage());
    }
  }

  @Override
  public void onGameEvent(GameEvent event) {
    // Handle different types of game events
    switch (event.getType()) {
      case BOARD_CREATED:
        view.renderBoard(model.getBoard());
        break;

      case PLAYER_ADDED:
        view.updatePlayersList(model.getPlayers());
        break;

      case DICE_ROLLED:
        view.showDiceRoll(event.getPlayer(), event.getDiceRoll());
        break;

      case PLAYER_MOVED:
        view.movePlayer(event.getPlayer(), event.getOldPosition(), event.getNewPosition());
        break;

      case ACTION_PERFORMED:
        view.showAction(event.getPlayer(), event.getAction());
        break;

      case TURN_CHANGED:
        view.highlightCurrentPlayer(event.getPlayer());
        break;

      case GAME_OVER:
        view.showGameOver(event.getPlayer());
        break;
    }
  }
}