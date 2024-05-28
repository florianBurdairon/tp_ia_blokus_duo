package blokus.utils;

import blokus.logic.Grid;
import blokus.logic.Position;
import blokus.logic.Turn;

import java.math.BigInteger;
import java.util.List;

public class BitMaskUtils {

    public static BigInteger getBitMask(int x, int y) {
        BigInteger i = BigInteger.ONE;
        return i.shiftLeft(x + y * Grid.width);
    }

    public static BigInteger getTurnBitMask(Turn turn) {
        BigInteger i = BigInteger.ZERO;
        List<Position> cases = Utils.transform(turn.getPiece().getCases(), turn.getTransform());
        for (Position pos : cases) {
            i = i.or(getBitMask(turn.getPos().x + pos.x, turn.getPos().y + pos.y));
        }
        return i;
    }

    public static BigInteger getCasesBitMask(List<Position> cases, Position pos) {
        BigInteger i = BigInteger.ZERO;
        for (Position casePos : cases) {
            i = i.or(getBitMask(pos.x + casePos.x, pos.y + casePos.y));
        }
        return i;
    }

    public static BigInteger getCornerBitMask(Turn turn) {
        BigInteger i = BigInteger.ZERO;
        List<Position> cases = Utils.transform(turn.getPiece().getCorners(), turn.getTransform());
        for (Position pos : cases) {
            i = i.or(getBitMask(turn.getPos().x + pos.x, turn.getPos().y + pos.y));
        }
        return i;
    }

    public static BigInteger getSideBitMask(Turn turn) {
        BigInteger i = BigInteger.ZERO;
        List<Position> cases = Utils.transform(turn.getPiece().getSides(), turn.getTransform());
        for (Position pos : cases) {
            i = i.or(getBitMask(turn.getPos().x + pos.x, turn.getPos().y + pos.y));
        }
        return i;
    }
}
