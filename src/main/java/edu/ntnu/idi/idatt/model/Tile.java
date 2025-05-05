package edu.ntnu.idi.idatt.model;


import edu.ntnu.idi.idatt.action.TileAction;

public class Tile {

  private int tileId;
  private Tile nextTile;
  private TileAction tileAction;

  private int row;
  private int col;

  public Tile(int tileId, int row, int col){
    this.row = row;
    this.col = col;
    this.tileId = tileId;
    this.nextTile = null;
    this.tileAction = null;
  }

  public int getTileId(){
    return tileId;
  }

  public int getRow(){
    return row;
  }
  public int getCol(){
    return col;
  }


  public Tile getNextTile(){
    return nextTile;
  }

  public void setNextTile(Tile nextTile){
    this.nextTile = nextTile;
  }

  public void setTileAction(TileAction tileAction){
    this.tileAction = tileAction;
  }

  public void landAction(Player player) {
    if (tileAction != null) {
      try {
        tileAction.perform(player);
      } catch (Exception e) {
        System.out.println("Error performing tile action for tile " + tileId + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  public TileAction getTileAction(){
    return tileAction;
  }

}
