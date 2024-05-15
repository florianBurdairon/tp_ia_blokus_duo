package blokus.logic;

import blokus.player.PlayerInterface;
import blokus.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Grid {
    public static final int width = 14;
    public static final int height = 14;

    private final int bonusAllPiecesPlaced = 15;
    private final int bonusSmallPiece = 5;

    private final PlayerColor[][] grid;

    private final PlayerInterface p1;
    private final PlayerInterface p2;

    private final List<Piece> p1Pieces;
    private final List<Piece> p2Pieces;

    private final Position player1Start = new Position(4, 4);
    private final Position player2Start = new Position(width - 4 - 1, height - 4 - 1);

    private Piece player1LastPiece = null;
    private Piece player2LastPiece = null;

    private PlayerColor playerTurn = PlayerColor.ORANGE;
    private boolean hasPlayed = false;

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
            playTurn();
            playerTurn = playerTurn.next();
        }
        System.out.println("Game finished");
        System.out.println("Winner is " + getWinner());
    }

    public boolean placePiece(Piece piece, Position position, Angle angle, boolean doSymmetry) {
        if (canFit(piece, position, playerTurn, angle, doSymmetry)) {
            for (Position casePos : Utils.transform(piece.getCases(), angle, doSymmetry)) {
                int x = position.x + casePos.x;
                int y = position.y + casePos.y;
                grid[x][y] = playerTurn;
            }
            if (playerTurn == PlayerColor.ORANGE) {
                player1LastPiece = piece;
                p1Pieces.remove(piece);
            } else {
                player2LastPiece = piece;
                p2Pieces.remove(piece);
            }
            hasPlayed = true;
            return true;
        }
        return false;
    }

    public boolean canFit(Piece piece, Position position, PlayerColor color, Angle angle, boolean doSymmetry) {
        boolean haveOneCorner = false;
        try {
            for (Position casePos : Utils.transform(piece.getCases(), angle, doSymmetry)) {
                int x = position.x + casePos.x;
                int y = position.y + casePos.y;

                // Check if piece is on the starting points
                if(color == PlayerColor.ORANGE && player1LastPiece == null  && x == player1Start.x && y == player1Start.y) {
                    System.out.println("Place ORANGE on start point (" + x + ", " + y + ")");
                    return true;
                }
                if(color == PlayerColor.PURPLE && player2LastPiece == null  && x == player2Start.x && y == player2Start.y) {
                    System.out.println("Place PURPLE on start point (" + x + ", " + y + ")");
                    return true;
                }

                // Check if piece is on the grid
                if (x < 0 || x >= width || y < 0 || y >= height) {
                    System.out.println("Piece out of grid (" + x + ", " + y + ")");
                    return false;
                }

                // Check if piece is on a taken case
                if (grid[x][y] != PlayerColor.EMPTY) {
                    System.out.println("Piece on taken case (" + x + ", " + y + ")");
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
                        System.out.println("Piece has a border with a piece of the same color (" + x + ", " + y + ")");
                        return false;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {

        }
        if (!haveOneCorner) {
            System.out.println("No corner allow to place here" + position);
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
                                || canFit(piece, new Position(x, y), playerTurn, angle, true))
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
        return !canPlayerPlay(PlayerColor.ORANGE) && !canPlayerPlay(PlayerColor.PURPLE);
    }

    public PlayerColor getWinner() {
        // Bonus if player placed all its pieces
        int orangeScore = p1Pieces.isEmpty() ? bonusAllPiecesPlaced : 0;
        int purpleScore = p2Pieces.isEmpty() ? bonusAllPiecesPlaced : 0;

        // Bonus if player ended with the smallest piece
        if (player1LastPiece.getCaseNumber() == 1) {
            orangeScore += bonusSmallPiece;
        }
        if (player2LastPiece.getCaseNumber() == 1) {
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

    public PlayerInterface getP1() {
        return p1;
    }

    public PlayerInterface getP2() {
        return p2;
    }

    public PlayerInterface getCurrentPlayer() {
        return playerTurn == PlayerColor.ORANGE ? p1 : p2;
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
