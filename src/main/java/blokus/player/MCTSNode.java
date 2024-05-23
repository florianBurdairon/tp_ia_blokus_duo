package blokus.player;

import blokus.logic.Grid;
import blokus.logic.Turn;

import java.util.*;

public class MCTSNode {
    private final Turn turn;
    private final Grid.PlayerColor color;
    private int nbSuccess;
    private int nbIteration;
    private boolean isEnd = false;

    private final Map<Turn, MCTSNode> children = new HashMap<>();
    private final ArrayList<Turn> possible;

    public MCTSNode(Turn turn, Grid grid, Grid.PlayerColor color) {
        this.turn = turn;
        this.color = color;
        possible = new ArrayList<>(grid.getPossibleTurns(color, grid.getPlayerPieces(color)));
    }

    public boolean expand(Grid grid, int depth) {
        System.out.println("Expanding node at depth " + depth + " with " + possible.size() + " possible turns");
        if (hasPossible()) {
            // Get random possible turn
            Turn turn = possible.get(new Random().nextInt(possible.size()));
            grid.placePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), color);

            // Create new child
            MCTSNode child = new MCTSNode(turn, grid, color.next());
            int score = child.simulate(grid,  color, 0);
            child.addSuccess(score);
            child.incrementIteration();
            grid.removePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), color);

            // Update node
            possible.remove(turn);
            children.put(turn, child);
            nbSuccess += score;
            nbIteration++;

            return true;
        }
        else {
            // Select child
            Turn turn = new Turn(null, null, null);
            MCTSNode selectedChild = new MCTSNode(null, grid, color);
            boolean isChildEnd = true;
            while (isChildEnd && !children.isEmpty()){
                turn = children.keySet().stream().skip(new Random().nextInt(children.size())).findFirst().get(); // TODO: Use tree policy instead of random
                selectedChild = children.get(turn);
                selectedChild.setIsEnd(grid);
                isChildEnd = selectedChild.isEnd();
            }
            if (children.isEmpty()) {
                return false;
            }

            // Expand child
            grid.placePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), color);
            boolean isExpanded = selectedChild.expand(grid, depth + 1);
            grid.removePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), color);

            // Update node
            nbSuccess += selectedChild.getNbSuccess();
            nbIteration++;

            return isExpanded;
        }
    }

    public int simulate(Grid grid, Grid.PlayerColor player, int depth) {
        if (grid.isGameFinished()) {
            Grid.PlayerColor winner = grid.getWinner();
            System.out.println("Simulated winner is : " + winner);
            return winner == MCTSPlayer.currentPlayer ? 1 : winner == MCTSPlayer.currentPlayer.next() ? -1 : 0;
        }
        else {
            if (depth > 42) {
                System.out.println("Should not happen");
                return 0;
            }
            List<Turn> possibleTurns = grid.getPossibleTurns(player, grid.getPlayerPieces(player));
            if (!possibleTurns.isEmpty()) {
                Turn turn = possibleTurns.get(new Random().nextInt(possibleTurns.size()));
                grid.placePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), player);
                int victoire = simulate(grid, player.next(), depth + 1);
                grid.removePieceInGrid(turn.getPiece(), turn.getTransform(), turn.getPos(), player);
                return victoire;
            } else {
                return simulate(grid, player.next(), depth + 1);
            }
        }
    }

    public void addSuccess(int score) {
        nbSuccess += score;
    }

    public void incrementIteration() {
        nbIteration++;
    }

    public Map<Turn, MCTSNode> getChildren() {
        return children;
    }

    public int getNbSuccess() {
        return nbSuccess;
    }

    public int getNbIteration() {
        return nbIteration;
    }

    public Turn getTurn() {
        return turn;
    }

    public boolean hasPossible() {
        return !possible.isEmpty();
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setIsEnd(Grid grid) {
        this.isEnd = grid.isGameFinished();
    }

}
