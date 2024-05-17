package blokus.render;

import blokus.logic.Grid;
import blokus.logic.Piece;
import blokus.logic.Position;
import blokus.utils.Utils;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.List;

public class PieceRenderer extends ObjectRenderer {

    private final Piece piece;
    private Position pos;
    private final Grid.PlayerColor playerColor;
    private final double scale;
    private final boolean isHollow;
    private Grid.Angle angle;
    private boolean symmetry;

    public PieceRenderer(Piece piece, Position pos, double scale, Grid.PlayerColor playerColor, boolean isHollow, Grid.Angle angle, boolean symmetry) {
        this.piece = piece;
        this.pos = pos;
        this.scale = scale;
        this.playerColor = playerColor;
        this.isHollow = isHollow;
        this.angle = angle;
        this.symmetry = symmetry;
    }

    public PieceRenderer(Piece piece, Position pos, double scale, Grid.PlayerColor playerColor, boolean isHollow) {
        this(piece, pos, scale, playerColor, isHollow, Grid.Angle.DEG_0, false);
    }

    public PieceRenderer(Piece piece, Position pos, double scale, Grid.PlayerColor playerColor) {
        this(piece, pos, scale, playerColor, false);
    }

    void buildObject()
    {
        renderCells();
        world.setTranslateX(pos.x);
        world.setTranslateZ(pos.y);
        world.setScale(CellRenderer.cellSize*scale);
    }

    private void renderCells()
    {
        world.getChildren().clear();
        List<Position> cases = Utils.transform(piece.getCases(), angle, symmetry);
        for(Position casePos : cases) {
            Position correctPos = new Position(casePos.x - Grid.width/2, casePos.y - Grid.height/2);
            PieceCellRenderer pieceCellRenderer = new PieceCellRenderer(
                    correctPos,
                    getDarkColor(),
                    getLightColor());
            //TODO: Allow movement of 1 on +
            //if (!casePos.equals(new Position(0, 0))) {
            //    pieceCellRenderer.world.setOnMouseEntered(event -> {
            //        PlayerInput.getInstance().setMousePos(pos);
            //    });
            //}
            pieceCellRenderer.setScaleX(scale);
            pieceCellRenderer.setScaleY(scale);
            pieceCellRenderer.setScaleZ(scale);
            pieceCellRenderer.renderInto(world);
        }
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

    public void applyRotation(Grid.Angle newAngle) {
        angle = newAngle;
        renderCells();
    }

    public void applySymmetry() {
        symmetry = !symmetry;
        renderCells();
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
