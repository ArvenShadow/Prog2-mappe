package edu.ntnu.idi.idatt.io;

import com.google.gson.*;

import java.io.FileWriter;
import java.io.IOException;

public class BoardJsonGenerator {
  public static void generateStandardBoard(String outputFilePath) throws IOException {
    JsonObject boardJson = new JsonObject();
    boardJson.addProperty("rows", 10);
    boardJson.addProperty("columns", 10);

    JsonArray tilesArray = new JsonArray();

    // Generate all tiles
    for (int id = 1; id <= 100; id++) {
      int row = 9 - ((id - 1) / 10);
      int col = (row % 2 == 1) ? ((id - 1) % 10) : (9 - ((id - 1) % 10));

      JsonObject tileJson = new JsonObject();
      tileJson.addProperty("id", id);
      tileJson.addProperty("row", row);
      tileJson.addProperty("col", col);

      if (id < 100) {
        tileJson.addProperty("nextTile", id + 1);
      }

      tilesArray.add(tileJson);
    }

    // Add ladder actions (going up)
    addLadderAction(tilesArray, 4, 14);
    addLadderAction(tilesArray, 9, 31);
    addLadderAction(tilesArray, 20, 38);
    addLadderAction(tilesArray, 28, 84);
    addLadderAction(tilesArray, 40, 59);
    addLadderAction(tilesArray, 51, 67);
    addLadderAction(tilesArray, 63, 81);

    // Add chute actions (going down)
    addLadderAction(tilesArray, 17, 7);
    addLadderAction(tilesArray, 54, 34);
    addLadderAction(tilesArray, 62, 19);
    addLadderAction(tilesArray, 64, 60);
    addLadderAction(tilesArray, 87, 24);
    addLadderAction(tilesArray, 93, 73);
    addLadderAction(tilesArray, 95, 75);
    addLadderAction(tilesArray, 99, 78);

    // Add skip turn actions
    addSkipTurnAction(tilesArray, 8);
    addSkipTurnAction(tilesArray, 15);
    addSkipTurnAction(tilesArray, 33);
    addSkipTurnAction(tilesArray, 47);
    addSkipTurnAction(tilesArray, 58);
    addSkipTurnAction(tilesArray, 77);
    addSkipTurnAction(tilesArray, 89);

    boardJson.add("tiles", tilesArray);

    try (FileWriter writer = new FileWriter(outputFilePath)) {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(boardJson, writer);
    }
  }

  /**
   * Adds a ladder action (up or down) to the specified tile
   */
  private static void addLadderAction(JsonArray tilesArray, int tileId, int destinationId) {
    for (JsonElement element : tilesArray) {
      JsonObject tile = element.getAsJsonObject();
      if (tile.get("id").getAsInt() == tileId) {
        JsonObject action = new JsonObject();
        action.addProperty("type", "LadderAction");
        action.addProperty("destinationTileId", destinationId);
        tile.add("action", action);
        break;
      }
    }
  }

  /**
   * Adds a skip turn action to the specified tile
   */
  private static void addSkipTurnAction(JsonArray tilesArray, int tileId) {
    for (JsonElement element : tilesArray) {
      JsonObject tile = element.getAsJsonObject();
      if (tile.get("id").getAsInt() == tileId) {
        JsonObject action = new JsonObject();
        action.addProperty("type", "SkipTurnAction");
        tile.add("action", action);
        break;
      }
    }
  }

  public static void main(String[] args) {
    try {
      generateStandardBoard("src/main/resources/standard_board.json");
      System.out.println("Board file generated successfully!");
    } catch (IOException e) {
      System.err.println("Error generating board: " + e.getMessage());
    }
  }
}