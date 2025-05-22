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
    if (game == null) {
      throw new IllegalArgumentException("BoardGame cannot be null");
    }
    this.game = game;
  }

  @Override
  public List<Player> readFromFile(String fileName) throws BoardGameException {
    List<Player> players = new ArrayList<>();
    Set<String> usedTokens = new HashSet<>();

    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      String line;
      boolean isHeader = true;

      while ((line = reader.readLine()) != null) {
        if (isHeader) {
          isHeader = false;
          continue;
        }

        line = line.trim();
        if (line.isEmpty()) continue;

        String[] parts = line.split(",");
        if (parts.length != 2) {
          throw new BoardGameException("Invalid CSV format. Expected: Name,Token");
        }

        String name = parts[0].trim();
        String token = parts[1].trim();

        // Validate token
        if (!ALLOWED_TOKENS.contains(token)) {
          throw new InvalidPlayerTokenException("Invalid token: " + token +
            ". Allowed: " + String.join(", ", ALLOWED_TOKENS));
        }

        if (usedTokens.contains(token)) {
          throw new BoardGameException("Duplicate token: " + token);
        }

        players.add(new Player(name, game, token));
        usedTokens.add(token);
      }

      if (players.isEmpty()) {
        throw new BoardGameException("No players found in file");
      }

      return players;

    } catch (IOException e) {
      throw new BoardGameException("Cannot read player file: " + fileName, e);
    }
  }

  @Override
  public void writeToFile(List<Player> players, String filename) throws BoardGameException {
    try (FileWriter writer = new FileWriter(filename)) {
      writer.write("PlayerName,PlayerToken\n");

      for (Player player : players) {
        String token = player.getTokenType();

        if (!ALLOWED_TOKENS.contains(token)) {
          throw new InvalidPlayerTokenException("Invalid token: " + token);
        }

        writer.write(player.getName() + "," + token + "\n");
      }

    } catch (IOException e) {
      throw new BoardGameException("Cannot write player file: " + filename, e);
    }
  }

  public static boolean isValidToken(String token) {
    return ALLOWED_TOKENS.contains(token);
  }
}