package edu.ntnu.idi.idatt.io;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import edu.ntnu.idi.idatt.action.LadderAction;
import edu.ntnu.idi.idatt.action.SkipTurnAction;
import edu.ntnu.idi.idatt.action.TileAction;
import edu.ntnu.idi.idatt.exception.BoardGameException;
import edu.ntnu.idi.idatt.model.Board;
import edu.ntnu.idi.idatt.model.Tile;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BoardJsonHandler implements FileHandler<Board> {

  @Override
  public Board readFromFile(String fileName) throws BoardGameException {
    try (JsonReader reader = new JsonReader(new FileReader(fileName))) {
      JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();


      int rows = json.get("rows").getAsInt();
      int cols = json.get("columns").getAsInt();


      Board board = new Board(rows, cols);


      JsonArray tilesArray = json.getAsJsonArray("tiles");
      for (JsonElement element : tilesArray) {
        JsonObject tileJson = element.getAsJsonObject();
        int id = tileJson.get("id").getAsInt();
        int row = tileJson.get("row").getAsInt();
        int col = tileJson.get("col").getAsInt();


        Tile tile = new Tile(id, row, col);


        if (tileJson.has("action")) {
          JsonObject actionJson = tileJson.getAsJsonObject("action");
          String actionType = actionJson.get("type").getAsString();

          if ("LadderAction".equals(actionType) && actionJson.has("destinationTileId")) {
            int destinationId = actionJson.get("destinationTileId").getAsInt();
            tile.setTileAction(new LadderAction(destinationId));
          } else if ("SkipTurnAction".equals(actionType)) {
            tile.setTileAction(new SkipTurnAction());
          }
        }

        board.addTile(tile);
      }
      return board;

    } catch (IOException | IllegalStateException e) {
      throw new BoardGameException("Failed to read board from file: " + e.getMessage(), e);
    }
  }

  @Override
  public void writeToFile(Board board, String filename) throws BoardGameException {
    try {
      JsonObject boardJson = new JsonObject();


      boardJson.addProperty("rows", board.getNumRows());
      boardJson.addProperty("columns", board.getNumCols());


      JsonArray tilesArray = new JsonArray();
      for (int tileId = 1; tileId <= board.getNumRows() * board.getNumCols(); tileId++) {
        Tile tile = board.getTile(tileId);

        if (tile != null) {
          JsonObject tileJson = new JsonObject();

          tileJson.addProperty("id", tile.getTileId());
          tileJson.addProperty("row", tile.getRow());
          tileJson.addProperty("col", tile.getCol());


          TileAction action = tile.getTileAction();
          if (action != null) {
            JsonObject actionJson = new JsonObject();

            if (action instanceof LadderAction) {
              actionJson.addProperty("type", "LadderAction");
              actionJson.addProperty("destinationTileId", ((LadderAction) action).getDestinationTileId());
            } else if (action instanceof SkipTurnAction) {
              actionJson.addProperty("type", "SkipTurnAction");
            }

            tileJson.add("action", actionJson);
          }

          tilesArray.add(tileJson);
        }
      }

      boardJson.add("tiles", tilesArray);


      try (FileWriter writer = new FileWriter(filename)) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(boardJson, writer);
      }
    } catch (IOException e) {
      throw new BoardGameException("Failed to write board to file: " + e.getMessage(), e);
    }
  }
}