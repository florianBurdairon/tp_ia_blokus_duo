package blokus.player;

import blokus.logic.Grid;
import blokus.logic.Piece;
import blokus.logic.Position;

import java.util.List;
import java.util.function.Consumer;

public class ThreeDimensionalPlayer implements PlayerInterface {

    private Grid grid;

    public Consumer<List<Piece>> askForPlay;

    @Override
    public void play(Grid grid) {
        this.grid = grid;
        askForPlay.accept(grid.getP1Pieces());
    }

    public boolean playPieceAt(Piece piece, Position pos) {
        System.out.println("Played at " + pos);
        return grid.placePiece(piece, pos, Grid.Angle.DEG_0, false);
    }
}
