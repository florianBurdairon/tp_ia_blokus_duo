package blokus.logic;

public class Turn {
    private Position pos;
    private Piece piece;
    private Grid.Angle angleNextTurn;
    private boolean symmetry;

    public Turn(Position pos, Piece piece, Grid.Angle angleNextTurn, boolean symmetry) {
        this.pos = pos;
        this.piece = piece;
        this.angleNextTurn = angleNextTurn;
        this.symmetry = symmetry;
    }

    public Position getPos() {
        return pos;
    }

    public Piece getPiece() {
        return piece;
    }

    public Grid.Angle getAngleNextTurn() {
        return angleNextTurn;
    }

    public boolean isSymmetry() {
        return symmetry;
    }
}
