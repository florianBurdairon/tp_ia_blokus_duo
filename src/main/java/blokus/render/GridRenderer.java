package blokus.render;

import blokus.logic.Grid;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

public class GridRenderer extends ObjectRenderer {
    private final Grid grid;

    public GridRenderer(Grid grid) {
        this.grid = grid;
    }

    void buildObject()
    {
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.WHITE);
        material.setSpecularColor(Color.WHITE);

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(
                -0.05f, -0.1f, -0.05f,
                1.05f, -0.1f, -0.05f,
                -0.05f, -0.1f, 1.05f,
                1.05f, -0.1f, 1.05f,
                0, 0.2f, 0,
                1, 0.2f, 0,
                0, 0.2f, 1,
                1, 0.2f, 1);
        mesh.getTexCoords().addAll(
                0.5f, 0.5f
        );
        mesh.getFaces().addAll(
                0, 0, 1, 0, 2, 0, 3, 0, 2, 0, 1, 0, // Bottom
                3, 0, 6, 0, 2, 0, 3, 0, 7, 0, 6, 0, // Front
                0, 0, 6, 0, 4, 0, 0, 0, 2, 0, 6, 0, // Left
                1, 0, 7, 0, 3, 0, 1, 0, 5, 0, 7, 0, // Right
                1, 0, 0, 0, 4, 0, 1, 0, 4, 0, 5, 0 // Back
        );

        MeshView meshView = new MeshView(mesh);
        meshView.setTranslateX(-0.5f);
        meshView.setTranslateZ(-0.5f);
        meshView.setScaleX(14);
        meshView.setScaleZ(14);
        meshView.setMaterial(material);

        world.getChildren().add(meshView);

        for (int x = 0; x < grid.width; x++) {
            for (int y = 0; y < grid.height; y++) {
                GridCellRenderer gridCellRenderer = new GridCellRenderer(x - grid.width/2, y - grid.height/2);
                gridCellRenderer.renderInto(world);
            }
        }

        world.setScale(CellRenderer.cellSize);
    }
}
