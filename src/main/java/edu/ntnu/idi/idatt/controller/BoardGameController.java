package edu.ntnu.idi.idatt.controller;

import edu.ntnu.idi.idatt.action.LadderAction;
import edu.ntnu.idi.idatt.event.GameEvent;
import edu.ntnu.idi.idatt.event.GameEventType;
import edu.ntnu.idi.idatt.event.GameObserver;
import edu.ntnu.idi.idatt.exception.BoardGameException;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.model.Player;
import edu.ntnu.idi.idatt.model.Tile;
import edu.ntnu.idi.idatt.view.BoardGameView;
import edu.ntnu.idi.idatt.view.BoardGameViewImpl;

public class BoardGameController implements GameObserver {
  private BoardGame model;
  private BoardGameView view;
  private boolean animationInProgress = false;

  public BoardGameController(BoardGame model, BoardGameView view) {
    this.model = model;
    this.view = view;

    // Register as observer
    model.addObserver(this);

    // Set up view event handlers
    view.setRollDiceHandler(this::handleRollDice);
    view.setNewGameHandler(this::handleNewGame);
    view.setLoadGameHandler(this::handleLoadGame);

    // Set up dice count change handler for integrated settings
    if (view instanceof BoardGameViewImpl) {
      ((BoardGameViewImpl) view).setDiceCountChangeHandler(this::handleDiceCountChange);
    }

    // Initialize view with current game state
    view.renderBoard(model.getBoard());
    view.updatePlayersList(model.getPlayers());

    // Highlight current player only if there are players
    if (!model.getPlayers().isEmpty()) {
      view.highlightCurrentPlayer(model.getCurrentPlayer());
    }
  }

  private void handleRollDice() {
    if (animationInProgress) {
      return; // Prevent actions during animations
    }

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
        model.advanceToNextPlayer();
        view.highlightCurrentPlayer(model.getCurrentPlayer());
        return;
      }

      // First, roll the dice
      int[] diceValues = model.getDice().rollAllDice();
      int total = model.getDice().getTotal();

      // Show dice roll
      view.showDiceRoll(currentPlayer, total, diceValues);

      // Get current position
      int oldPosition = currentPlayer.getCurrentTile().getTileId();

      // Calculate new position
      int newPosition = oldPosition + total;
      int maxTileId = model.getBoard().getFinalTileId();

      // Handle case where player would move beyond the board
      if (newPosition > maxTileId) {
        newPosition = maxTileId;
      }

      // Get the destination tile
      final int destinationTileId = newPosition;

      // Start animation sequence
      animationInProgress = true;

      // 1. Animate player moving to the new position
      view.movePlayerWithAnimation(currentPlayer, oldPosition, destinationTileId, () -> {
        // After move animation completes, update model and check for tile action
        model.movePlayerToTile(currentPlayer, destinationTileId);

        // Get the tile the player landed on
        Tile landedTile = model.getBoard().getTile(destinationTileId);

        // Check if player landed on a tile with an action
        if (landedTile != null && landedTile.getTileAction() != null) {
          // Save the current position before action
          int preActionPosition = destinationTileId;

          // Execute the action in the model (this should update the player's position)
          landedTile.landAction(currentPlayer);

          // Get the new position after action
          int postActionPosition = currentPlayer.getCurrentTile().getTileId();

          // Only animate if positions different (action moved the player)
          if (preActionPosition != postActionPosition) {
            // 2. Animate the action (ladder/slide)
            view.showActionWithAnimation(currentPlayer, landedTile.getTileAction(), postActionPosition, () -> {
              finishTurn(currentPlayer);
            });
          } else {
            finishTurn(currentPlayer);
          }
        } else {
          finishTurn(currentPlayer);
        }
      });

    } catch (Exception e) {
      animationInProgress = false;
      view.showError("Error during turn", e.getMessage());
    }
  }

  // Helper method to finish the turn
  private void finishTurn(Player currentPlayer) {
    // Check if game is over
    if (currentPlayer.hasWon(model.getBoard().getFinalTileId())) {
      model.setWinner(currentPlayer);
      model.setGameFinished(true);
      view.showGameOver(currentPlayer);
    } else {
      // Move to next player
      model.advanceToNextPlayer();
      view.highlightCurrentPlayer(model.getCurrentPlayer());
    }

    // Animation sequence complete
    animationInProgress = false;
  }

  private void handleNewGame() {

    try {
      // Navigate back to character selection for full game setup
      edu.ntnu.idi.idatt.navigation.NavigationManager.getInstance()
        .navigateTo(edu.ntnu.idi.idatt.navigation.NavTo.CHARACTER_SELECTION);
    } catch (Exception e) {
      view.showError("Error starting new game", e.getMessage());
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

  /**
   * Handles dice count changes from the integrated settings panel
   */
  private void handleDiceCountChange(int newDiceCount) {
    try {
      int currentDiceCount = model.getDice().getNumberOfDice();

      if (newDiceCount != currentDiceCount) {
        // Update the model
        model.getDice().setNumberOfDice(newDiceCount);

        // Update the view
        view.updateDiceView(newDiceCount);

        view.showMessage("Settings Updated",
          "Number of dice changed from " + currentDiceCount + " to " + newDiceCount);
      }
    } catch (Exception e) {
      view.showError("Error Updating Settings", e.getMessage());
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
        if (event.getDiceValues() != null) {
          view.showDiceRoll(event.getPlayer(), event.getDiceRoll(), event.getDiceValues());
        } else {
          view.showDiceRoll(event.getPlayer(), event.getDiceRoll());
        }
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