package blokus.render;

import javafx.scene.Group;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

public abstract class ObjectRenderer extends Group {
    protected final XForm world = new XForm();
    protected final List<ObjectRenderer> objects = new ArrayList<>();
    private Group parent;

    protected void addKeyboardEvents(Scene scene) {}
    protected void addMouseEvents(Scene scene) {}

    abstract void buildObject();

    public void renderInto(Group parent)
    {
        this.parent = parent;
        buildObject();
        this.parent.getChildren().add(world);
    }

    public void registerEvents(Scene scene) {
        for(ObjectRenderer renderer : objects) {
            renderer.registerEvents(scene);
        }

        addKeyboardEvents(scene);
        addMouseEvents(scene);
    }

    public void reset(){
        world.getChildren().clear();
        buildObject();
        //this.parent.getChildren().add(world);
    }
}
