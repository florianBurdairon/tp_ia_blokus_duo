package blokus.logic;

public class Transform {
    public final Grid.Angle angle;
    public final boolean symmetry;

    public Transform(Grid.Angle angle, boolean symmetry) {
        this.angle = angle;
        this.symmetry = symmetry;
    }
}
