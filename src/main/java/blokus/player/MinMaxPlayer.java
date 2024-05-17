package blokus.player;

import blokus.logic.Grid;
import blokus.logic.Piece;
import blokus.logic.Position;
import blokus.logic.Turn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MinMaxPlayer implements PlayerInterface {
    private static final int MINIMAX_DEPTH = 1;

    private Grid grid;

    private Turn nextTurn;

    @Override
    public void play(Grid grid) {
        this.grid = grid;
        int minmaxScore = minmax(0, true, grid.getGrid(),
                grid.getPlayerPieces(Grid.PlayerColor.PURPLE),
                grid.getPlayerPieces(Grid.PlayerColor.ORANGE));
        System.out.println("Reachable score: " + minmaxScore);
        grid.placePiece(nextTurn.getPiece(), nextTurn.getPos(), nextTurn.getAngleNextTurn(), nextTurn.isSymmetry());
    }

    private int minmax(int currentDepth, boolean maximizing, Grid.PlayerColor[][] cases, List<Piece> pieces, List<Piece> otherPieces)
    {
        Grid.PlayerColor player = maximizing ? grid.getCurrentPlayerColor() : grid.getCurrentPlayerColor().next();
        int score = maximizing ? (int)Double.NEGATIVE_INFINITY : (int)Double.POSITIVE_INFINITY;

        Map<Turn, Integer> possiblesTurns = Grid.getPossibleTurns(cases, player, pieces);
        for(Turn turn : possiblesTurns.keySet()) {
            int newScore;
            if(currentDepth < MINIMAX_DEPTH) {
                List<Piece> newPieces = new ArrayList<>(pieces);
                newPieces.remove(turn.getPiece());
                newScore = minmax(currentDepth + 1, !maximizing, Grid.placePieceInGrid(cases, turn.getPiece().getCases(), turn.getPos(), player.next()), otherPieces, newPieces);
            } else {
                newScore = possiblesTurns.get(turn);
            }
            if (maximizing) {
                score = Math.max(score, newScore);
            } else {
                score = Math.min(score, newScore);
            }
            possiblesTurns.put(turn, score);
        }
        if (currentDepth == 0) {
            for(Turn turn : possiblesTurns.keySet())
            {
                if (possiblesTurns.get(turn) == score) {
                    nextTurn = turn;
                }
            }
        }
        return score;
    }
}
