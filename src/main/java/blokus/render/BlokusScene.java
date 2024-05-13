package blokus.render;

import blokus.logic.Grid;
import blokus.logic.Piece;
import blokus.logic.Position;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.stage.Stage;

import java.util.List;

public class BlokusScene extends Application {
    private Scene scene;
    private Grid grid;
    private GridRenderer gridRenderer;

    final Group root = new Group();
    final XForm lightGroup = new XForm();
    final XForm axisGroup = new XForm();

    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final XForm cameraXForm = new XForm();
    final XForm cameraXForm2 = new XForm();
    final XForm cameraXForm3 = new XForm();
    final XForm world = new XForm();

    private static final double CAMERA_INITIAL_DISTANCE = -1200;
    private static final double CAMERA_INITIAL_X_ANGLE = 35.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 135.0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    private static final double CONTROL_MULTIPLIER = 0.75;
    private static final double SHIFT_MULTIPLIER = 5.0;
    private static final double MOUSE_SPEED = 0.2;
    private static final double ROTATION_SPEED = 1.5;
    private static final double TRACK_SPEED = 3;
    private static final double SCROLL_SPEED = 2;
    private static final double AXIS_LENGTH = 250.0;

    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;

    public void startApplication(){
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane globalPane = new BorderPane();
        scene = new Scene(globalPane, 1000, 800);

        globalPane.setCenter(setUp3DScene());

        handleKeyboard(scene);
        handleMouse(scene);

        primaryStage.setTitle("Blokus Duo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private SubScene setUp3DScene(){
        root.getChildren().add(world);
        world.setDepthTest(DepthTest.ENABLE);

        // buildScene();
        buildCamera();
        buildLight();
        buildAxes();

        world.getChildren().clear();
        grid = new Grid();
        gridRenderer = new GridRenderer(grid);
        gridRenderer.renderInto(world);
        gridRenderer.registerEvents(scene);

        List<Piece> p1Pieces = grid.getP1Pieces();
        Position pos = new Position(-grid.width * 9, - grid.width/2 * CellRenderer.cellSize);
        for (Piece p : p1Pieces){
            if (pos.y > grid.width * CellRenderer.cellSize) {
                pos.x -= 2 * CellRenderer.cellSize;
                pos.y = -grid.width/2 * CellRenderer.cellSize;
            }
            PieceRenderer pieceRenderer = new PieceRenderer(p, new Position(
                    pos.x, pos.y), 0.5, Grid.PlayerColor.ORANGE);
            pos.y += 3 * CellRenderer.cellSize;
            pieceRenderer.renderInto(world);
        }

        List<Piece> p2Pieces = grid.getP2Pieces();
        pos = new Position(grid.width * 18, -grid.width/2 * CellRenderer.cellSize);
        for (Piece p : p2Pieces){
            if (pos.y > grid.width * CellRenderer.cellSize) {
                pos.x += 2 * CellRenderer.cellSize;
                pos.y = -grid.width/2 * CellRenderer.cellSize;
            }
            PieceRenderer pieceRenderer = new PieceRenderer(p, new Position(
                    pos.x, pos.y), 0.5, Grid.PlayerColor.PURPLE);
            pos.y += 3 * CellRenderer.cellSize;
            pieceRenderer.renderInto(world);
        }

        SubScene subScene = new SubScene(root, 800, 800, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.GREY);
        subScene.setCamera(camera);

        return subScene;
    }

    private void buildCamera() {
        System.out.println("building camera");
        root.getChildren().add(cameraXForm);
        cameraXForm.getChildren().add(cameraXForm2);
        cameraXForm2.getChildren().add(cameraXForm3);
        cameraXForm3.getChildren().add(camera);
        cameraXForm3.setRotateZ(180.0);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXForm.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXForm.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
    }

    private void buildLight() {
        System.out.println("building lights");

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Point3D(0.5, -1, 0.5));

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);

        lightGroup.getChildren().addAll(ambientLight);
        lightGroup.setTranslateX(500);
        root.getChildren().addAll(lightGroup);
    }

    private void buildAxes() {
        System.out.println("buildAxes()");
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        final Box xAxis = new Box(AXIS_LENGTH, 1, 1);
        final Box yAxis = new Box(1, AXIS_LENGTH, 1);
        final Box zAxis = new Box(1, 1, AXIS_LENGTH);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        axisGroup.setVisible(false);
        root.getChildren().addAll(axisGroup);
    }

    private void handleMouse(Scene scene) {
        scene.setOnMousePressed(me -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnScroll(se -> camera.setTranslateZ(camera.getTranslateZ() + se.getDeltaY()*SCROLL_SPEED));
        scene.setOnMouseDragged(me -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            double modifier = 1.0;

            if (me.isControlDown()) {
                modifier = CONTROL_MULTIPLIER;
            }
            if (me.isShiftDown()) {
                modifier = SHIFT_MULTIPLIER;
            }
            if (me.isPrimaryButtonDown()) {
                cameraXForm.ry.setAngle(cameraXForm.ry.getAngle() - mouseDeltaX*MOUSE_SPEED*modifier*ROTATION_SPEED);
                cameraXForm.rx.setAngle(cameraXForm.rx.getAngle() + mouseDeltaY*MOUSE_SPEED*modifier*ROTATION_SPEED);
            }
            else if (me.isSecondaryButtonDown() || me.isMiddleButtonDown()) {
                cameraXForm2.t.setX(cameraXForm2.t.getX() + mouseDeltaX*MOUSE_SPEED*modifier*TRACK_SPEED);
                cameraXForm2.t.setY(cameraXForm2.t.getY() + mouseDeltaY*MOUSE_SPEED*modifier*TRACK_SPEED);
            }
        });
    }

    private void handleKeyboard(Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case Z:
                    cameraXForm2.t.setX(0.0);
                    cameraXForm2.t.setY(0.0);
                    camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
                    cameraXForm.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
                    cameraXForm.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
                    break;
                case X:
                    axisGroup.setVisible(!axisGroup.isVisible());
                    break;
            }
        });
    }
}
