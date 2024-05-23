package blokus.player;

import blokus.logic.Grid;
import blokus.logic.Turn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AlphaBetaPlayer implements PlayerInterface {
    private static final int ALPHABETA_DEPTH = 2;

    private static final int processTime = 500;

    private Grid grid;

    private Turn nextTurn;

    @Override
    public void play(Grid grid) {
        long start = System.currentTimeMillis();
        this.grid = grid;
        alphabeta(ALPHABETA_DEPTH, true, -Integer.MAX_VALUE, Integer.MAX_VALUE, grid.getCurrentPlayerColor());

        try {
            Thread.sleep(Math.max(0, processTime - (System.currentTimeMillis() - start)));
        } catch (InterruptedException ignored) {
        }
        grid.placePiece(nextTurn.getPiece(), nextTurn.getPos(), nextTurn.getTransform());
    }

    private int alphabeta(int depth, boolean maximizing, int alpha, int beta, Grid.PlayerColor player)
    {
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
                int newScore = alphabeta(depth - 1, false, alpha, beta, player.next());
                grid.removePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), player);
                if (newScore > score) {
                    score = newScore;
                    if (depth == ALPHABETA_DEPTH) {
                        bestTurns.clear();
                        bestTurns.add(turn);
                    }
                }
                else if (newScore == score && depth == ALPHABETA_DEPTH) {
                    bestTurns.add(turn);
                }
                alpha = Math.max(alpha, score);
                if (score > beta) {
                    break;
                }
            }
        } else {
            score = Integer.MAX_VALUE;
            if (possiblesTurns.isEmpty()) {
                return grid.getPlayerScore(player) - grid.getPlayerScore(player.next());
            }
            for (Turn turn : possiblesTurns) {
                grid.placePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), player);
                int newScore = alphabeta(depth - 1, true, alpha, beta, player.next());
                grid.removePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), player);
                if (newScore < score) {
                    score = newScore;
                }
                beta = Math.min(beta, score);
                if (score < alpha) {
                    break;
                }
            }

        }
        if (depth == ALPHABETA_DEPTH) {
            int index = new Random().nextInt(bestTurns.size());
            nextTurn = bestTurns.get(index);
        }
        return score;
    }
}
