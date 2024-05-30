package blokus.utils;

import blokus.logic.Grid;
import blokus.logic.Position;
import blokus.logic.Turn;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BitMaskUtils {

    public static final int offset = 3;

    public static BigInteger getBitMask(int x, int y) {
        BigInteger i = BigInteger.ONE;
        if (x >= Grid.width+offset || x < -offset || y >= Grid.height+offset || y < -offset) {
            return BigInteger.ZERO;
        }
        return i.shiftLeft(x + offset + (y + offset) * (Grid.width+offset*2));
    }

    public static BigInteger getCasesBitMask(Turn turn) {
        List<Position> positions = new ArrayList<>(Utils.transform(turn.getPiece().getCases(), turn.getTransform()));
        positions.replaceAll(position -> position.add(turn.getPos()));
        return getBitMask(positions);
    }

    public static BigInteger getCornerBitMask(Turn turn) {
        List<Position> positions = new ArrayList<>(Utils.transform(turn.getPiece().getCorners(), turn.getTransform()));
        positions.replaceAll(position -> position.add(turn.getPos()));
        return getBitMask(positions);
    }

    public static BigInteger getSideBitMask(Turn turn) {
        List<Position> positions = new ArrayList<>(Utils.transform(turn.getPiece().getSides(), turn.getTransform()));
        positions.replaceAll(position -> position.add(turn.getPos()));
        return getBitMask(positions);
    }

    private static BigInteger getBitMask(List<Position> pos) {
        BigInteger i = BigInteger.ZERO;
        for (Position p : pos) {
            i = i.or(getBitMask(p.x, p.y));
        }
        return i;
    }
}
