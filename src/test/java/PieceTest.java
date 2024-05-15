import blokus.logic.Grid;
import blokus.logic.Piece;
import blokus.logic.Position;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PieceTest {

    @Test
    public void testPiece() {
        // Given
        List<Piece> pieces = Piece.getPieces();

        // Then
        assertEquals(pieces.size(), 21);
    }

}
