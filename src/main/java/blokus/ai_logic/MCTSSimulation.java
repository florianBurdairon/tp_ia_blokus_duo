package blokus.ai_logic;

import blokus.logic.Grid;

public class MCTSSimulation implements Runnable {
    private final MCTSNode node;
    private final Grid grid;

    private volatile int score;

    public MCTSSimulation(MCTSNode node, Grid grid) {
        this.node = node;
        this.grid = grid;
    }


    @Override
    public void run() {
        score = node.simulate(grid, node.getColor());
    }

    public int getScore() {
        return score;
    }
}
