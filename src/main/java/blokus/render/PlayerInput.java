package blokus.render;

import blokus.logic.Grid;
import blokus.logic.Piece;
import blokus.logic.Position;

public class PlayerInput {
    private static PlayerInput instance;

    private Position mousePos = new Position(0, 0);

    private PieceRenderer selectedPiece;
    private Grid.Angle rotation = Grid.Angle.DEG_0;
    private boolean symmetry = false;

    private PlayerInput(){}

    public static PlayerInput getInstance() {
        if (instance == null)
            instance = new PlayerInput();
        return  instance;
    }

    public void setMousePos(Position pos){
        mousePos = pos;
        movePieceToMouse();
    }

    public Position getMousePos(){
        return mousePos;
    }

    public void selectPiece(Piece piece, Grid.PlayerColor pieceColor)
    {
        BlokusScene.tempGroup.getChildren().clear();
        selectedPiece = new PieceRenderer(piece, mousePos, 1, pieceColor, true);
        selectedPiece.renderInto(BlokusScene.tempGroup);
        movePieceToMouse();
    }

    public void unselectPiece(){
        BlokusScene.tempGroup.getChildren().clear();
        rotation = Grid.Angle.DEG_0;
        symmetry = false;
        selectedPiece = null;
    }

    public PieceRenderer getSelectedPiece()
    {
        return selectedPiece;
    }

    private void movePieceToMouse(){
        if (selectedPiece != null) {
            selectedPiece.moveToCoords(mousePos);
        }
    }

    public void addRotation(Grid.Angle angle){
        for (int i = 0; i < angle.ordinal(); i++) {
            rotation = rotation.rotate90();
        }
        System.out.println("Rotation from PI: " + rotation);
        selectedPiece.applyRotation(rotation);
    }

    public Grid.Angle getRotation(){
        return rotation;
    }

    public void toggleSymmetry(){
        System.out.println("Applied from PlayerInput");
        symmetry = !symmetry;
        selectedPiece.applySymmetry();
    }

    public boolean hasSymmetry(){
        return symmetry;
    }
}
