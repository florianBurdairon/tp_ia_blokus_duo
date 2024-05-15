package blokus.utils;

import blokus.logic.Grid;
import blokus.logic.Position;

import java.util.List;

public class Utils {

    public static List<Position> transform(List<Position> cases, Grid.Angle angle, boolean doSymmetry)
    {
        return doSymmetry ? symmetry(rotate(cases, angle)) : rotate(cases, angle);
    }

    public static List<Position> rotate(List<Position> cases, Grid.Angle angle) {
        int nbRotate = angle.ordinal();
        for (int i = 0; i < nbRotate; i++) {
            for (Position position : cases) {
                int xTemp = position.x;
                position.x = position.y;
                position.y = -xTemp;
            }
        }

        return cases;
    }

    public static List<Position> symmetry(List<Position> cases) {
        for (Position position : cases) {
            position.y = -position.y;
        }
        return cases;
    }

}
