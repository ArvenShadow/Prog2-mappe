package edu.ntnu.idi.idatt.action;

import edu.ntnu.idi.idatt.event.GameEvent;
import edu.ntnu.idi.idatt.event.GameEventType;
import edu.ntnu.idi.idatt.event.ObservableGame;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.model.Player;
import edu.ntnu.idi.idatt.model.Tile;

public class LadderAction implements TileAction {

  private final int destinationTileId;
  private boolean isLadder; //True for up, false for down

  public LadderAction(int destinationTileId){
    this.destinationTileId = destinationTileId;
    this.isLadder = true;
  }

  @Override
  public void perform(Player player) {
    int startTileId = player.getCurrentTile().getTileId();
    isLadder = destinationTileId > startTileId;

    String actionType = isLadder ? "climbs ladder" : "slides down chute";
    System.out.println(player.getName() + " " + actionType + " to tile " + destinationTileId);

    Tile destinationTile = player.getGame().getBoard().getTile(destinationTileId);
    if (destinationTile != null) {
      Tile oldTile = player.getCurrentTile();
      player.placeOnTile(destinationTile);

      // Create appropriate event based on ladder direction
      GameEventType eventType = isLadder ?
        GameEventType.LADDER_CLIMBED :
        GameEventType.CHUTE_SLID;

      // Notify observers if game supports it
      BoardGame game = player.getGame();
      if (game instanceof ObservableGame) {
        ((ObservableGame) game).notifyObservers(
          new GameEvent(eventType, player, oldTile.getTileId(), destinationTile.getTileId())
        );
      }
    } else {
      throw new IllegalStateException("Destination tile " + destinationTileId + " not found");
    }
  }

  public int getDestinationTileId() {
    return destinationTileId;
  }

  public boolean isLadder() {
    return isLadder;
  }

}



