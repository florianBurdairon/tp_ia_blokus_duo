package blokus.utils;

import blokus.logic.Grid;
import blokus.logic.Position;
import blokus.logic.Turn;

import java.util.List;

public class BitMaskUtils {

    public static long getBitMask(int x, int y) {
        return 1L << (x + y * Grid.width);
    }

    public static long getTurnBitMask(Turn turn) {
        long mask = 0;
        List<Position> cases = Utils.transform(turn.getPiece().getCases(), turn.getTransform());
        for (Position pos : cases) {
            mask |= getBitMask(turn.getPos().x + pos.x, turn.getPos().y + pos.y);
        }
        return mask;
    }

    public static long getCasesBitMask(List<Position> cases, Position pos) {
        long mask = 0;
        for (Position casePos : cases) {
            mask |= getBitMask(pos.x + casePos.x, pos.y + casePos.y);
        }
        return mask;
    }

    public static long getCornerBitMask(Turn turn) {
        long mask = 0;
        List<Position> cases = Utils.transform(turn.getPiece().getCorners(), turn.getTransform());
        for (Position pos : cases) {
            mask |= getBitMask(turn.getPos().x + pos.x, turn.getPos().y + pos.y);
        }
        return mask;
    }
}
