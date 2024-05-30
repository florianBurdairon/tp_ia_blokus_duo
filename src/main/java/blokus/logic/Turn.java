package blokus.logic;

import blokus.utils.BitMaskUtils;

import java.math.BigInteger;

public class Turn implements Cloneable {
    private final Position pos;
    private final Piece piece;
    private final Transform transform;

    private BigInteger casesMask = null;
    private BigInteger cornersMask = null;
    private BigInteger sidesMask = null;

    public Turn(Position pos, Piece piece, Transform transform) {
        this.pos = pos;
        this.piece = piece;
        this.transform = transform;
    }

    public void moveBy1()
    {
        this.casesMask = this.getCasesMask().shiftLeft(1);
        this.cornersMask = this.getCornersMask().shiftLeft(1);
        this.sidesMask = this.getSidesMask().shiftLeft(1);
        pos.x++;
        if (pos.x == Grid.width) {
            pos.x = 0;
            pos.y++;
            this.casesMask = this.getCasesMask().shiftLeft(BitMaskUtils.offset*2);
            this.cornersMask = this.getCornersMask().shiftLeft(BitMaskUtils.offset*2);
            this.sidesMask = this.getSidesMask().shiftLeft(BitMaskUtils.offset*2);
        }
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

    public BigInteger getCasesMask() {
        if (casesMask == null) {
            casesMask = BitMaskUtils.getCasesBitMask(this);
        }
        return casesMask;
    }

    public BigInteger getCornersMask() {
        if (cornersMask == null) {
            cornersMask = BitMaskUtils.getCornerBitMask(this);
        }
        return cornersMask;
    }

    public BigInteger getSidesMask() {
        if (sidesMask == null) {
            sidesMask = BitMaskUtils.getSideBitMask(this);
        }
        return sidesMask;
    }

    @Override
    public String toString() {
        return "Turn{" +
                "pos=" + pos +
                ", piece=" + piece +
                ", transform=" + transform +
                '}';
    }

    @Override
    public Turn clone() {
        return new Turn(pos.clone(), piece, transform);
    }
}
