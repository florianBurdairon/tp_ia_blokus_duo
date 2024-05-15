import blokus.logic.Grid;
import blokus.logic.Position;
import blokus.utils.Utils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {

    @Test
    public void testPieceRotated() {
        assertCases(getDefaultCases(), Utils.rotate(getDefaultCases(), Grid.Angle.DEG_0));

        List<Position> casesRot90 = new ArrayList<>(3);
        casesRot90.add(new Position(0, 0));
        casesRot90.add(new Position(1, 0));
        casesRot90.add(new Position(1, -1));
        assertCases(casesRot90, Utils.rotate(getDefaultCases(), Grid.Angle.DEG_90));

        List<Position> casesRot180 = new ArrayList<>(3);
        casesRot180.add(new Position(0, 0));
        casesRot180.add(new Position(0, -1));
        casesRot180.add(new Position(-1, -1));
        assertCases(casesRot180, Utils.rotate(getDefaultCases(), Grid.Angle.DEG_180));

        List<Position> casesRot270 = new ArrayList<>(3);
        casesRot270.add(new Position(0, 0));
        casesRot270.add(new Position(-1, 0));
        casesRot270.add(new Position(-1, 1));
        assertCases(casesRot270, Utils.rotate(getDefaultCases(), Grid.Angle.DEG_270));


        List<Position> casesRot360 = Utils.rotate(Utils.rotate(getDefaultCases(), Grid.Angle.DEG_180), Grid.Angle.DEG_180);
        assertCases(casesRot360, getDefaultCases());
    }

    @Test
    public void testSymmetry() {
        List<Position> casesRotSymm = new ArrayList<>(3);
        casesRotSymm.add(new Position(0, 0));
        casesRotSymm.add(new Position(0, -1));
        casesRotSymm.add(new Position(1, -1));

        assertCases(casesRotSymm, Utils.symmetry(getDefaultCases()));

        assertCases(getDefaultCases(), Utils.symmetry(Utils.symmetry(getDefaultCases())));
    }

    private List<Position> getDefaultCases() {
        List<Position> cases = new ArrayList<>(3);
        cases.add(new Position(0, 0));
        cases.add(new Position(0, 1));
        cases.add(new Position(1, 1));
        return cases;
    }

    private void assertCases(List<Position> expected, List<Position> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }
}
