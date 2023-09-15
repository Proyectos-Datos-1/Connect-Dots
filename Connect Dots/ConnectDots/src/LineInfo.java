public class LineInfo {
    private Coordinates startPoint;
    private Coordinates endPoint;

    public LineInfo(Coordinates startPoint, Coordinates endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public Coordinates getStartPoint() {
        return startPoint;
    }

    public Coordinates getEndPoint() {
        return endPoint;
    }
}
