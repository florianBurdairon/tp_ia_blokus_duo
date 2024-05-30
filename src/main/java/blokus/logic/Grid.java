package blokus.logic;

import blokus.player.PlayerInterface;
import blokus.utils.BitMaskUtils;
import blokus.utils.Utils;

import java.math.BigInteger;
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

    private BigInteger orangeGridBitMask = BigInteger.ZERO;
    private BigInteger purpleGridBitMask = BigInteger.ZERO;
    private final BigInteger startPointOrangeBitMask = BitMaskUtils.getBitMask(player1Start.x, player1Start.y);
    private final BigInteger startPointPurpleBitMask = BitMaskUtils.getBitMask(player2Start.x, player2Start.y);

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
        this.orangeGridBitMask = new BigInteger(grid.orangeGridBitMask.toString());
        this.purpleGridBitMask = new BigInteger(grid.purpleGridBitMask.toString());
        this.p1Pieces = new ArrayList<>(List.copyOf(grid.p1Pieces));
        this.p2Pieces = new ArrayList<>(List.copyOf(grid.p2Pieces));
        this.player1PlayedPieces.addAll(grid.player1PlayedPieces.stream().toList());
        this.player2PlayedPieces.addAll(grid.player2PlayedPieces.stream().toList());
        this.grid = Utils.cloneGrid(grid.grid);
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

    public boolean placePiece(Turn turn) {
        System.out.println(turn);
        if (canFit(turn, playerTurn)) {
            placePieceInGrid(turn, playerTurn);
            hasPlayed = true;

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (!orangeGridBitMask.and(BitMaskUtils.getBitMask(i, j)).equals(BigInteger.ZERO)) {
                        grid[i][j] = PlayerColor.ORANGE;
                    } else if (!purpleGridBitMask.and(BitMaskUtils.getBitMask(i, j)).equals(BigInteger.ZERO)) {
                        grid[i][j] = PlayerColor.PURPLE;
                    } else {
                        grid[i][j] = PlayerColor.EMPTY;
                    }
                }
            }

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

    public void placePieceInGrid(Turn turn, PlayerColor color) {
        //PlayerColor[][] newGrid = Utils.cloneGrid(grid);
        List<Position> transform = Utils.transform(turn.getPiece().getCases(), turn.getTransform());
        for (Position casePos : transform) {
            int x = turn.getPos().x + casePos.x;
            int y = turn.getPos().y + casePos.y;
//            if(x >= 0 && x < width && y >= 0 && y < height)
            if (color == PlayerColor.ORANGE) {
                orangeGridBitMask = orangeGridBitMask.or(BitMaskUtils.getBitMask(x, y));
            } else {
                purpleGridBitMask = purpleGridBitMask.or(BitMaskUtils.getBitMask(x, y));
            }
            //grid[x][y] = color;
        }
        if (color == PlayerColor.ORANGE) {
            player1PlayedPieces.push(turn.getPiece());
            p1Pieces.remove(turn.getPiece());
        } else {
            player2PlayedPieces.push(turn.getPiece());
            p2Pieces.remove(turn.getPiece());
        }
    }

    public void removePieceInGrid(Turn turn, PlayerColor color) {
        //PlayerColor[][] newGrid = Utils.cloneGrid(grid);
        List<Position> transform = Utils.transform(turn.getPiece().getCases(), turn.getTransform());
        for (Position casePos : transform) {
            int x = turn.getPos().x + casePos.x;
            int y = turn.getPos().y + casePos.y;
//            if(x >= 0 && x < width && y >= 0 && y < height)
            if (color == PlayerColor.ORANGE) {
                orangeGridBitMask = orangeGridBitMask.xor(BitMaskUtils.getBitMask(x, y));
            } else {
                purpleGridBitMask = purpleGridBitMask.xor(BitMaskUtils.getBitMask(x, y));
            }
        }
        if (color == PlayerColor.ORANGE) {
            player1PlayedPieces.pop();
            p1Pieces.add(turn.getPiece());
        } else {
            player2PlayedPieces.pop();
            p2Pieces.add(turn.getPiece());
        }
    }

    public boolean canFit(Turn turn, PlayerColor color) {
        BigInteger pieceBitMask = turn.getCasesMask();
        BigInteger cornerBitMask = turn.getCornersMask();
        BigInteger sidesBitMask = turn.getSidesMask();
        // Piece is on another piece
        if (!pieceBitMask.and(orangeGridBitMask).equals(BigInteger.ZERO) || !pieceBitMask.and(purpleGridBitMask).equals(BigInteger.ZERO)) {
            return false;
        }
        for(Position p : Utils.transform(turn.getPiece().getCases(), turn.getTransform())) {
            Position pos = p.add(turn.getPos());
            if (pos.x >= width || pos.y >= height || pos.x < 0 || pos.y < 0) {
                return false;
            }
        }
        if (color == PlayerColor.ORANGE) {
            // First piece and on start point
            if (player1PlayedPieces.isEmpty() && !startPointOrangeBitMask.and(pieceBitMask).equals(BigInteger.ZERO)) {
                return true;
            }
            // Not right next to another
            if(!sidesBitMask.and(orangeGridBitMask).equals(BigInteger.ZERO)) {
                return false;
            }
            // Piece touches corner of another
            return !cornerBitMask.and(orangeGridBitMask).equals(BigInteger.ZERO);
        } else {
            // First piece and on start point
            if (player2PlayedPieces.isEmpty() && !startPointPurpleBitMask.and(pieceBitMask).equals(BigInteger.ZERO)) {
                return true;
            }
            // Not right next to another
            if(!sidesBitMask.and(purpleGridBitMask).equals(BigInteger.ZERO)) {
                return false;
            }
            // Piece touches corner of another
            return !cornerBitMask.and(purpleGridBitMask).equals(BigInteger.ZERO);
        }
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
                } catch (InterruptedException ignored) {
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
        for (Piece piece : pieces.reversed()) {
            for (Transform transformation : piece.getTransformations()) {
                Turn t = new Turn(new Position(0, 0), piece, transformation);
                for (int i = 0; i < Grid.height * Grid.width; i++) {
                    if (canFit(t, color)) {
                        turns.add(t.clone());
                        if (turns.size() >= nbResults) {
                            return turns;
                        }
                    }
                    t.moveBy1();
                }
            }
        }
        return turns;
    }

    public Turn getRandomTurn(PlayerColor color, List<Piece> pieces) {
        int pieceIndex = new Random().nextInt(pieces.size());
        int transformationIndex = new Random().nextInt(pieces.get(pieceIndex).getTransformations().size());
        for (int p = 0; p < pieces.size(); p++) {
            Piece piece = pieces.get((pieceIndex + p) % pieces.size());
            for (int t = 0; t < piece.getTransformations().size(); t++) {
                Transform transformation = piece.getTransformations().get((transformationIndex + t) % piece.getTransformations().size());
                Turn turn = new Turn(new Position(0, 0), piece, transformation);
                for (int i = 0; i < Grid.height * Grid.width; i++) {
                    if (canFit(turn, color)) {
                        return turn;
                    }
                    turn.moveBy1();
                }
            }
        }
        return null;
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
