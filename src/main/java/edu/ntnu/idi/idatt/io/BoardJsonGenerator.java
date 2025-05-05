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

    // Generate all 100 tiles in snake pattern
    for (int id = 1; id <= 100; id++) {
      int row = 9 - ((id - 1) / 10);
      int col = (row % 2 == 1) ? ((id - 1) % 10) : (9 - ((id - 1) % 10));

      JsonObject tileJson = new JsonObject();
      tileJson.addProperty("id", id);
      tileJson.addProperty("row", row);
      tileJson.addProperty("col", col);

      // Add next tile for all except the last one
      if (id < 100) {
        tileJson.addProperty("nextTile", id + 1);
      }

      tilesArray.add(tileJson);
    }

    // Add all ladders and chutes (both use LadderAction)
    // Ladders (going up)
    addAction(tilesArray, 4, 14);
    addAction(tilesArray, 9, 31);
    addAction(tilesArray, 20, 38);
    addAction(tilesArray, 28, 84);
    addAction(tilesArray, 40, 59);
    addAction(tilesArray, 51, 67);
    addAction(tilesArray, 63, 81);

    // Chutes (going down)
    addAction(tilesArray, 17, 7);
    addAction(tilesArray, 54, 34);
    addAction(tilesArray, 62, 19);
    addAction(tilesArray, 64, 60);
    addAction(tilesArray, 87, 24);
    addAction(tilesArray, 93, 73);
    addAction(tilesArray, 95, 75);
    addAction(tilesArray, 99, 78);

    boardJson.add("tiles", tilesArray);

    // Write to file with pretty printing
    try (FileWriter writer = new FileWriter(outputFilePath)) {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(boardJson, writer);
    }
  }

  private static void addAction(JsonArray tilesArray, int tileId, int destinationId) {
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

  public static void main(String[] args) {
    try {
      generateStandardBoard("src/main/resources/standard_board.json");
      System.out.println("Board file generated successfully!");
    } catch (IOException e) {
      System.err.println("Error generating board: " + e.getMessage());
    }
  }
}