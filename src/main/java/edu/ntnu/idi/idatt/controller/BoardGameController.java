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
    view.setLoadGameHandler(this::handleLoadGame);

    // Initialize view with current game state
    view.renderBoard(model.getBoard());
    view.updatePlayersList(model.getPlayers());

    // Highlight current player
    if (!model.getPlayers().isEmpty()) {
      view.highlightCurrentPlayer(model.getCurrentPlayer());
    }
  }

  private void handleRollDice() {
    try {
      if (model.isFinished()) {
        view.showMessage("Game Over", "The game is already finished. Start a new game to play again.");
        return;
      }

      Player currentPlayer = model.getCurrentPlayer();

      // Check if player should skip turn
      if (currentPlayer.getSkipsNextTurn()) {
        currentPlayer.setSkipsNextTurn(false);
        view.showMessage("Skip Turn",
          currentPlayer.getName() + " skips this turn.");

        // Move to next player
        int nextPlayerIndex = (model.getPlayers().indexOf(currentPlayer) + 1) % model.getPlayers().size();
        Player nextPlayer = model.getPlayers().get(nextPlayerIndex);
        view.highlightCurrentPlayer(nextPlayer);
        return;
      }

      // Play turn - model will notify us via observer pattern
      model.playTurn(currentPlayer);

    } catch (Exception e) {
      view.showError("Error during turn", e.getMessage());
    }
  }

  private void handleNewGame() {
    try {
      // Get existing players
      List<Player> existingPlayers = model.getPlayers();

      // Reset the model
      model.createBoard();
      model.createDice(2);

      // Clear players
      model.getPlayers().clear();

      // Add players back with reset positions
      for (Player oldPlayer : existingPlayers) {
        Player newPlayer = new Player(oldPlayer.getName(), model, oldPlayer.getTokenType());
        model.addPlayer(newPlayer);
        newPlayer.placeOnTile(model.getBoard().getTile(1));
      }

      // Update view
      view.renderBoard(model.getBoard());
      view.updatePlayersList(model.getPlayers());
      view.highlightCurrentPlayer(model.getCurrentPlayer());

      // Re-enable roll button if it was disabled
      view.showMessage("New Game", "A new game has been started.");

    } catch (Exception e) {
      view.showError("Error creating new game", e.getMessage());
    }
  }


  private void handleLoadGame() {
    try {
      String filename = view.showLoadDialog();
      if (filename != null && !filename.isEmpty()) {
        model.loadGame(filename);
        view.renderBoard(model.getBoard());
        view.updatePlayersList(model.getPlayers());
        view.highlightCurrentPlayer(model.getCurrentPlayer());
        view.showMessage("Game Loaded", "Game successfully loaded from " + filename);
      }
    } catch (BoardGameException e) {
      view.showError("Error Loading Game", e.getMessage());
    }
  }

  @Override
  public void onGameEvent(GameEvent event) {
    GameEventType type = event.getType();

    switch (type) {
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

      // Add cases for LADDER_CLIMBED and CHUTE_SLID if added to GameEventType
      default:
        // No specific handling for other event types
        break;
    }
  }
}