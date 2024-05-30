package blokus.player;

import blokus.logic.Grid;

public class LearningAlphaBeta extends AlphaBetaPlayer{

    private final int initialValue = -1;
    private final int increment = 1;

    public LearningAlphaBeta()
    {
        super("learning-alphabeta-execution-time.csv");
        ALPHABETA_DEPTH = initialValue;
    }

    @Override
    public long playOnGrid(Grid grid) {
        ALPHABETA_DEPTH += increment;
        return super.playOnGrid(grid);
    }
}
