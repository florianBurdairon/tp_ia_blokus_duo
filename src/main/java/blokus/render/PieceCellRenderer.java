package blokus.render;

import blokus.logic.Position;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

public class PieceCellRenderer extends CellRenderer {
    private final Color outlineColor;
    private final Color inlineColor;

    private final Position pos;

    public PieceCellRenderer(Position pos, Color outlineColor, Color inlineColor) {
        this.outlineColor = outlineColor;
        this.inlineColor = inlineColor;
        this.pos = pos;
    }

    void buildObject()
    {
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(outlineColor);

        final PhongMaterial materialBottom = new PhongMaterial();
        materialBottom.setDiffuseColor(inlineColor);

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(
                0.2f, 0.4f, 0.2f,
                0.8f, 0.4f, 0.2f,
                0.2f, 0.4f, 0.8f,
                0.8f, 0.4f, 0.8f,
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
                0.2f, 0.4f, 0.2f,
                0.8f, 0.4f, 0.2f,
                0.2f, 0.4f, 0.8f,
                0.8f, 0.4f, 0.8f);
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
