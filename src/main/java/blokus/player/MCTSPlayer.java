package blokus.player;

import blokus.ai_logic.MCTSNode;
import blokus.logic.Grid;
import blokus.logic.Turn;

import java.util.*;

public class MCTSPlayer implements PlayerInterface{
    public static final int nbSimulations = 200;
    public static final int nbIterations = 200000;

    private static final int processTime = 500;
    public static final int c = 2;

    private Grid.PlayerColor color;
    private MCTSNode root = null;
    public static Grid.PlayerColor currentPlayer;

    private Turn nextTurn;

    @Override
    public void play(Grid grid) {
        long start = System.currentTimeMillis();

        color = grid.getCurrentPlayerColor();
        currentPlayer = color;
        //if (root == null)
        root = new MCTSNode(grid, color);
        do {
            MCTS(grid);

            try {
                Thread.sleep(Math.max(0, processTime - (System.currentTimeMillis() - start)));
            } catch (InterruptedException ignored) {
            }
            System.out.println(color + " played: " + nextTurn);
        } while (!grid.placePiece(nextTurn.getPiece(), nextTurn.getPos(), nextTurn.getTransform()) && !grid.isInterrupted());
        //root = root.getChildren().get(nextTurn);
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
