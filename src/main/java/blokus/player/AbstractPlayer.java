package blokus.player;

import blokus.logic.Grid;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public abstract class AbstractPlayer {
    private static final String fileHeader = "src/main/resources/";

    private final String filename;

    public AbstractPlayer(String file) {
        filename = file;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileHeader + filename, true));
            writer.newLine();
            writer.flush();
            writer.close();
        } catch (IOException ignored) {}
    }

    public void play(Grid grid) {
        long processTime = playOnGrid(grid);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileHeader + filename, true));
            writer.write((int) processTime + ";");
            writer.flush();
            writer.close();
        } catch (IOException ignored) {}
    }

    abstract protected long playOnGrid(Grid grid);

    abstract public String playerType();
}
