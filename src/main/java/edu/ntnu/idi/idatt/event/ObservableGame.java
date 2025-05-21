package edu.ntnu.idi.idatt.event;

public interface ObservableGame {
  void addObserver(GameObserver observer);
  void removeObserver(GameObserver observer);
  void notifyObservers(GameEvent event);
}
