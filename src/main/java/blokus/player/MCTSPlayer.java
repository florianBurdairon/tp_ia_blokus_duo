package blokus.player;

import blokus.logic.Grid;
import blokus.logic.Turn;

import java.util.*;

public class MCTSPlayer implements PlayerInterface{
    private static final int nbSimulations = 100;

    private static final int processTime = 500;
    private final int c = 2;

    private Grid.PlayerColor color;
    private MCTSNode root;
    public static Grid.PlayerColor currentPlayer;

    private Turn nextTurn;

    @Override
    public void play(Grid grid) {
        long start = System.currentTimeMillis();

        color = grid.getCurrentPlayerColor();
        currentPlayer = color;
        root = new MCTSNode(new Turn(null, null, null), grid, color);
        do {
            MCTS(grid);

            try {
                Thread.sleep(Math.max(0, processTime - (System.currentTimeMillis() - start)));
            } catch (InterruptedException ignored) {
            }
        } while (!grid.placePiece(nextTurn.getPiece(), nextTurn.getPos(), nextTurn.getTransform()));
    }

    public void MCTS(Grid grid) {
        for (int i = 0; i < nbSimulations; i++) {
            System.out.println("Simulation " + i);
            root.expand(grid, 0);
        }
        float maxChance = 0;
        for (Map.Entry<Turn, MCTSNode> child : root.getChildren().entrySet()) {
            float chance = (float)child.getValue().getNbSuccess()/(float)child.getValue().getNbIteration();
            if (chance > maxChance) {
                maxChance = chance;
                nextTurn = child.getKey();
            }
        }
    }

//    public int expand(Grid grid, MCTSNode node, Grid.PlayerColor color) {
//        if (node.hasPossible()) {
//            Turn turn = node.getPossible().get(new Random().nextInt(node.getPossible().size()));
//            grid.placePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), color);
//            int score = simulate(grid, color, 0);
//            grid.removePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), color);
//
//            MCTSNode child = new MCTSNode(turn, grid, color.next());
//            child.incrementIteration();
//            child.addSuccess(score);
//            node.addChild(turn, node);
//            node.incrementIteration();
//            node.addSuccess(score);
//
//            node.removePossible(turn);
//
//            return score;
//        } else {
//            Turn turn = node.getChildren().keySet().stream().toList().get(new Random().nextInt(node.getChildren().size()));
//            grid.placePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), color);
//            MCTSNode n = node.getChildren().get(turn);
//            int score = expand(grid, n, color.next());
//            grid.removePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), color);
//
//            node.incrementIteration();
//            node.addSuccess(score);
//
//            return score;
//        }
//    }
//
//    public int simulate(Grid grid, Grid.PlayerColor player, int depth) {
//        if (grid.isGameFinished()) {
//            Grid.PlayerColor winner = grid.getWinner();
//            System.out.println("Simulated winner is : " + winner);
//            return winner == this.color ? 1 : winner == this.color.next() ? -1 : 0;
//        }
//        else {
//            if (depth > 42) {
//                System.out.println("Should not happen");
//                System.out.println("Depth : " + depth);
//                System.out.println("Nb pieces Orange : " + grid.getPlayerPieces(Grid.PlayerColor.ORANGE).size());
//                System.out.println("Nb pieces Purple : " + grid.getPlayerPieces(Grid.PlayerColor.PURPLE).size());
//                return 0;
//            }
//            List<Turn> possibleTurns = grid.getPossibleTurns(player, grid.getPlayerPieces(player));
//            if (!possibleTurns.isEmpty()) {
//                Turn turn = possibleTurns.get(new Random().nextInt(possibleTurns.size()));
//                grid.placePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), player);
//                int victoire = simulate(grid, player.next(), depth + 1);
//                grid.removePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), player);
//                return victoire;
//            } else {
//                return simulate(grid, player.next(), depth + 1);
//            }
//        }
//    }
}
