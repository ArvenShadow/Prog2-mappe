package edu.ntnu.idi.idatt.io;

import edu.ntnu.idi.idatt.exception.BoardGameException;

public interface FileHandler<T> {

    T readFromFile(String fileName) throws BoardGameException;

    void writeToFile(T data, String filename) throws BoardGameException;
}
