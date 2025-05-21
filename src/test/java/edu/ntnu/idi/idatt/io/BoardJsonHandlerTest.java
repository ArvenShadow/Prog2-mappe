package edu.ntnu.idi.idatt.io;

import edu.ntnu.idi.idatt.action.LadderAction;
import edu.ntnu.idi.idatt.exception.BoardGameException;
import edu.ntnu.idi.idatt.model.Board;
import edu.ntnu.idi.idatt.model.Tile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardJsonHandlerTest {

    @Test
    public void testReadFromFile() {
        BoardJsonHandler handler = new BoardJsonHandler();

        try {

            Board board = handler.readFromFile("src/main/resources/standard_board.json");


            assertNotNull(board, "Board should not be null");
            assertEquals(10, board.getNumRows(), "Number of rows should be 10");
            assertEquals(10, board.getNumCols(), "Number of columns should be 10");


            Tile tile4 = board.getTile(4);
            assertNotNull(tile4, "Tile 4 should exist");
            assertNotNull(tile4.getTileAction(), "Tile 4 should have an action");
            assertTrue(tile4.getTileAction() instanceof LadderAction, "Tile 4 action should be a LadderAction");
            assertEquals(14, ((LadderAction) tile4.getTileAction()).getDestinationTileId(), "Tile 4 action destination should be Tile 14");

            Tile tile17 = board.getTile(17);
            assertNotNull(tile17, "Tile 17 should exist");
            assertNotNull(tile17.getTileAction(), "Tile 17 should have an action");
            assertTrue(tile17.getTileAction() instanceof LadderAction, "Tile 17 action should be a LadderAction");
            assertEquals(7, ((LadderAction) tile17.getTileAction()).getDestinationTileId(), "Tile 17 action destination should be Tile 7");

        } catch (BoardGameException e) {
            fail("An exception occurred while reading the Board: " + e.getMessage());
        }
    }
}