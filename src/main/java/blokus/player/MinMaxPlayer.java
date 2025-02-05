package blokus.player;

import blokus.logic.Grid;
import blokus.logic.Turn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MinMaxPlayer extends AbstractPlayer {
    private static final int MINMAX_DEPTH = 2;

    private static final int processTime = 500;

    private Grid grid;

    private Turn nextTurn;

    public MinMaxPlayer() {
        super("minmax-execution-time.csv");
    }

    @Override
    public long playOnGrid(Grid grid) {
        long start = System.currentTimeMillis();
        this.grid = grid;
        minmax(MINMAX_DEPTH, true, grid.getCurrentPlayerColor());

        long executionTime = (System.currentTimeMillis() - start);
        try {
            System.out.println("Execution time: " + executionTime + "ms");
            Thread.sleep(Math.max(0, processTime - executionTime));
        } catch (InterruptedException ignored) {
        }
        grid.placePiece(nextTurn);
        return executionTime;
    }

    @Override
    public String playerType() {
        return "minmax";
    }

    private int minmax(int depth, boolean maximizing, Grid.PlayerColor player) {
        if (depth == 0) {
            return  maximizing ?
                    grid.getPlayerScore(player) - grid.getPlayerScore(player.next())
                    : grid.getPlayerScore(player.next()) - grid.getPlayerScore(player);
        }
        int score;
        List<Turn> possiblesTurns = grid.getPossibleTurns(player, grid.getPlayerPieces(player));
        List<Turn> bestTurns = new ArrayList<>();
        if (maximizing) {
            score = -Integer.MAX_VALUE;
            if (possiblesTurns.isEmpty()) {
                return grid.getPlayerScore(player.next()) - grid.getPlayerScore(player);
            }
            for (Turn turn : possiblesTurns) {
                grid.placePieceInGrid(turn, player);
                int newScore = minmax(depth - 1, false, player.next());
                grid.removePieceInGrid(turn, player);
                if (newScore > score) {
                    score = newScore;
                    if (depth == MINMAX_DEPTH) {
                        bestTurns.clear();
                        bestTurns.add(turn);
                    }
                }
                else if (newScore == score && depth == MINMAX_DEPTH) {
                    bestTurns.add(turn);
                }
            }
        } else {
            score = Integer.MAX_VALUE;
            if (possiblesTurns.isEmpty()) {
                return grid.getPlayerScore(player) - grid.getPlayerScore(player.next());
            }
            for (Turn turn : possiblesTurns) {
                grid.placePieceInGrid(turn, player);
                int newScore = minmax(depth - 1, true, player.next());
                grid.removePieceInGrid(turn, player);
                if (newScore < score) {
                    score = newScore;
                }
            }

        }
        if (depth == MINMAX_DEPTH) {
            int index = new Random().nextInt(bestTurns.size());
            nextTurn = bestTurns.get(index);
        }
        return score;
    }
}
