package blokus.player;

import blokus.logic.Grid;
import blokus.logic.Turn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        int minmaxScore = minmax(MINIMAX_DEPTH, true, grid.getCurrentPlayerColor());
        System.out.println("Reachable score: " + minmaxScore + " with piece: " + nextTurn.getPiece());

        try {
            Thread.sleep(Math.max(0, processTime - (System.currentTimeMillis() - start)));
        } catch (InterruptedException ignored) {
        }
        grid.placePiece(nextTurn.getPiece(), nextTurn.getPos(), nextTurn.getTransform());
    }

    private int minmax(int depth, boolean maximizing, Grid.PlayerColor player) {
//        Grid.PlayerColor player = maximizing ? grid.getCurrentPlayerColor() : grid.getCurrentPlayerColor().next();
//        int score = maximizing ? (int)Double.NEGATIVE_INFINITY : (int)Double.POSITIVE_INFINITY;
//
//        Map<Turn, Integer> possibleTurns = grid.getPossibleTurns(player, pieces);
//        for(Turn turn : possibleTurns.keySet()) {
//            int newScore;
//            List<Piece> newPieces = new ArrayList<>(pieces);
//            newPieces.remove(turn.getPiece());
//            if(currentDepth < MINIMAX_DEPTH) {
//                grid.placePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), player.next());
//                newScore = minmax(currentDepth + 1, !maximizing, otherPieces, newPieces);
//                grid.removePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), player.next());
//            } else {
//                newScore = maximizing ?
//                        Grid.getPlayerScore(newPieces) - Grid.getPlayerScore(otherPieces)
//                        : Grid.getPlayerScore(otherPieces) - Grid.getPlayerScore(newPieces);
//            }
//            if (maximizing) {
//                score = Math.max(score, newScore);
//            } else {
//                score = Math.min(score, newScore);
//            }
//            if (currentDepth == 0)
//                possibleTurns.put(turn, newScore);
//        }
//        if (currentDepth == 0) {
//            List<Turn> bestTurns = new ArrayList<>();
//            for(Turn turn : possibleTurns.keySet())
//            {
//                if (possibleTurns.get(turn) == score) {
//                    bestTurns.add(turn);
//                }
//            }
//            if (bestTurns.isEmpty()) {
//                System.err.println("No possible turns found, should not be possible here");
//                return score;
//            }
//            int index = new Random().nextInt(bestTurns.size());
//            nextTurn = bestTurns.get(index);
//        }
//        return score;
        if (depth == 0) {
            return  maximizing ?
                    grid.getPlayerScore(player) - grid.getPlayerScore(player.next())
                    : grid.getPlayerScore(player.next()) - grid.getPlayerScore(player);
        }
        int score;
        Map<Turn, Integer> possiblesTurns = grid.getPossibleTurns(player, grid.getPlayerPieces(player));
        List<Turn> bestTurns = new ArrayList<>();
        if (maximizing) {
            score = -Integer.MAX_VALUE;
            if (possiblesTurns.isEmpty()) {
                return grid.getPlayerScore(player.next()) - grid.getPlayerScore(player);
            }
            for (Turn turn : possiblesTurns.keySet()) {
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
            for (Turn turn : possiblesTurns.keySet()) {
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
