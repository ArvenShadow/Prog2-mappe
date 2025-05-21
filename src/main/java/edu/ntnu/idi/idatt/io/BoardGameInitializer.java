package edu.ntnu.idi.idatt.io;

import edu.ntnu.idi.idatt.exception.BoardGameException;
import edu.ntnu.idi.idatt.model.Board;

import java.io.File;
import java.util.Scanner;

public class BoardGameInitializer {
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        File[] boardFiles = BoardFileSelector.getBoardFiles("src/main/resources/");
        System.out.print("\nAvailable Boards: ");
        for(int i = 0; i < boardFiles.length; i++) {
            System.out.println((i + 1) + ": "
                    + boardFiles[i].getName().replace(".json", ""));
        }
        System.out.println("\nEnter board number to load: ");
        int boardChoice = scan.nextInt() - 1;

        if(boardChoice >= 0 && boardChoice < boardFiles.length){
            String filename = boardFiles[boardChoice].getAbsolutePath();
            BoardJsonHandler handler = new BoardJsonHandler();
            try {
                Board board = handler.readFromFile(filename);
                System.out.println("Board loaded successfully! " + filename);
            } catch (BoardGameException e) {
                System.err.println("Failed to load board: " + e.getMessage());
            }
        } else{
            System.out.println("Invalid choice");
        }
    }
}
