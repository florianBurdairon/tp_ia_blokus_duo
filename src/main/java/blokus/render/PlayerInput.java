package blokus.render;

import blokus.logic.Grid;
import blokus.logic.Piece;
import blokus.logic.Position;

public class PlayerInput {
    private static PlayerInput instance;

    private Position mousePos = new Position(0, 0);

    private PieceRenderer selectedPiece;

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
}
