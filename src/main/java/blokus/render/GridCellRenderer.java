package blokus.render;

import blokus.logic.Grid;
import blokus.logic.Position;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

public class GridCellRenderer extends CellRenderer {
    private final Position pos;

    public GridCellRenderer(Position pos) {
        this.pos = pos;
    }

    void buildObject()
    {
        boolean isP1StartingPoint = Grid.player1Start.equals(pos.add(new Position(Grid.width/2, Grid.height/2)));
        boolean isP2StartingPoint = Grid.player2Start.equals(pos.add(new Position(Grid.width/2, Grid.height/2)));

        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.WHITE);

        final PhongMaterial materialBottom = new PhongMaterial();
        materialBottom.setDiffuseColor(isP1StartingPoint ? Color.rgb(255, 180, 76) : isP2StartingPoint ? Color.rgb(184, 70, 187) : Color.LIGHTGREY);

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(
                0.2f, 0, 0.2f,
                0.8f, 0, 0.2f,
                0.2f, 0, 0.8f,
                0.8f, 0, 0.8f,
                0, 0.2f, 0,
                1, 0.2f, 0,
                0, 0.2f, 1,
                1, 0.2f, 1);
        mesh.getTexCoords().addAll(
                0.5f, 0.5f
        );
        mesh.getFaces().addAll(
                //0, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, // Bottom
                3, 0, 2, 0, 6, 0, 3, 0, 6, 0, 7, 0, // Front
                0, 0, 4, 0, 6, 0, 0, 0, 6, 0, 2, 0, // Left
                1, 0, 3, 0, 7, 0, 1, 0, 7, 0, 5, 0, // Right
                1, 0, 4, 0, 0, 0, 1, 0, 5, 0, 4, 0 // Back
        );

        TriangleMesh meshBottom = new TriangleMesh();
        meshBottom.getPoints().addAll(
                0.2f, 0, 0.2f,
                0.8f, 0, 0.2f,
                0.2f, 0, 0.8f,
                0.8f, 0, 0.8f);
        meshBottom.getTexCoords().addAll(
                0.5f, 0.5f
        );
        meshBottom.getFaces().addAll(
                0, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0 // Bottom
        );

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);

        MeshView meshViewBottom = new MeshView(meshBottom);
        meshViewBottom.setMaterial(materialBottom);

        world.getChildren().addAll(meshView, meshViewBottom);
        world.setTranslateX(pos.x);
        world.setTranslateZ(pos.y);
    }
}
