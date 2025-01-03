package blokus.render;

import blokus.logic.Grid;
import blokus.logic.Position;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

public class GridRenderer extends ObjectRenderer {
    private final Grid grid;

    private final Group cellGroup = new Group();

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
        meshView.setScaleX(Grid.width);
        meshView.setScaleZ(Grid.height);
        meshView.setMaterial(material);

        world.getChildren().add(meshView);
        world.getChildren().add(cellGroup);

        updateAll();

        world.setScale(CellRenderer.cellSize);
    }

    public void updatePos(Position pos) {
        if (pos.x < 0 || pos.x >= Grid.width || pos.y < 0 || pos.y >= Grid.height)
            return;
        if (grid.currentGrid[pos.x][pos.y] == Grid.PlayerColor.EMPTY) {
            GridCellRenderer gridCellRenderer = new GridCellRenderer(pos.add(new Position(- Grid.width / 2, - Grid.height / 2)));
            gridCellRenderer.renderInto(cellGroup);
            gridCellRenderer.world.setOnMouseEntered(event -> PlayerInput.getInstance().setMousePos(pos));
        } else if (grid.getCase(pos.x, pos.y) == Grid.PlayerColor.ORANGE) {
            PieceCellRenderer pieceCellRenderer = new PieceCellRenderer(
                    new Position(pos.x - Grid.width / 2, pos.y - Grid.height / 2),
                    Color.rgb(255, 148, 0),
                    Color.rgb(255, 180, 76));
            pieceCellRenderer.renderInto(cellGroup);
            pieceCellRenderer.world.setOnMouseEntered(event -> PlayerInput.getInstance().setMousePos(pos));
        } else {
            PieceCellRenderer pieceCellRenderer = new PieceCellRenderer(
                    new Position(pos.x - Grid.width / 2, pos.y - Grid.height / 2),
                    Color.rgb(167, 0, 171),
                    Color.rgb(184, 70, 187));
            pieceCellRenderer.renderInto(cellGroup);
            pieceCellRenderer.world.setOnMouseEntered(event -> PlayerInput.getInstance().setMousePos(pos));
        }
    }

    public void updateAll() {
        this.cellGroup.getChildren().clear();
        for (int x = 0; x < Grid.width; x++) {
            for (int y = 0; y < Grid.height; y++) {
                updatePos(new Position(x, y));
            }
        }
    }
}
