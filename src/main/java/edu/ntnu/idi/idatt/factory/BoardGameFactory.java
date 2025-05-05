package edu.ntnu.idi.idatt.factory;

import edu.ntnu.idi.idatt.action.LadderAction;
import edu.ntnu.idi.idatt.model.Board;
import edu.ntnu.idi.idatt.model.Tile;

public class BoardGameFactory {
  public static Board createDefaultBoard() {
    Board board = new Board(10, 10);
    board.setupGameBoard();

    //Ladders
    addConnection(board, 4, 14);
    addConnection(board, 9, 31);
    addConnection(board, 20, 38);
    addConnection(board, 28, 84);
    addConnection(board, 40, 59);
    addConnection(board, 51, 67);
    addConnection(board, 63, 81);

    //Chutes
    addConnection(board, 17, 7);
    addConnection(board, 54, 34);
    addConnection(board, 62, 19);
    addConnection(board, 64, 60);
    addConnection(board, 87, 24);
    addConnection(board, 93, 73);
    addConnection(board, 95, 75);
    addConnection(board, 99, 78);

    return board;
  }

  private static void addConnection(Board board, int fromTileId, int toTileId) {
    Tile tile = board.getTile(fromTileId);
    if (tile != null) {
      tile.setTileAction(new LadderAction(toTileId));
    }
  }
}
