package blokus.utils;

import blokus.logic.Grid;
import blokus.logic.Position;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static Grid.PlayerColor[][] cloneGrid(Grid.PlayerColor[][] originalGrid) {
        Grid.PlayerColor[][] newGrid = new Grid.PlayerColor[Grid.width][Grid.height];
        for (int x = 0; x < newGrid.length; x++) {
            for (int y = 0; y < newGrid[x].length; y++) {
                newGrid[x][y] = originalGrid[x][y];
            }
        }
        return newGrid;
    }

    public static List<Position> transform(List<Position> cases, Grid.Angle angle, boolean doSymmetry)
    {
        List<Position> newCases = new ArrayList<>();
        for (Position aCase : cases) {
            newCases.add(aCase.clone());
        }
        if (doSymmetry) {
            newCases = symmetry(newCases);
            boolean similar = true;
            for (Position newCase : newCases) {
                if (!cases.contains(newCase)) {
                    similar = false;
                    break;
                }
            }

            if (similar) {
                newCases = symmetry(rotate(newCases, Grid.Angle.DEG_90));
                angle = angle.rotate90().rotate90().rotate90();
                return rotate(newCases, angle);
            }
        }

        return rotate(newCases, angle);
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
