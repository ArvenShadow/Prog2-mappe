import edu.ntnu.idi.idatt.event.GameEvent;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.model.Player;
import edu.ntnu.idi.idatt.model.Tile;

public class LadderAction implements TileAction {
  private final int destinationTileId;

  public LadderAction(int destinationTileId) {
    this.destinationTileId = destinationTileId;
  }

  @Override
  public void perform(Player player) {
    // Get current tile ID to determine if going up or down
    int startTileId = player.getCurrentTile().getTileId();
    boolean isLadderUp = destinationTileId > startTileId;

    // Print clear log message
    System.out.println(player.getName() +
      (isLadderUp ? " climbing ladder" : " sliding down chute") +
      " from " + startTileId + " to " + destinationTileId);

    // Get destination tile
    Tile destinationTile = player.getGame().getBoard().getTile(destinationTileId);
    if (destinationTile != null) {
      // Store current tile for event notification
      Tile oldTile = player.getCurrentTile();

      // Move player to new tile
      player.placeOnTile(destinationTile);

      // Create appropriate event
      GameEvent.EventType eventType = isLadderUp ?
        GameEvent.EventType.LADDER_CLIMBED :
        GameEvent.EventType.CHUTE_SLID;

      // Notify observers
      BoardGame game = player.getGame();
      game.notifyObservers(new GameEvent(eventType, player, oldTile, destinationTile));
    } else {
      throw new IllegalStateException("Destination tile " + destinationTileId + " not found");
    }
  }

  public int getDestinationTileId() {
    return destinationTileId;
  }
}