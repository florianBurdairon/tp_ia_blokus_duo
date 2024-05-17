package blokus.logic;

import blokus.player.PlayerInterface;
import blokus.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grid {
    public static final int width = 14;
    public static final int height = 14;
    public static final int startScore = -89;

    private final int bonusAllPiecesPlaced = 15;
    private final int bonusSmallPiece = 5;

    private static final Position player1Start = new Position(4, 4);
    private static final Position player2Start = new Position(width - 4 - 1, height - 4 - 1);

    private PlayerColor[][] grid;

    private final PlayerInterface p1;
    private final PlayerInterface p2;

    private final List<Piece> p1Pieces;
    private final List<Piece> p2Pieces;

    private Piece player1LastPiece = null;
    private Piece player2LastPiece = null;

    private PlayerColor playerTurn = PlayerColor.ORANGE;
    private boolean hasPlayed = false;

    private static boolean isPlayer1FirstTurn = true;
    private static boolean isPlayer2FirstTurn = true;

    public Grid(PlayerInterface p1, PlayerInterface p2) {
        grid = new PlayerColor[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = PlayerColor.EMPTY;
            }
        }

        this.p1 = p1;
        this.p2 = p2;

        p1Pieces = Piece.getPieces();
        p2Pieces = Piece.getPieces();
    }

    public void start(){
        while (!isGameFinished()) {
            System.out.println("Player " + playerTurn + " turn");
            playTurn();
            playerTurn = playerTurn.next();
        }
        System.out.println("Game finished");
        System.out.println("Winner is " + getWinner());
    }

    public boolean placePiece(Piece piece, Position position, Angle angle, boolean doSymmetry) {
        if (canFit(piece, position, playerTurn, angle, doSymmetry)) {
            grid = placePieceInGrid(grid, Utils.transform(piece.getCases(), angle, doSymmetry), position, playerTurn);
            if (playerTurn == PlayerColor.ORANGE) {
                if (isPlayer1FirstTurn)
                    isPlayer1FirstTurn = false;
                player1LastPiece = piece;
                p1Pieces.remove(piece);
            } else {
                if (isPlayer2FirstTurn)
                    isPlayer2FirstTurn = false;
                player2LastPiece = piece;
                p2Pieces.remove(piece);
            }
            System.out.println(playerTurn + " played piece at (" + position.x + ", " + position.y + ")");
            hasPlayed = true;
            return true;
        }
        return false;
    }

    public static PlayerColor[][] placePieceInGrid(PlayerColor[][] grid, List<Position> piece, Position position, PlayerColor color) {
        PlayerColor[][] newGrid = Utils.cloneGrid(grid);
        for (Position casePos : piece) {
            int x = position.x + casePos.x;
            int y = position.y + casePos.y;
            if(x >= 0 && x < width && y >= 0 && y < height)
                newGrid[x][y] = color;
        }
        return newGrid;
    }

    public boolean canFit(Piece piece, Position position, PlayerColor color, Angle angle, boolean doSymmetry) {
        return canFitInGrid(grid, piece, position, color, angle, doSymmetry);
    }

    public static boolean canFitInGrid(PlayerColor[][] grid, Piece piece, Position position, PlayerColor color, Angle angle, boolean doSymmetry) {
        boolean haveOneCorner = false;
        try {
            for (Position casePos : Utils.transform(piece.getCases(), angle, doSymmetry)) {
                int x = position.x + casePos.x;
                int y = position.y + casePos.y;

                // Check if piece is on the starting points
                if(color == PlayerColor.ORANGE && isPlayer1FirstTurn && x == player1Start.x && y == player1Start.y) {
                    return true;
                }
                if(color == PlayerColor.PURPLE && isPlayer2FirstTurn && x == player2Start.x && y == player2Start.y) {
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
                    // Check if piece has a border with a piece of the same color
                    if (x + i >= 0 && x + i < width && y + i >= 0 && y + i < height && (grid[x + i][y] == color || grid[x][y + i] == color)) {
                        return false;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {

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
                    for (Angle angle : Angle.values()) {
                        if (
                                grid[x][y] == PlayerColor.EMPTY
                                && (canFit(piece, new Position(x, y), playerTurn, angle, true)
                                || canFit(piece, new Position(x, y), playerTurn, angle, false))
                        ) {
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
        System.out.println("Orange can play: " + canPlayerPlay(PlayerColor.ORANGE));
        System.out.println("Purple can play: " + canPlayerPlay(PlayerColor.PURPLE));
        return !canPlayerPlay(PlayerColor.ORANGE) && !canPlayerPlay(PlayerColor.PURPLE);
    }

    public PlayerColor getWinner() {
        // Bonus if player placed all its pieces
        int orangeScore = p1Pieces.isEmpty() ? bonusAllPiecesPlaced : 0;
        int purpleScore = p2Pieces.isEmpty() ? bonusAllPiecesPlaced : 0;

        // Bonus if player ended with the smallest piece
        if (player1LastPiece != null && player1LastPiece.getCaseNumber() == 1) {
            orangeScore += bonusSmallPiece;
        }
        if (player2LastPiece != null && player2LastPiece.getCaseNumber() == 1) {
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

    public static Map<Turn, Integer> getPossibleTurns(PlayerColor[][] grid, PlayerColor color, List<Piece> pieces) {
        Map<Turn, Integer> turns = new HashMap<>();
        for (Piece piece : pieces) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (Angle angle : Angle.values()) {
                        if (
                                grid[x][y] == PlayerColor.EMPTY
                                && (canFitInGrid(grid, piece, new Position(x, y), color, angle, true))
                        ) {;
                            Turn possibleTurn = new Turn(new Position(x, y), piece, angle, true);
                            PlayerColor[][] newGrid = placePieceInGrid(grid, Utils.transform(piece.getCases(), angle, true), new Position(x, y), color);
                            turns.put(possibleTurn, getPlayerCasesPlayed(newGrid, color));
                        }
                        if (
                                grid[x][y] == PlayerColor.EMPTY
                                && (canFitInGrid(grid, piece, new Position(x, y), color, angle, false))
                        ) {;
                            Turn possibleTurn = new Turn(new Position(x, y), piece, angle, false);
                            PlayerColor[][] newGrid = placePieceInGrid(grid, Utils.transform(piece.getCases(), angle, false), new Position(x, y), color);
                            turns.put(possibleTurn, getPlayerCasesPlayed(newGrid, color));
                        }
                    }
                }
            }
        }
        return turns;
    }

    public static int getPlayerCasesPlayed(PlayerColor[][] grid, PlayerColor color) {
        int casesPlayed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == color) {
                    casesPlayed++;
                }
            }
        }
        return casesPlayed;
    }

    public PlayerColor[][] getGrid() {
        return Utils.cloneGrid(grid);
    }

    public PlayerColor getCase(int x, int y) {
        return grid[x][y];
    }

    public void setCase(int x, int y, PlayerColor c) {
        grid[x][y] = c;
    }

    public List<Piece> getP1Pieces() {
        return p1Pieces;    
    }

    public List<Piece> getP2Pieces() {
        return p2Pieces;
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
