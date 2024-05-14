package blokus.render;

import blokus.logic.Grid;
import blokus.logic.Piece;
import blokus.logic.Position;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class PieceRenderer extends ObjectRenderer {
    private final Piece piece;
    private final Position pos;
    private final Grid.PlayerColor playerColor;

    private double prevMouseX;
    private double prevMouseY;

    private final double scale;

    public PieceRenderer(Piece piece, Position pos, double scale, Grid.PlayerColor playerColor) {
        this.piece = piece;
        this.pos = pos;
        this.scale = scale;
        this.playerColor = playerColor;
    }

    void buildObject()
    {
        for(Position casePos : piece.getCases()) {
            Position correctPos = new Position(casePos.x - Grid.width/2, casePos.y - Grid.height/2);
            PieceCellRenderer pieceCellRenderer = new PieceCellRenderer(
                    correctPos,
                    playerColor == Grid.PlayerColor.ORANGE ? Color.DARKORANGE : Color.PURPLE,
                    playerColor == Grid.PlayerColor.ORANGE ? Color.ORANGE : Color.DEEPPINK);
            pieceCellRenderer.setScaleX(scale);
            pieceCellRenderer.setScaleY(scale);
            pieceCellRenderer.setScaleZ(scale);
            pieceCellRenderer.renderInto(world);
        }
        world.setTranslateX(pos.x);
        world.setTranslateZ(pos.y);
        world.setScale(CellRenderer.cellSize*scale);
    }
}
