package blokus.logic;

import blokus.player.PlayerInterface;
import blokus.utils.Utils;

import java.util.*;

public class Grid extends Thread implements Observable, Cloneable, Runnable {
    public static final int width = 14;
    public static final int height = 14;

    public static final int bonusAllPiecesPlaced = 15;
    public static final int bonusSmallPiece = 5;

    public static final Position player1Start = new Position(4, 4);
    public static final Position player2Start = new Position(width - 4 - 1, height - 4 - 1);

    private final List<Observer> observers = new ArrayList<>();

    private final PlayerColor[][] grid;
    public PlayerColor[][] currentGrid;

    private final PlayerInterface p1;
    private final PlayerInterface p2;

    private final List<Piece> p1Pieces;
    public List<Piece> currentP1Pieces;
    private final List<Piece> p2Pieces;
    public List<Piece> currentP2Pieces;

    private final Stack<Piece> player1PlayedPieces = new Stack<>();
    private final Stack<Piece> player2PlayedPieces = new Stack<>();

    private PlayerColor playerTurn = PlayerColor.ORANGE;
    private boolean hasPlayed = false;
    private boolean isInterrupt = false;

    private Grid(Grid grid) {
        this.p1 = grid.p1;
        this.p2 = grid.p2;
        this.grid = Utils.cloneGrid(grid.grid);
        this.p1Pieces = new ArrayList<>(List.copyOf(grid.p1Pieces));
        this.p2Pieces = new ArrayList<>(List.copyOf(grid.p2Pieces));
        this.player1PlayedPieces.addAll(grid.player1PlayedPieces.stream().toList());
        this.player2PlayedPieces.addAll(grid.player2PlayedPieces.stream().toList());
    }

    public Grid(PlayerInterface p1, PlayerInterface p2) {
        grid = new PlayerColor[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = PlayerColor.EMPTY;
            }
        }
        currentGrid = Utils.cloneGrid(grid);

        this.p1 = p1;
        this.p2 = p2;

        p1Pieces = Piece.getPieces();
        p2Pieces = Piece.getPieces();
        currentP1Pieces = new ArrayList<>(p1Pieces);
        currentP2Pieces = new ArrayList<>(p2Pieces);
    }

    @Override
    public void run(){
        while (!isGameFinished() && !isInterrupt) {
            playTurn();
            playerTurn = playerTurn.next();
        }
        if (!isInterrupt) {
            System.out.println("Game finished");
            System.out.println("Winner is " + getWinner());
            System.out.println("Orange score: " + getTotalPlayerScore(PlayerColor.ORANGE));
            System.out.println("Purple score: " + getTotalPlayerScore(PlayerColor.PURPLE));
        }
    }

    public boolean placePiece(Piece piece, Position position, Transform transform) {
        if (canFit(Utils.transform(piece.getCases(), transform), position, playerTurn)) {
            placePieceInGrid(piece, transform, position, playerTurn);
            hasPlayed = true;
            currentGrid = Utils.cloneGrid(grid);
            currentP1Pieces = new ArrayList<>(p1Pieces);
            currentP1Pieces.sort(Comparator.comparingInt(Piece::getId));
            currentP2Pieces = new ArrayList<>(p2Pieces);
            currentP2Pieces.sort(Comparator.comparingInt(Piece::getId));
            updateObservers();
            return true;
        }
        else {
            System.out.println("Piece can't be placed");
        }
        return false;
    }

    public void placePieceInGrid(Piece piece, Transform transformation , Position position, PlayerColor color) {
        //PlayerColor[][] newGrid = Utils.cloneGrid(grid);
        List<Position> transform = Utils.transform(piece.getCases(), transformation);
        for (Position casePos : transform) {
            int x = position.x + casePos.x;
            int y = position.y + casePos.y;
//            if(x >= 0 && x < width && y >= 0 && y < height)
            grid[x][y] = color;
        }
        if (color == PlayerColor.ORANGE) {
            player1PlayedPieces.push(piece);
            p1Pieces.remove(piece);
        } else {
            player2PlayedPieces.push(piece);
            p2Pieces.remove(piece);
        }
    }

    public void removePieceInGrid(Piece piece, Transform transformation, Position position, PlayerColor color) {
        //PlayerColor[][] newGrid = Utils.cloneGrid(grid);
        List<Position> transform = Utils.transform(piece.getCases(), transformation);
        for (Position casePos : transform) {
            int x = position.x + casePos.x;
            int y = position.y + casePos.y;
//            if(x >= 0 && x < width && y >= 0 && y < height)
            grid[x][y] = PlayerColor.EMPTY;
        }
        if (color == PlayerColor.ORANGE) {
            player1PlayedPieces.pop();
            p1Pieces.add(piece);
        } else {
            player2PlayedPieces.pop();
            p2Pieces.add(piece);
        }
    }

    public boolean canFit(List<Position> cases, Position position, PlayerColor color) {
        boolean haveOneCorner = false;
        try {
            for (Position casePos : cases) {
                int x = position.x + casePos.x;
                int y = position.y + casePos.y;

                // Check if piece is on the starting points
                if(color == PlayerColor.ORANGE && player1PlayedPieces.isEmpty() && x == player1Start.x && y == player1Start.y) {
                    return true;
                }
                if(color == PlayerColor.PURPLE && player2PlayedPieces.isEmpty() && x == player2Start.x && y == player2Start.y) {
                    return true;
                }

                // Check if piece is on the grid
                if (x < 0 || x >= width || y < 0 || y >= height) {
                    return false;
                }

                // Check if piece is on a taken case
                if (grid[x][y] != PlayerColor.EMPTY) {
                    return false;
                }

                for(int i = -1; i < 2; i += 2) {
                    // Check if piece has a border with a piece of the same color
                    if ((x + i >= 0 && x + i < width && grid[x + i][y] == color) || (y + i >= 0 && y + i < height && grid[x][y + i] == color)) {
                        return false;
                    }
                    for (int j = -1; j < 2; j +=2) {
                        int xDiag = x + i;
                        int yDiag = y + j;

                        // Check if piece has a corner with a piece of the same color
                        if (xDiag >= 0 && xDiag < width && yDiag >= 0 && yDiag < height) {
                            if (grid[xDiag][yDiag] == color) {
                                haveOneCorner = true;
                            }
                        }
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
            /* Ignored */
        }
        return haveOneCorner;
    }

    public boolean canCurrentPlayerPlay() {
        return canPlayerPlay(playerTurn);
    }

    public boolean canPlayerPlay(PlayerColor player) {
        List<Piece> pieces = new ArrayList<>(player == PlayerColor.ORANGE ? p1Pieces : p2Pieces);
        return !getPossibleTurns(player, pieces, 1).isEmpty();
    }

    public void playTurn() {
        if (canCurrentPlayerPlay()) {
            if (playerTurn == PlayerColor.ORANGE) {
                p1.play(this);
            } else {
                p2.play(this);
            }
            // Wait for player to place a piece
            while (!hasPlayed && !isInterrupt) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            hasPlayed = false;
        }
    }

    public boolean isGameFinished() {
        return !canPlayerPlay(PlayerColor.ORANGE) && !canPlayerPlay(PlayerColor.PURPLE);
    }

    public PlayerColor getWinner() {
        // Score with bonus
        int orangeScore = getTotalPlayerScore(PlayerColor.ORANGE);
        int purpleScore = getTotalPlayerScore(PlayerColor.PURPLE);

        // Winner is biggest score
        if (orangeScore > purpleScore) {
            return PlayerColor.ORANGE;
        } else if (orangeScore < purpleScore) {
            return PlayerColor.PURPLE;
        } else {
            return PlayerColor.EMPTY;
        }
    }

    public List<Turn> getPossibleTurns(PlayerColor color, List<Piece> pieces) {
        return getPossibleTurns(color, pieces, Integer.MAX_VALUE);
    }

    public List<Turn> getPossibleTurns(PlayerColor color, List<Piece> pieces, int nbResults) {
        List<Turn> turns = new ArrayList<>();
        boolean noNeedForColorAround = color == PlayerColor.ORANGE ?
                grid[player1Start.x][player1Start.y] == PlayerColor.EMPTY
                : grid[player2Start.x][player2Start.y] == PlayerColor.EMPTY;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                boolean hasColorAround = noNeedForColorAround;
                for (int xp = -3; xp < 3 && !hasColorAround; xp++) {
                    for (int yp = -3; yp < 3; yp++) {
                        if (x + xp >= 0 && x + xp < width && y + yp >= 0 && y + yp < height && grid[x + xp][y + yp] == color) {
                            hasColorAround = true;
                            break;
                        }
                    }
                }
                if (hasColorAround) {
                    for (Piece piece : pieces) {
                        for (Map.Entry<List<Position>, Transform> transformation : piece.getTransformations().entrySet()) {
                            if (canFit(transformation.getKey(), new Position(x, y), color)) {
                                Turn possibleTurn = new Turn(new Position(x, y), piece, transformation.getValue());
                                turns.add(possibleTurn);
                                if (turns.size() >= nbResults) {
                                    return turns;
                                }
                            }
                        }
                    }
                }
            }
        }
        return turns;
    }

    public int getPlayerScore(PlayerColor player) {
        return getPlayerScore(getPlayerPieces(player));
    }

    public static int getPlayerScore(List<Piece> pieces) {
        int score = 0;
        for (Piece piece : pieces) {
            score -= piece.getCaseNumber();
        }
        return score;
    }

    public int getTotalPlayerScore(PlayerColor player) {
        List<Piece> pieces = getPlayerPieces(player);
        int score = pieces.isEmpty() ? bonusAllPiecesPlaced : getPlayerScore(pieces);
        Stack<Piece> piecesPlayed = player == PlayerColor.ORANGE ? player1PlayedPieces : player2PlayedPieces;
        if (piecesPlayed.peek().getCaseNumber() == 1) {
            score += bonusSmallPiece;
        }
        return score;
    }

    public PlayerColor[][] getGrid() {
        return Utils.cloneGrid(grid);
    }

    public PlayerColor getCase(int x, int y) {
        return grid[x][y];
    }

    public List<Piece> getPlayerPieces(PlayerColor player)
    {
        if (player == PlayerColor.ORANGE)
            return p1Pieces;
        else
            return p2Pieces;
    }

    public PlayerInterface getP1() {
        return p1;
    }

    public PlayerInterface getP2() {
        return p2;
    }

    public PlayerInterface getCurrentPlayer() {
        return playerTurn == PlayerColor.ORANGE ? p1 : p2;
    }

    public PlayerColor getCurrentPlayerColor() {
        return playerTurn;
    }

    @Override
    public void addListener(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeListener(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void updateObservers(){
        for (Observer o : observers)
        {
            o.update();
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        isInterrupt = true;
    }

    @Override
    public boolean isInterrupted() {
        return isInterrupt;
    }

    @Override
    public Grid clone() {
        return new Grid(this);
    }

    public enum PlayerColor {
        EMPTY {
            public PlayerColor next() {
                return EMPTY;
            }

            @Override
            public String toString() {
                return "EMPTY";
            }
        },
        ORANGE {
            public PlayerColor next() {
                return PURPLE;
            }

            @Override
            public String toString() {
                return "ORANGE";
            }
        },
        PURPLE {
            public PlayerColor next() {
                return ORANGE;
            }

            @Override
            public String toString() {
                return "PURPLE";
            }
        };

        public abstract PlayerColor next();
    }

    public enum Angle {
        DEG_0 {
            @Override
            public Angle rotate90() {
                return DEG_90;
            }
        },
        DEG_90 {
            @Override
            public Angle rotate90() {
                return DEG_180;
            }
        },
        DEG_180 {
            @Override
            public Angle rotate90() {
                return DEG_270;
            }
        },
        DEG_270 {
            @Override
            public Angle rotate90() {
                return DEG_0;
            }
        };

        public abstract Angle rotate90();
    }
}
