/**
 * @author Fabricio Mena, Joseph Murillo, Nathalia Ocampo
 * Esta clase representa los datos utilizados en el juego "Connect Dots". Puede representar puntos,
 * líneas, colores y puntuaciones.
 */
public class GameData {
    private String type; // Tipo de datos (point, line, color, score)
    private int x; // Coordenada X de un punto
    private int y; // Coordenada Y de un punto
    private int startX; // Coordenada X de inicio de una línea
    private int startY; // Coordenada Y de inicio de una línea
    private int endX; // Coordenada X de fin de una línea
    private int endY; // Coordenada Y de fin de una línea
    private String color; // Color asociado a los datos (utilizado en líneas y colores)
    private int score; // Puntuación asociada a los datos (utilizada en puntuaciones)

    /**
     * Crea y devuelve un objeto GameData que representa un punto en el juego.
     *
     * @param x Coordenada X del punto.
     * @param y Coordenada Y del punto.
     * @return Objeto GameData que representa un punto.
     */
    public static GameData createPointData(int x, int y) {
        GameData data = new GameData();
        data.type = "point";
        data.x = x;
        data.y = y;
        return data;
    }

    /**
     * Crea y devuelve un objeto GameData que representa una línea en el juego.
     *
     * @param startX Coordenada X de inicio de la línea.
     * @param startY Coordenada Y de inicio de la línea.
     * @param endX   Coordenada X de fin de la línea.
     * @param endY   Coordenada Y de fin de la línea.
     * @param color  Color de la línea.
     * @return Objeto GameData que representa una línea.
     */
    public static GameData createLineData(int startX, int startY, int endX, int endY, String color) {
        GameData data = new GameData();
        data.type = "line";
        data.startX = startX;
        data.startY = startY;
        data.endX = endX;
        data.endY = endY;
        data.color = color;
        return data;
    }

    /**
     * Crea y devuelve un objeto GameData que representa un color en el juego.
     *
     * @param color Color a representar.
     * @return Objeto GameData que representa un color.
     */
    public static GameData createColorData(String color) {
        GameData data = new GameData();
        data.type = "color";
        data.color = color;
        return data;
    }

    /**
     * Crea y devuelve un objeto GameData que representa una puntuación en el juego.
     *
     * @param color Color asociado a la puntuación.
     * @param score Puntuación a representar.
     * @return Objeto GameData que representa una puntuación.
     */
    public static GameData createScoreData(String color, int score) {
        GameData data = new GameData();
        data.type = "score";
        data.color = color;
        data.score = score;
        return data;
    }

    @Override
    public String toString() {
        if ("point".equals(type)) {
            return "Point: (" + x + ", " + y + ")";
        } else if ("line".equals(type)) {
            return "Line: (" + startX + ", " + startY + ") to (" + endX + ", " + endY + "), Color: " + color;
        } else if ("color".equals(type)) {
            return "Color: " + color;
        } else if ("score".equals(type)) {
            return "Score: Color: " + color + ", Score: " + score;
        } else {
            return "Unknown Data Type";
        }
    }

    /**
     * Obtiene el tipo de datos.
     *
     * @return Tipo de datos (point, line, color, score).
     */
    public String getType() {
        return type;
    }

    /**
     * Obtiene la coordenada X.
     *
     * @return Coordenada X.
     */
    public int getX() {
        return x;
    }

    /**
     * Obtiene la coordenada Y.
     *
     * @return Coordenada Y.
     */
    public int getY() {
        return y;
    }

    /**
     * Obtiene la coordenada X de inicio de una línea.
     *
     * @return Coordenada X de inicio de la línea.
     */
    public int getStartX() {
        return startX;
    }

    /**
     * Obtiene la coordenada Y de inicio de una línea.
     *
     * @return Coordenada Y de inicio de la línea.
     */
    public int getStartY() {
        return startY;
    }

    /**
     * Obtiene la coordenada X de fin de una línea.
     *
     * @return Coordenada X de fin de la línea.
     */
    public int getEndX() {
        return endX;
    }

    /**
     * Obtiene la coordenada Y de fin de una línea.
     *
     * @return Coordenada Y de fin de la línea.
     */
    public int getEndY() {
        return endY;
    }

    /**
     * Obtiene el color asociado a los datos.
     *
     * @return Color asociado a los datos.
     */
    public String getColor() {
        return color;
    }

    /**
     * Establece el color asociado a los datos.
     *
     * @param color Color a establecer.
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Obtiene la puntuación asociada a los datos.
     *
     * @return Puntuación asociada a los datos.
     */
    public int getScore() {
        return score;
    }

    /**
     * Establece la puntuación asociada a los datos.
     *
     * @param score Puntuación a establecer.
     */
    public void setScore(int score) {
        this.score = score;
    }
}
