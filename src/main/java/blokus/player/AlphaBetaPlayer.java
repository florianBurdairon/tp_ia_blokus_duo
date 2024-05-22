package blokus.player;

import blokus.logic.Grid;
import blokus.logic.Piece;
import blokus.logic.Turn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlphaBetaPlayer implements PlayerInterface {
    private static final int ALPHABETA_DEPTH = 2;

    private static final int processTime = 500;

    private Grid grid;

    private Turn nextTurn;

    @Override
    public void play(Grid grid) {
        long start = System.currentTimeMillis();
        this.grid = grid;
        int minmaxScore = alphabeta(ALPHABETA_DEPTH, true, -Integer.MAX_VALUE, Integer.MAX_VALUE,
                grid.getPlayerPieces(grid.getCurrentPlayerColor()),
                grid.getPlayerPieces(grid.getCurrentPlayerColor().next()));
        System.out.println("Reachable score: " + minmaxScore + " with piece: " + nextTurn.getPiece());

        try {
            Thread.sleep(Math.max(0, processTime - (System.currentTimeMillis() - start)));
        } catch (InterruptedException ignored) {
        }
        grid.placePiece(nextTurn.getPiece(), nextTurn.getPos(), nextTurn.getTransform());
    }

    private int alphabeta(int depth, boolean maximizing, int alpha, int beta, List<Piece> pieces, List<Piece> otherPieces)
    {
//        Grid.PlayerColor player = maximizing ? grid.getCurrentPlayerColor() : grid.getCurrentPlayerColor().next();
//        int score = maximizing ? -Integer.MAX_VALUE : Integer.MAX_VALUE;
//
//        Map<Turn, Integer> possiblesTurns = Grid.getPossibleTurns(cases, player, pieces);
//        for(Turn turn : possiblesTurns.keySet()) {
//            int newScore;
//            List<Piece> newPieces = new ArrayList<>(pieces);
//            newPieces.remove(turn.getPiece());
//            if(currentDepth < ALPHABETA_DEPTH) {
//                newScore = alphabeta(currentDepth + 1, !maximizing, alpha, beta, Grid.placePieceInGrid(cases, turn.getPiece().getCases(), turn.getPos(), player.next()), otherPieces, newPieces);
//            } else {
//                newScore = maximizing ?
//                        Grid.getPlayerScore(newPieces) - Grid.getPlayerScore(otherPieces)
//                        : Grid.getPlayerScore(otherPieces) - Grid.getPlayerScore(newPieces);
//            }
//            if (maximizing) {
//                score = Math.max(score, newScore);
//                if (beta <= score)
//                    break;
//                alpha = Math.max(alpha, score);
//            } else {
//                score = Math.min(score, newScore);
//                if (score <= alpha)
//                    break;
//                beta = Math.min(beta, score);
//            }
//            if (currentDepth == 0)
//                possiblesTurns.put(turn, newScore);
//        }
//        if (currentDepth == 0) {
//            List<Turn> possibleTurns = new ArrayList<>();
//            for(Turn turn : possiblesTurns.keySet())
//            {
//                if (possiblesTurns.get(turn) == score) {
//                    possibleTurns.add(turn);
//                }
//            }
//            if (possibleTurns.isEmpty()) {
//                System.err.println("No possible turns found, should not be possible here");
//                return 0;
//            }
//            int index = new Random().nextInt(possibleTurns.size());
//            nextTurn = possibleTurns.get(index);
//        }
//        return score;
        if (depth == 0) {
            return  maximizing ?
                    Grid.getPlayerScore(otherPieces) - Grid.getPlayerScore(pieces)
                    : Grid.getPlayerScore(pieces) - Grid.getPlayerScore(otherPieces);
        }
        int score;
        Map<Turn, Integer> possiblesTurns;
        if (maximizing) {
            score = -Integer.MAX_VALUE;
            possiblesTurns = grid.getPossibleTurns(grid.getCurrentPlayerColor(), pieces);
            if (possiblesTurns.isEmpty()) {
                return Grid.getPlayerScore(otherPieces) - Grid.getPlayerScore(pieces);
            }
            for (Turn turn : possiblesTurns.keySet()) {
                List<Piece> newPieces = new ArrayList<>(pieces);
                newPieces.remove(turn.getPiece());
                grid.placePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), grid.getCurrentPlayerColor());
                int newScore = alphabeta(depth - 1, false, alpha, beta, otherPieces, newPieces);
                grid.removePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos());
                if (newScore > score) {
                    score = newScore;
                    if (depth == ALPHABETA_DEPTH) {
                        nextTurn = turn;
                    }
                }
                alpha = Math.max(alpha, score);
                if (score > beta) {
                    break;
                }
            }
        } else {
            score = Integer.MAX_VALUE;
            possiblesTurns = grid.getPossibleTurns(grid.getCurrentPlayerColor().next(), otherPieces);
            if (possiblesTurns.isEmpty()) {
                return Grid.getPlayerScore(pieces) - Grid.getPlayerScore(otherPieces);
            }
            for (Turn turn : possiblesTurns.keySet()) {
                List<Piece> newPieces = new ArrayList<>(otherPieces);
                newPieces.remove(turn.getPiece());
                grid.placePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), grid.getCurrentPlayerColor());
                int newScore = alphabeta(depth - 1, true, alpha, beta, pieces, newPieces);
                grid.removePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos());
                if (newScore < score) {
                    score = newScore;
                }
                beta = Math.min(beta, score);
                if (score < alpha) {
                    break;
                }
            }

        }
        return score;
    }
}
