package blokus.render;

import blokus.logic.Grid;
import blokus.logic.Position;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
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
        meshView.setScaleX(grid.width);
        meshView.setScaleZ(grid.height);
        meshView.setMaterial(material);

        world.getChildren().add(meshView);

        for (int x = 0; x < grid.width; x++) {
            for (int y = 0; y < grid.height; y++) {
                updatePos(new Position(x, y));
            }
        }

        world.setScale(CellRenderer.cellSize);


        final PhongMaterial tableMaterial = new PhongMaterial();
        tableMaterial.setDiffuseColor(Color.DARKGREY);

        TriangleMesh tableMesh = new TriangleMesh();
        tableMesh.getPoints().addAll(
                0, -0.15f, 0,
                1, -0.15f, 0,
                0, -0.15f, 1,
                1, -0.15f, 1);
        tableMesh.getTexCoords().addAll(
                0.5f, 0.5f
        );
        tableMesh.getFaces().addAll(
                0, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0 // Bottom
        );
        MeshView tableMeshView = new MeshView(tableMesh);
        tableMeshView.setCullFace(CullFace.NONE);
        tableMeshView.setScaleX(100);
        tableMeshView.setScaleZ(100);
        tableMeshView.setMaterial(tableMaterial);

        world.getChildren().add(tableMeshView);
    }

    public void updatePos(Position pos) {
        if (grid.getCase(pos.x, pos.y) == Grid.PlayerColor.EMPTY) {
            GridCellRenderer gridCellRenderer = new GridCellRenderer(pos.x - Grid.width / 2, pos.y - Grid.height / 2);
            gridCellRenderer.renderInto(world);
        } else if (grid.getCase(pos.x, pos.y) == Grid.PlayerColor.ORANGE) {
            PieceCellRenderer pieceCellRenderer = new PieceCellRenderer(
                    new Position(pos.x - Grid.width / 2, pos.y - Grid.height / 2),
                    Color.DARKORANGE,
                    Color.ORANGE);
            pieceCellRenderer.renderInto(world);
        } else {
            PieceCellRenderer pieceCellRenderer = new PieceCellRenderer(
                    new Position(pos.x - Grid.width / 2, pos.y - Grid.height / 2),
                    Color.PURPLE,
                    Color.DEEPPINK);
            pieceCellRenderer.renderInto(world);
        }
    }
}
