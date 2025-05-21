package edu.ntnu.idi.idatt.factory;

import edu.ntnu.idi.idatt.action.LadderAction;
import edu.ntnu.idi.idatt.exception.BoardGameException;
import edu.ntnu.idi.idatt.io.BoardJsonHandler;
import edu.ntnu.idi.idatt.model.Board;
import edu.ntnu.idi.idatt.model.Tile;

public class BoardGameFactory {
  public static Board createBoard() {
    BoardJsonHandler handler = new BoardJsonHandler();
    try{
      return handler.readFromFile("src/main/resources/standard_board.json");
    } catch (BoardGameException e){
      throw new RuntimeException("Could not create board from JSon file: " + e.getMessage());

    }

  }
}
