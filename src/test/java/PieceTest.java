import blokus.logic.Piece;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PieceTest {

    @Test
    public void testPiece() {
        // Given
        List<Piece> pieces = Piece.getPieces();

        // Then
        assertEquals(pieces.size(), 21);
    }
}
