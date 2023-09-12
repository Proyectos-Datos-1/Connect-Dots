import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ConnectDots extends Application {
    private static final int GRID_SIZE = 200; // Tamaño de la cuadrícula
    private static final int ROWS = 4; // Número de filas
    private static final int COLS = 4; // Número de columnas
    private static final int WIDTH = COLS * GRID_SIZE;
    private static final int HEIGHT = ROWS * GRID_SIZE;
    private boolean[][] points = new boolean[COLS][ROWS];
    private int[][] horizontalLines = new int[COLS][ROWS - 1];
    private int[][] verticalLines = new int[COLS - 1][ROWS];
    private int startX = -1, startY = -1, endX = -1, endY = -1;
    private boolean selectingStartPoint = true;
    private boolean playerRedTurn = true;
    private int playerRedScore = 0;
    private int playerBlueScore = 0;
    private Pane root;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connect Dots Game");
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleMouseClick);
        root = new Pane(canvas);
        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.show();
        initializeGrid();
        drawGrid(gc);
        drawScores(gc);
    }

    private void initializeGrid() {
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                points[x][y] = false;
            }
        }
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS - 1; y++) {
                horizontalLines[x][y] = 0;
            }
        }
        for (int x = 0; x < COLS - 1; x++) {
            for (int y = 0; y < ROWS; y++) {
                verticalLines[x][y] = 0;
            }
        }
    }

    private void handleMouseClick(MouseEvent event) {
        int clickedX = (int) (event.getX() / GRID_SIZE);
        int clickedY = (int) (event.getY() / GRID_SIZE);
        if (clickedX >= 0 && clickedX < COLS && clickedY >= 0 && clickedY < ROWS) {
            if (!points[clickedX][clickedY]) {
                if (selectingStartPoint) {
                    startX = clickedX;
                    startY = clickedY;
                    selectingStartPoint = false;
                } else {
                    endX = clickedX;
                    endY = clickedY;
                    if ((startX == endX && startY != endY) || (startY == endY && startX != endX)) {
                        if (isValidMove()) {
                            drawLineBetweenPoints();
                            if (checkForSquare()) {
                                if (playerRedTurn) {
                                    playerRedScore++;
                                } else {
                                    playerBlueScore++;
                                }
                                drawScores(((Canvas) root.getChildren().get(0)).getGraphicsContext2D());
                            } else {
                                playerRedTurn = !playerRedTurn;
                            }
                        }
                    }
                    selectingStartPoint = true;
                }
            }
        }
    }

    private boolean isValidMove() {
        if (startX == endX && startY != endY) {
            if (horizontalLines[startX][Math.min(startY, endY)] == 0) {
                horizontalLines[startX][Math.min(startY, endY)] = playerRedTurn ? 1 : 2;
                return true;
            }
        } else if (startY == endY && startX != endX) {
            if (verticalLines[Math.min(startX, endX)][startY] == 0) {
                verticalLines[Math.min(startX, endX)][startY] = playerRedTurn ? 1 : 2;
                return true;
            }
        }
        return false;
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                double centerX = x * GRID_SIZE + GRID_SIZE / 2;
                double centerY = y * GRID_SIZE + GRID_SIZE / 2;
                gc.fillOval(centerX - 2, centerY - 2, 4, 4);
            }
        }
    }

    private void drawLineBetweenPoints() {
        if (startX != -1 && startY != -1 && endX != -1 && endY != -1) {
            GraphicsContext gc = ((Canvas) root.getChildren().get(0)).getGraphicsContext2D();
            double startXCoord = startX * GRID_SIZE + GRID_SIZE / 2;
            double startYCoord = startY * GRID_SIZE + GRID_SIZE / 2;
            double endXCoord = endX * GRID_SIZE + GRID_SIZE / 2;
            double endYCoord = endY * GRID_SIZE + GRID_SIZE / 2;
            gc.setStroke(playerRedTurn ? Color.RED : Color.BLUE);
            gc.setLineWidth(2);
            gc.strokeLine(startXCoord, startYCoord, endXCoord, endYCoord);
        }
    }

    private void drawScores(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, WIDTH, GRID_SIZE);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(20));
        gc.fillText("Puntaje Rojo: " + playerRedScore, 20, 30);
        gc.fillText("Puntaje Azul: " + playerBlueScore, 20, 60);
    }

    private boolean checkForSquare() {
        boolean squareFormed = false;

        // Recorre todas las celdas de la cuadrícula
        for (int x = 0; x < COLS - 1; x++) {
            for (int y = 0; y < ROWS - 1; y++) {
                boolean validSquare = true;
                for (int i = x; i < x + 1; i++) {
                    for (int j = y; j < y + 1; j++) {
                        if (horizontalLines[i][j] == 0 || verticalLines[i][j] == 0) {
                            validSquare = false;
                            break;
                        }
                    }
                    if (!validSquare) {
                        break;
                    }
                }
                if (validSquare) {
                    squareFormed = true;
                    for (int i = x; i < x + 1; i++) {
                        for (int j = y; j < y + 1; j++) {
                            horizontalLines[i][j] = 0;
                            verticalLines[i][j] = 0;
                        }
                    }
                }
            }
        }

        return squareFormed;
    }
}