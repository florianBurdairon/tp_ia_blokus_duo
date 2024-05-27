package blokus.player;

import blokus.logic.Grid;
import blokus.logic.Turn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MinMaxPlayer implements PlayerInterface {
    private static final int MINIMAX_DEPTH = 1;

    private static final int processTime = 500;

    private Grid grid;

    private Turn nextTurn;

    @Override
    public void play(Grid grid) {
        long start = System.currentTimeMillis();
        this.grid = grid;
        minmax(MINIMAX_DEPTH, true, grid.getCurrentPlayerColor());

        try {
            Thread.sleep(Math.max(0, processTime - (System.currentTimeMillis() - start)));
        } catch (InterruptedException ignored) {
        }
        grid.placePiece(nextTurn);
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
                grid.placePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), player);
                int newScore = minmax(depth - 1, false, player.next());
                grid.removePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), player);
                if (newScore > score) {
                    score = newScore;
                    if (depth == MINIMAX_DEPTH) {
                        bestTurns.clear();
                        bestTurns.add(turn);
                    }
                }
                else if (newScore == score && depth == MINIMAX_DEPTH) {
                    bestTurns.add(turn);
                }
            }
        } else {
            score = Integer.MAX_VALUE;
            if (possiblesTurns.isEmpty()) {
                return grid.getPlayerScore(player) - grid.getPlayerScore(player.next());
            }
            for (Turn turn : possiblesTurns) {
                grid.placePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), player);
                int newScore = minmax(depth - 1, true, player.next());
                grid.removePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), player);
                if (newScore < score) {
                    score = newScore;
                }
            }

        }
        if (depth == MINIMAX_DEPTH) {
            int index = new Random().nextInt(bestTurns.size());
            nextTurn = bestTurns.get(index);
        }
        return score;
    }
}
