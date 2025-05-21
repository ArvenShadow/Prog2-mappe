package edu.ntnu.idi.idatt.io;

import edu.ntnu.idi.idatt.exception.BoardGameException;
import edu.ntnu.idi.idatt.exception.InvalidPlayerTokenException;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.model.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerCsvHandler implements FileHandler<List<Player>> {

  private BoardGame game;
  private static final Set<String> ALLOWED_TOKENS = new HashSet<>();

  static {
    ALLOWED_TOKENS.add("TopHat");
    ALLOWED_TOKENS.add("RaceCar");
    ALLOWED_TOKENS.add("Shoe");
    ALLOWED_TOKENS.add("Thimble");
    ALLOWED_TOKENS.add("Cat");
  }

  public PlayerCsvHandler(BoardGame game) {
    this.game = game;
  }

  @Override
  public List<Player> readFromFile(String fileName) throws BoardGameException {
    List<Player> players = new ArrayList<>();
    boolean isHeader = true;

    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      String line;
      while ((line = reader.readLine()) != null) {
        // Skip header row
        if (isHeader) {
          isHeader = false;
          continue;
        }

        String[] parts = line.split(",");
        if (parts.length == 2) {
          String name = parts[0].trim();
          String token = parts[1].trim();

          // Validate token
          if (!ALLOWED_TOKENS.contains(token)) {
            throw new InvalidPlayerTokenException(
              "Invalid token type: " + token + ". Allowed tokens are: " +
                String.join(", ", ALLOWED_TOKENS));
          }

          players.add(new Player(name, game, token));
        }
      }
      return players;
    } catch (IOException e) {
      throw new BoardGameException("Could not read file " + fileName + ": " + e.getMessage(), e);
    }
  }

  @Override
  public void writeToFile(List<Player> players, String filename) throws BoardGameException {
    try (FileWriter writer = new FileWriter(filename)) {
      // Write header
      writer.write("Player,PlayerToken\n");

      // Write each player
      for (Player player : players) {
        String token = player.getTokenType();

        // Validate token before writing
        if (!ALLOWED_TOKENS.contains(token)) {
          throw new InvalidPlayerTokenException(
            "Invalid token type: " + token + ". Allowed tokens are: " +
              String.join(", ", ALLOWED_TOKENS));
        }

        writer.write(player.getName() + "," + token + "\n");
      }
    } catch (IOException e) {
      throw new BoardGameException("Error writing players to file: " + filename, e);
    }
  }

  public static boolean isValidToken(String token) {
    return ALLOWED_TOKENS.contains(token);
  }
}