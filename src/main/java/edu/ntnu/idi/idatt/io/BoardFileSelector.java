package edu.ntnu.idi.idatt.io;

import java.io.File;

public class BoardFileSelector {
    public static File[] getBoardFiles(String path) {
        File folder = new File(path);
        return folder.listFiles((dir, name) -> name.endsWith(".json"));
    }
}

