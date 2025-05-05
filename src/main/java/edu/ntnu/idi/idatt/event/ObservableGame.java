package edu.ntnu.idi.idatt.event;

public interface ObservableGame {
  void addObserver(BoardGameObserver observer);
  void removeObserver(BoardGameObserver observer);
  void notifyObservers(GameEvent event);
}
