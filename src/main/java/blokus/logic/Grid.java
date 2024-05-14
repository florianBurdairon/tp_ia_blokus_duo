package blokus.logic;

import blokus.player.Player;
import blokus.player.PlayerInterface;

import java.util.List;

public class Grid {
    public static final int width = 14;
    public static final int height = 14;

    private final PlayerColor[][] grid;

    private final PlayerInterface p1;
    private final PlayerInterface p2;

    private final List<Piece> p1Pieces;
    private final List<Piece> p2Pieces;

    private PlayerColor playerTurn = PlayerColor.ORANGE;

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
        p1.play(this);
    }

    public boolean placePiece(Piece piece, Position position, Angle angle, boolean doSymmetry) {
        if (canFit(piece, position, playerTurn, angle, doSymmetry)) {
            for (Position casePos : piece.getCases()) {
                int x = position.x + casePos.x;
                int y = position.y + casePos.y;
                grid[x][y] = playerTurn;
            }
            if (playerTurn == PlayerColor.ORANGE) {
                p1Pieces.remove(piece);
                playerTurn = PlayerColor.PURPLE;
            } else {
                p2Pieces.remove(piece);
                playerTurn = PlayerColor.ORANGE;
            }
            return true;
        }
        return false;
    }

    public boolean canFit(Piece piece, Position position, PlayerColor color, Angle angle, boolean doSymmetry) {
        return true;
//        boolean haveOneCorner = false;
//        for (Position casePos : piece.getCases()) {
//            int x = position.x + casePos.x;
//            int y = position.y + casePos.y;
//            if (x < 0 || x >= width || y < 0 || y >= height) {
//                return false;
//            }
//            if (grid[x][y] != PlayerColor.EMPTY) {
//                return false;
//            }
//            for(int i = 1; i < 3; i++) {
//                for (int j = 1; j < 3; j++) {
//                    x += (int) Math.pow(-1, i);
//                    y += (int) Math.pow(-1, j);
//                    if (x >= 0 && x < width && y >= 0 && y < height) {
//                        if (grid[x][y] == color) {
//                            haveOneCorner = true;
//                        }
//                    }
//                }
//            }
//        }
//        return haveOneCorner;
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

    public enum PlayerColor {
        EMPTY,
        ORANGE,
        PURPLE,
    }

    public enum Angle {
        DEG_0,
        DEG_90,
        DEG_180,
        DEG_270
    }
}
