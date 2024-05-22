package blokus.player;

import blokus.logic.Grid;
import blokus.logic.Piece;

import java.util.List;
import java.util.function.Consumer;

public class Player implements PlayerInterface {

    public Consumer<List<Piece>> askForPlay;

    @Override
    public void play(Grid grid) {
        askForPlay.accept(grid.getPlayerPieces(grid.getCurrentPlayerColor()));
    }

//    public boolean playPieceAt(Piece piece, Position pos) {
//        System.out.println("Played at " + pos);
//        return grid.placePiece(piece, pos, Grid.Angle.DEG_0, false);
//    }
}
