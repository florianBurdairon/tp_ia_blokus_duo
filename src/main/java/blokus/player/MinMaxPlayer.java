package blokus.player;

import blokus.logic.Grid;
import blokus.logic.Piece;
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
        int minmaxScore = minmax(0, true,
                grid.getPlayerPieces(grid.getCurrentPlayerColor()),
                grid.getPlayerPieces(grid.getCurrentPlayerColor().next()));
        System.out.println("Reachable score: " + minmaxScore + " with piece: " + nextTurn.getPiece());

        try {
            Thread.sleep(Math.max(0, processTime - (System.currentTimeMillis() - start)));
        } catch (InterruptedException ignored) {
        }
        grid.placePiece(nextTurn.getPiece(), nextTurn.getPos(), nextTurn.getTransform());
    }

    private int minmax(int currentDepth, boolean maximizing, List<Piece> pieces, List<Piece> otherPieces)
    {
        Grid.PlayerColor player = maximizing ? grid.getCurrentPlayerColor() : grid.getCurrentPlayerColor().next();
        int score = maximizing ? (int)Double.NEGATIVE_INFINITY : (int)Double.POSITIVE_INFINITY;

        Map<Turn, Integer> possiblesTurns = grid.getPossibleTurns(player, pieces);
        for(Turn turn : possiblesTurns.keySet()) {
            int newScore;
            List<Piece> newPieces = new ArrayList<>(pieces);
            newPieces.remove(turn.getPiece());
            if(currentDepth < MINIMAX_DEPTH) {
                grid.placePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), player.next());
                newScore = minmax(currentDepth + 1, !maximizing, otherPieces, newPieces);
                grid.removePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos());
            } else {
                newScore = maximizing ?
                        Grid.getPlayerScore(newPieces) - Grid.getPlayerScore(otherPieces)
                        : Grid.getPlayerScore(otherPieces) - Grid.getPlayerScore(newPieces);
            }
            if (maximizing) {
                score = Math.max(score, newScore);
            } else {
                score = Math.min(score, newScore);
            }
            if (currentDepth == 0)
                possiblesTurns.put(turn, newScore);
        }
        if (currentDepth == 0) {
            List<Turn> possibleTurns = new ArrayList<>();
            for(Turn turn : possiblesTurns.keySet())
            {
                if (possiblesTurns.get(turn) == score) {
                    possibleTurns.add(turn);
                }
            }
            if (possibleTurns.isEmpty()) {
                System.err.println("No possible turns found, should not be possible here");
                return score;
            }
            int index = new Random().nextInt(possibleTurns.size());
            nextTurn = possibleTurns.get(index);
        }
        return score;
    }
}
