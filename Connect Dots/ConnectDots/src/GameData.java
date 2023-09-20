public class GameData {
    private String type;
    private int x;
    private int y;
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private String color; // Nueva propiedad de color
    private int score; // Nueva propiedad de puntuación


    public static GameData createPointData(int x, int y) {
        GameData data = new GameData();
        data.type = "point";
        data.x = x;
        data.y = y;
        return data;
    }

    public static GameData createLineData(int startX, int startY, int endX, int endY, String color) {
        GameData data = new GameData();
        data.type = "line";
        data.startX = startX;
        data.startY = startY;
        data.endX = endX;
        data.endY = endY;
        data.color = color; // Establece el color
        return data;
    }
    
    public static GameData createColorData(String color) {
        GameData data = new GameData();
        data.type = "color";
        data.color = color;
        return data;
    }

    public static GameData createScoreData(String color, int score) {
        GameData data = new GameData();
        data.type = "score";
        data.color = color;
        data.score = score;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
