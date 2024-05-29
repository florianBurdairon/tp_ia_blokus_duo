package blokus.ai_logic;

import blokus.logic.Grid;
import blokus.logic.Turn;
import blokus.player.MCTSPlayer;

import java.util.*;

public class MCTSNode {
    private final Grid.PlayerColor color;
    private int nbSuccess;
    private int nbIteration;
    private boolean isEnd = false;

    private final Map<Turn, MCTSNode> children = new HashMap<>();
    private final ArrayList<Turn> possible;

    public MCTSNode(Grid grid, Grid.PlayerColor color) {
        this.color = color;
        possible = new ArrayList<>(grid.getPossibleTurns(color, grid.getPlayerPieces(color)));
    }

    public Turn retrieveTurn(List<Turn> turns){
        float maxScore = Integer.MIN_VALUE;
        List<Turn> maxTurns = new ArrayList<>();
        for (Turn turn : turns) {
            float score =
                    (float) ((float) children.get(turn).getNbSuccess() / children.get(turn).getNbIteration()
                                                + MCTSPlayer.c * Math.sqrt((float) nbIteration / children.get(turn).getNbIteration()));
            if (score > maxScore) {
                maxTurns.clear();
                maxTurns.add(turn);
                maxScore = score;
            } else if (score == maxScore) {
                maxTurns.add(turn);
            }
        }
        return maxTurns.get(new Random().nextInt(maxTurns.size()));
    }

    public boolean expand(Grid grid, int depth) {
        if (grid.isInterrupted()) {
            return false;
        }
        if (hasPossible()) {
            // Get random possible turn
            Turn turn = possible.get(new Random().nextInt(possible.size()));
            grid.placePieceInGrid(turn, color);

            // Create new child
            MCTSNode child = new MCTSNode(grid, color.next());
            List<Thread> simulationsThreads = new ArrayList<>();
            List<MCTSSimulation> simulations = new ArrayList<>();

            for (int i = 0; i < MCTSPlayer.nbSimulations; i++) {
                MCTSSimulation simu = new MCTSSimulation(child, grid.clone());
                Thread s = new Thread(simu);
                s.start();
                simulationsThreads.add(s);
                simulations.add(simu);
            }
            for (int i = 0; i < simulations.size(); i++) {
                try {
                    simulationsThreads.get(i).join();
                    int score = simulations.get(i).getScore();
                    child.addSuccess(score);
                    child.incrementIteration();
                    nbSuccess += score;
                    nbIteration++;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            grid.removePieceInGrid(turn, color);

            // Update node
            possible.remove(turn);
            children.put(turn, child);

            return true;
        }
        else {
            // Select child
            Turn turn = new Turn(null, null, null);
            MCTSNode selectedChild = new MCTSNode(grid, color);
            boolean isChildEnd = true;
            while (isChildEnd && !children.isEmpty() && !grid.isInterrupted()) {
                turn = retrieveTurn(children.keySet().stream().toList());
                selectedChild = children.get(turn);
                selectedChild.setIsEnd(grid);
                isChildEnd = selectedChild.isEnd();
            }
            if (children.isEmpty()) {
                return false;
            }

            int childSuccess = selectedChild.getNbSuccess();

            // Expand child
            grid.placePieceInGrid(turn, color);
            boolean isExpanded = selectedChild.expand(grid, depth + 1);
            grid.removePieceInGrid(turn, color);

            // Update node
            nbSuccess += selectedChild.getNbSuccess() - childSuccess;
            nbIteration++;

            return isExpanded;
        }
    }

    public int simulate(Grid grid, Grid.PlayerColor player) {
        List<Turn> possibleTurns = grid.getPossibleTurns(player, grid.getPlayerPieces(player));
        if (possibleTurns.isEmpty() && grid.getPossibleTurns(player.next(), grid.getPlayerPieces(player.next()), 1).isEmpty()) {
            Grid.PlayerColor winner = grid.getWinner();
            return winner == MCTSPlayer.currentPlayer ? 1 : winner == MCTSPlayer.currentPlayer.next() ? -1 : 0;
        }
        else {
            if (!possibleTurns.isEmpty()) {
                Turn turn = possibleTurns.get(new Random().nextInt(possibleTurns.size()));
                grid.placePieceInGrid(turn, player);
                int victoire = simulate(grid, player.next());
                grid.removePieceInGrid(turn, player);
                return victoire;
            } else {
                return simulate(grid, player.next());
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

    public Grid.PlayerColor getColor() {
        return color;
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
