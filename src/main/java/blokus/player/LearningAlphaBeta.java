package blokus.player;

import blokus.logic.Grid;

public class LearningAlphaBeta extends AlphaBetaPlayer{

    private final int initialValue = 1;
    private float totalIncrement = 0.0f;
    private final float increment = 0.3f;

    public LearningAlphaBeta()
    {
        super("learning-alphabeta-execution-time.csv");
        ALPHABETA_DEPTH = initialValue;
    }

    @Override
    public long playOnGrid(Grid grid) {
        totalIncrement += increment;
        ALPHABETA_DEPTH = initialValue + (int)Math.floor(totalIncrement);
        return super.playOnGrid(grid);
    }
}
