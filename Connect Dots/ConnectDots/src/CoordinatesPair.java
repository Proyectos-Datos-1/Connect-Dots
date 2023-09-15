public class CoordinatesPair {
    private Coordinates firstPoint;
    private Coordinates secondPoint;

    public CoordinatesPair(Coordinates firstPoint, Coordinates secondPoint) {
        this.firstPoint = firstPoint;
        this.secondPoint = secondPoint;
    }

    public Coordinates getFirstPoint() {
        return firstPoint;
    }

    public Coordinates getSecondPoint() {
        return secondPoint;
    }
}
