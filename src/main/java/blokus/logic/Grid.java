package blokus.logic;

import blokus.player.PlayerInterface;
import blokus.utils.Utils;

import java.util.*;

public class Grid implements Observable {
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

    public void start(){
        while (!isGameFinished()) {
            playTurn();
            playerTurn = playerTurn.next();
        }
        System.out.println("Game finished");
        System.out.println("Winner is " + getWinner());
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
        for (Piece piece : pieces) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (List<Position> transformation : piece.getTransformations().keySet()) {
                        if (grid[x][y] != PlayerColor.EMPTY) {
                            break;
                        }
                        if (canFit(transformation, new Position(x, y), player)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void playTurn() {
        if (canCurrentPlayerPlay()) {
            if (playerTurn == PlayerColor.ORANGE) {
                p1.play(this);
            } else {
                p2.play(this);
            }
            // Wait for player to place a piece
            while (!hasPlayed) {
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
        //System.out.println("Orange can play: " + canPlayerPlay(PlayerColor.ORANGE));
        //System.out.println("Purple can play: " + canPlayerPlay(PlayerColor.PURPLE));
        return !canPlayerPlay(PlayerColor.ORANGE) && !canPlayerPlay(PlayerColor.PURPLE);
    }

    public PlayerColor getWinner() {
        // Bonus if player placed all its pieces
        int orangeScore = p1Pieces.isEmpty() ? bonusAllPiecesPlaced : 0;
        int purpleScore = p2Pieces.isEmpty() ? bonusAllPiecesPlaced : 0;

        // Bonus if player ended with the smallest piece
        if (player1PlayedPieces.isEmpty() && player1PlayedPieces.peek().getCaseNumber() == 1) {
            orangeScore += bonusSmallPiece;
        }
        if (player2PlayedPieces.isEmpty() && player2PlayedPieces.peek().getCaseNumber() == 1) {
            purpleScore += bonusSmallPiece;
        }

        // Minus if player has pieces left
        for (Piece piece : p1Pieces) {
            orangeScore -= piece.getCaseNumber();
        }
        for (Piece piece : p2Pieces) {
            purpleScore -= piece.getCaseNumber();
        }

        System.out.println("Orange score: " + orangeScore);
        System.out.println("Purple score: " + purpleScore);

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
        List<Turn> turns = new ArrayList<>();
        boolean noNeedForColorAround = color == PlayerColor.ORANGE ?
                grid[player1Start.x][player1Start.y] == PlayerColor.EMPTY
                : grid[player2Start.x][player2Start.y] == PlayerColor.EMPTY;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
/*
                if (grid[x][y] != PlayerColor.EMPTY) {
                    break;
                }*/
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
