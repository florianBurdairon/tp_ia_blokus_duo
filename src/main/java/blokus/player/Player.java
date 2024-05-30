package blokus.player;

import blokus.logic.Grid;
import blokus.logic.Piece;

import java.util.List;
import java.util.function.Consumer;

public class Player extends AbstractPlayer {

    public Consumer<List<Piece>> askForPlay;

    public Player() {
        super("player-execution-time.csv");
    }

    @Override
    public long playOnGrid(Grid grid) {
        askForPlay.accept(grid.getPlayerPieces(grid.getCurrentPlayerColor()));
        return 0;
    }

    @Override
    public String playerType() {
        return "human";
    }
}
