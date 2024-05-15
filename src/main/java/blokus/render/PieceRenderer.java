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
    private Position pos;
    private final Grid.PlayerColor playerColor;
    private final double scale;
    private final boolean isHollow;

    public PieceRenderer(Piece piece, Position pos, double scale, Grid.PlayerColor playerColor, boolean isHollow) {
        this.piece = piece;
        this.pos = pos;
        this.scale = scale;
        this.playerColor = playerColor;
        this.isHollow = isHollow;
    }

    public PieceRenderer(Piece piece, Position pos, double scale, Grid.PlayerColor playerColor) {
        this(piece, pos, scale, playerColor, false);
    }

    void buildObject()
    {
        for(Position casePos : piece.getCases()) {
            Position correctPos = new Position(casePos.x - Grid.width/2, casePos.y - Grid.height/2);
            PieceCellRenderer pieceCellRenderer = new PieceCellRenderer(
                    correctPos,
                    getDarkColor(),
                    getLightColor());
            pieceCellRenderer.setScaleX(scale);
            pieceCellRenderer.setScaleY(scale);
            pieceCellRenderer.setScaleZ(scale);
            pieceCellRenderer.renderInto(world);
        }
        world.setTranslateX(pos.x);
        world.setTranslateZ(pos.y);
        world.setScale(CellRenderer.cellSize*scale);
    }

    public Color getDarkColor(){
        return isHollow ?
                playerColor == Grid.PlayerColor.ORANGE ? Color.rgb(255, 148, 0, 0.8) : Color.rgb(167, 0, 171, 0.8)
                : playerColor == Grid.PlayerColor.ORANGE ? Color.rgb(255, 148, 0) : Color.rgb(167, 0, 171);
    }

    public Color getLightColor() {
        return isHollow ?
                playerColor == Grid.PlayerColor.ORANGE ? Color.rgb(255, 180, 76, 0.8) : Color.rgb(184, 70, 187, 0.8)
                : playerColor == Grid.PlayerColor.ORANGE ? Color.rgb(255, 180, 76) : Color.rgb(184, 70, 187);
    }

    public void moveToCoords(Position newPos)
    {
        pos = newPos;
        world.setTranslateX(pos.x * CellRenderer.cellSize*scale);
        world.setTranslateZ(pos.y * CellRenderer.cellSize*scale);
    }

    public Piece getPiece()
    {
        return piece;
    }

    public Position getPos()
    {
        return pos;
    }
}
