package edu.ntnu.idi.idatt.view;

import edu.ntnu.idi.idatt.action.TileAction;
import edu.ntnu.idi.idatt.model.Board;
import edu.ntnu.idi.idatt.model.Player;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.List;

public interface BoardGameView {
  void renderBoard(Board board);
  void updatePlayersList(List<Player> players);
  void movePlayer(Player player, int oldPosition, int newPosition);
  void showDiceRoll(Player player, int roll);
  void showAction(Player player, TileAction action);
  void highlightCurrentPlayer(Player player);
  void showGameOver(Player winner);

  void showError(String title, String message);
  void showMessage(String title, String message);

  String showLoadDialog();

  void setRollDiceHandler(Runnable handler);
  void setNewGameHandler(Runnable handler);
  void setLoadGameHandler(Runnable handler);
}