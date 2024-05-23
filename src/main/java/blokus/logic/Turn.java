package blokus.logic;

public class Turn {
    private final Position pos;
    private final Piece piece;
    private final Transform transform;

    public Turn(Position pos, Piece piece, Transform transform) {
        this.pos = pos;
        this.piece = piece;
        this.transform = transform;
    }

    public Position getPos() {
        return pos;
    }

    public Piece getPiece() {
        return piece;
    }

    public Transform getTransform(){
        return transform;
    }

    @Override
    public String toString() {
        return "Turn{" +
                "pos=" + pos +
                ", piece=" + piece +
                ", transform=" + transform +
                '}';
    }
}
