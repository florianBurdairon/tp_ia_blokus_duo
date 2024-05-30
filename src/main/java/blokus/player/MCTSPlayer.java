package blokus.player;

import blokus.ai_logic.MCTSNode;
import blokus.logic.Grid;
import blokus.logic.Turn;

import java.util.*;

public class MCTSPlayer extends AbstractPlayer {
    public static final int nbSimulations = 1;
    public static final int nbIterations = 1000;

    private static final int processTime = 500;
    public static final int c = 2;

    private MCTSNode root = null;
    public static Grid.PlayerColor currentPlayer;

    private Turn nextTurn;

    public MCTSPlayer() {
        super("mcts-execution-time.csv");
    }

    @Override
    public long playOnGrid(Grid grid) {
        long start = System.currentTimeMillis();

        Grid.PlayerColor color = grid.getCurrentPlayerColor();
        currentPlayer = color;
        //if (root == null)
        root = new MCTSNode(grid, color);
        MCTS(grid);

        long executionTime = (System.currentTimeMillis() - start);
        try {
            System.out.println("Execution time: " + executionTime + "ms");
            Thread.sleep(Math.max(0, processTime - executionTime));
        } catch (InterruptedException ignored) {
        }
        //root = root.getChildren().get(nextTurn);
        grid.placePiece(nextTurn);
        return executionTime;
    }

    @Override
    public String playerType() {
        return "mcts";
    }

    public void MCTS(Grid grid) {
        for (int i = 0; i < nbIterations && !grid.isInterrupted(); i += nbSimulations) {
            root.expand(grid, 0);
        }
        float maxChance = -Integer.MAX_VALUE;
        for (Map.Entry<Turn, MCTSNode> child : root.getChildren().entrySet()) {
            float chance = (float)child.getValue().getNbSuccess()/(float)child.getValue().getNbIteration();
            if (chance > maxChance) {
                maxChance = chance;
                nextTurn = child.getKey();
            }
        }
    }
}
