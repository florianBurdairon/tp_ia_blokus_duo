package blokus.render;

import blokus.logic.*;
import blokus.player.*;
import blokus.utils.Utils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;

public class BlokusScene extends Application implements Observer {

    private enum PlayerTypes {
        Human {
            @Override
            public AbstractPlayer getPlayer() {
                return new Player();
            }
        },
        MinMax {
            @Override
            public AbstractPlayer getPlayer() {
                return new MinMaxPlayer();
            }
        },
        AlphaBeta {
            @Override
            public AbstractPlayer getPlayer() {
                return new AlphaBetaPlayer();
            }
        },
        MCTS {
            @Override
            public AbstractPlayer getPlayer() {
                return new MCTSPlayer();
            }
        };

        public abstract AbstractPlayer getPlayer();
    }

    private Scene scene;
    private Grid grid;
    private GridRenderer gridRenderer;

    private boolean canPlay = false;
    private boolean isHoverGrid = false;

    private final Group root = new Group();
    public static final Group tempGroup = new Group();
    public final Group pieces1Group = new Group();
    public final Group pieces2Group = new Group();
    final XForm lightGroup = new XForm();
    final XForm axisGroup = new XForm();

    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final XForm cameraXForm = new XForm();
    final XForm cameraXForm2 = new XForm();
    final XForm cameraXForm3 = new XForm();
    final XForm world = new XForm();

    private Thread gameThread;
    private Text orangeScoreText;
    private Text purpleScoreText;
    private Text winnerText;

    private static final double CAMERA_INITIAL_DISTANCE = -1200;
    private static final double CAMERA_INITIAL_X_ANGLE = 35.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 135.0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
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

    public void setGrid(Grid grid) {
        this.grid = grid;
        if (this.grid.getP1() instanceof Player p) {
            p.askForPlay = pieces -> canPlay = true;
        }

        if (this.grid.getP2() instanceof Player p) {
            p.askForPlay = pieces -> canPlay = true;
        }
    }

    public void startApplication(){
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane globalPane = new BorderPane();
        scene = new Scene(globalPane, 1200, 800);

        globalPane.setRight(setUpSettingsPane(primaryStage));
        globalPane.setCenter(setUp3DScene());

        handleKeyboard(scene);
        handleMouse(scene);

        primaryStage.setTitle("Blokus Duo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setUpGame(AbstractPlayer p1, AbstractPlayer p2)
    {
        Grid grid = new Grid(p1, p2);

        grid.addListener(this);

        setGrid(grid);
        renderGridInWorld();
    }

    private SubScene setUp3DScene(){
        root.getChildren().addAll(world, tempGroup, pieces1Group, pieces2Group);
        world.setDepthTest(DepthTest.ENABLE);

        // buildScene();
        buildCamera();
        buildLight();
        buildAxes();

        SubScene subScene = new SubScene(root, 1000, 800, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.SKYBLUE);
        subScene.setCamera(camera);

        return subScene;
    }

    private void renderGridInWorld() {
        world.getChildren().clear();

        gridRenderer = new GridRenderer(grid);
        gridRenderer.renderInto(world);
        gridRenderer.world.setOnMouseEntered(e -> isHoverGrid = true);
        gridRenderer.registerEvents(scene);

        updatePieces();

        final PhongMaterial tableMaterial = new PhongMaterial();
        tableMaterial.setDiffuseColor(Color.DARKGREY);

        TriangleMesh tableMesh = new TriangleMesh();
        tableMesh.getPoints().addAll(
                0, 0, 0,
                1, 0, 0,
                0, 0, 1,
                1, 0, 1);
        tableMesh.getTexCoords().addAll(
                0.5f, 0.5f
        );
        tableMesh.getFaces().addAll(
                0, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0 // Bottom
        );
        MeshView tableMeshView = new MeshView(tableMesh);
        tableMeshView.setCullFace(CullFace.NONE);
        tableMeshView.setScaleX(2000);
        tableMeshView.setScaleZ(2000);
        tableMeshView.setTranslateY(-4f);
        tableMeshView.setMaterial(tableMaterial);
        tableMeshView.setOnMouseEntered(e -> isHoverGrid = false);

        world.getChildren().add(tableMeshView);
        world.requestFocus();
    }

    private Pane setUpSettingsPane(Stage stage)
    {
        VBox pane = new VBox();
        pane.setPrefSize(200, 1000);
        pane.setSpacing(15);
        pane.setAlignment(Pos.CENTER);

        ObservableList<PlayerTypes> options = FXCollections.observableArrayList(
                PlayerTypes.Human,
                PlayerTypes.MinMax,
                PlayerTypes.AlphaBeta,
                PlayerTypes.MCTS
        );
        ComboBox<PlayerTypes> cbp1 = new ComboBox<>(options);
        cbp1.setValue(options.getFirst());
        ComboBox<PlayerTypes> cbp2 = new ComboBox<>(options);
        cbp2.setValue(options.getFirst());
        pane.getChildren().addAll(cbp1, cbp2);

        Button startGameButton = new Button();
        startGameButton.setText("Start Game");
        startGameButton.setOnMouseClicked(e -> {
            if (gameThread != null) {
                gameThread.interrupt();
            }
            setUpGame(cbp1.getValue().getPlayer(), cbp2.getValue().getPlayer());
            update();
            gameThread = grid;
            gameThread.start();
            stage.setOnCloseRequest(we -> {
                if (gameThread.isAlive()) {
                    System.err.println("GAME THREAD INTERRUPTED");
                    gameThread.interrupt();
                }
            });
        });
        pane.getChildren().add(startGameButton);

        winnerText = new Text("");
        orangeScoreText = new Text("");
        purpleScoreText = new Text("");

        pane.getChildren().addAll(winnerText, orangeScoreText, purpleScoreText);

        return pane;
    }

    private void buildCamera() {
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
        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Point3D(0.5, -1, 0.5));

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);

        lightGroup.getChildren().addAll(ambientLight);
        lightGroup.setTranslateX(500);
        root.getChildren().addAll(lightGroup);
    }

    private void buildAxes() {
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

    private void updatePieces()
    {
        pieces1Group.getChildren().clear();
        pieces2Group.getChildren().clear();

        List<Piece> p1Pieces = grid.currentP1Pieces;
        Position pos = new Position(-Grid.width * 9, -Grid.width /2 * CellRenderer.cellSize);
        for (Piece p : p1Pieces){
            if (pos.y > Grid.width * CellRenderer.cellSize) {
                pos.x -= 2 * CellRenderer.cellSize;
                pos.y = -Grid.width/2 * CellRenderer.cellSize;
            }
            PieceRenderer pieceRenderer = new PieceRenderer(p, new Position(
                    pos.x, pos.y), 0.5, Grid.PlayerColor.ORANGE);
            pos.y += 3 * CellRenderer.cellSize;
            pieceRenderer.renderInto(pieces1Group);
            pieceRenderer.registerEvents(scene);
            pieceRenderer.world.setOnMouseClicked(event -> {
                if (grid.getCurrentPlayer() == grid.getP1() && canPlay)
                    PlayerInput.getInstance().selectPiece(p, Grid.PlayerColor.ORANGE);
            });
        }

        List<Piece> p2Pieces = grid.currentP2Pieces;
        pos = new Position(Grid.width * 18, -Grid.width/2 * CellRenderer.cellSize);
        for (Piece p : p2Pieces){
            if (pos.y > Grid.width * CellRenderer.cellSize) {
                pos.x += 2 * CellRenderer.cellSize;
                pos.y = -Grid.width/2 * CellRenderer.cellSize;
            }
            PieceRenderer pieceRenderer = new PieceRenderer(p, new Position(
                    pos.x, pos.y), 0.5, Grid.PlayerColor.PURPLE);
            pos.y += 3 * CellRenderer.cellSize;
            pieceRenderer.renderInto(pieces2Group);
            pieceRenderer.registerEvents(scene);
            pieceRenderer.world.setOnMouseClicked(event -> {
                if (grid.getCurrentPlayer() == grid.getP2() && canPlay)
                    PlayerInput.getInstance().selectPiece(p, Grid.PlayerColor.PURPLE);
            });
        }
    }

    private void handleMouse(Scene scene) {
        scene.setOnMouseClicked(me -> {
            if (!me.isShiftDown()) {
                if (canPlay && isHoverGrid && PlayerInput.getInstance().getSelectedPiece() != null) {
                    if (me.getButton().equals(MouseButton.PRIMARY)) {
                        PieceRenderer pieceRenderer = PlayerInput.getInstance().getSelectedPiece();
                        Piece piecePlayed = pieceRenderer.getPiece();

                        Transform transform = new Transform(PlayerInput.getInstance().getRotation(), PlayerInput.getInstance().hasSymmetry());
                        Turn turn = new Turn(pieceRenderer.getPos(), piecePlayed, transform);
                        if (grid.placePiece(turn)) {
                            System.out.println("Player played at " + pieceRenderer.getPos());
                            updatePieces();
                            canPlay = false;
                            for (Position pieceCase : Utils.transform(
                                    piecePlayed.getCases(),
                                    PlayerInput.getInstance().getRotation(),
                                    PlayerInput.getInstance().hasSymmetry())) {
                                gridRenderer.updatePos(pieceRenderer.getPos().add(pieceCase));
                            }
                        } else {
                            System.out.println("Player can not play at " + pieceRenderer.getPos() + ", reselect a piece");
                        }
                        PlayerInput.getInstance().unselectPiece();
                    }
                    else if (me.getButton().equals(MouseButton.SECONDARY)) {
                        PlayerInput.getInstance().toggleSymmetry();
                    } else {
                        System.out.println("Taken click into account but couldn't process it: " + me.getButton());
                    }
                }
            }
        });
        scene.setOnMousePressed(me -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnScroll(se -> {
            if (se.isShiftDown())
            {
                camera.setTranslateZ(camera.getTranslateZ() + se.getDeltaY()*SCROLL_SPEED);
            }
            else {
                PlayerInput.getInstance().addRotation(se.getDeltaY() > 0 ? Grid.Angle.DEG_90 : Grid.Angle.DEG_270);
            }
        });
        scene.setOnMouseDragged(me -> {
            if (me.isShiftDown()) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX);
                mouseDeltaY = (mousePosY - mouseOldY);

                if (me.isPrimaryButtonDown()) {
                    cameraXForm.ry.setAngle(cameraXForm.ry.getAngle() - mouseDeltaX * MOUSE_SPEED * ROTATION_SPEED);
                    cameraXForm.rx.setAngle(cameraXForm.rx.getAngle() + mouseDeltaY * MOUSE_SPEED * ROTATION_SPEED);
                } else if (me.isSecondaryButtonDown() || me.isMiddleButtonDown()) {
                    cameraXForm2.t.setX(cameraXForm2.t.getX() + mouseDeltaX * MOUSE_SPEED * TRACK_SPEED);
                    cameraXForm2.t.setY(cameraXForm2.t.getY() + mouseDeltaY * MOUSE_SPEED * TRACK_SPEED);
                }
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
                case UP:
                    PlayerInput.getInstance().setMousePos(PlayerInput.getInstance().getMousePos().add(new Position(0, -1)));
                    break;
                case DOWN:
                    PlayerInput.getInstance().setMousePos(PlayerInput.getInstance().getMousePos().add(new Position(0, 1)));
                    break;
                case LEFT:
                    PlayerInput.getInstance().setMousePos(PlayerInput.getInstance().getMousePos().add(new Position(-1, 0)));
                    break;
                case RIGHT:
                    PlayerInput.getInstance().setMousePos(PlayerInput.getInstance().getMousePos().add(new Position(1, 0)));
                    break;
            }
        });
    }


    @Override
    public void update() {
        Platform.runLater(() -> {
            gridRenderer.updateAll();
            updatePieces();
            orangeScoreText.setText("Orange score: " + grid.getTotalPlayerScore(Grid.PlayerColor.ORANGE));
            purpleScoreText.setText("Purple score: " + grid.getTotalPlayerScore(Grid.PlayerColor.PURPLE));
            if (grid.isGameFinished()) {
                winnerText.setText("The winner is player " + grid.getWinner());
            }
        });
    }
}
