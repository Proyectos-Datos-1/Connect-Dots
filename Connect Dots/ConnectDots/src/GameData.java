public class GameData {
    private String type;
    private int x;
    private int y;
    private int startX;
    private int startY;
    private int endX;
    private int endY;

    public static GameData createPointData(int x, int y) {
        GameData data = new GameData();
        data.type = "point";
        data.x = x;
        data.y = y;
        return data;
    }

    public static GameData createLineData(int startX, int startY, int endX, int endY) {
        GameData data = new GameData();
        data.type = "line";
        data.startX = startX;
        data.startY = startY;
        data.endX = endX;
        data.endY = endY;
        return data;
    }

    public String getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }
}
