package blokus.utils;

import blokus.logic.Piece;
import blokus.logic.Position;

import java.util.List;

public class PieceChecker {
    public static void main(String[] args) {
        System.out.println("Verifying pieces definition:\n");

        List<Piece> pieces = Piece.getPieces();
        for (Piece piece : pieces) {
            String[][] strings = new String[7][7];
            for (int i = 0; i < 7; i++) {
                strings[i] = new String[]{".", ".", ".", ".", ".", ".", "."};
            }
            for (Position p : piece.getCases()) {
                strings[p.x+3][p.y+3] = "#";
            }
            for (Position p : piece.getCorners()) {
                strings[p.x+3][p.y+3] = "O";
            }

            System.out.println("Piece n°" + piece.getId());
            for(String[] s : strings) {
                for (String ss : s) {
                    System.out.print(ss + " ");
                }
                System.out.println();
            }

            System.out.println();
        }
    }
}
