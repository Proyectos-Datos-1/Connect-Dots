import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ConnectDotsGame extends Application {

    private static final int ROWS = 4;
    private static final int COLUMNS = 4;
    private static final double DOT_RADIUS = 10.0;
    private static final double SHAPE_SIZE = 40.0;

    private boolean[][] connectedDots = new boolean[ROWS][COLUMNS];
    private double[][] dotX = new double[ROWS][COLUMNS];
    private double[][] dotY = new double[ROWS][COLUMNS];

    private int currentPlayer = 1;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Canvas canvas = new Canvas(400, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            double x = e.getX();
            double y = e.getY();
            int row = (int) (y / (SHAPE_SIZE + DOT_RADIUS * 2));
            int col = (int) (x / (SHAPE_SIZE + DOT_RADIUS * 2));

            if (row >= 0 && row < ROWS && col >= 0 && col < COLUMNS && !connectedDots[row][col]) {
                dotX[row][col] = col * (SHAPE_SIZE + DOT_RADIUS * 2) + SHAPE_SIZE / 2;
                dotY[row][col] = row * (SHAPE_SIZE + DOT_RADIUS * 2) + SHAPE_SIZE / 2;
                connectedDots[row][col] = true;
                drawSquare(gc, dotX[row][col], dotY[row][col]);
                checkForWin(row, col);
                currentPlayer = (currentPlayer == 1) ? 2 : 1;
            }
        });

        root.setCenter(canvas);
        Scene scene = new Scene(root, 400, 400);

        primaryStage.setTitle("Connect the Dots Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawSquare(GraphicsContext gc, double centerX, double centerY) {
        gc.setFill(currentPlayer == 1 ? Color.RED : Color.BLUE);
        gc.fillRect(centerX - SHAPE_SIZE / 2, centerY - SHAPE_SIZE / 2, SHAPE_SIZE, SHAPE_SIZE);
    }

    private void checkForWin(int row, int col) {
        // Verificar si se ha completado un cuadrado en la fila superior
        if (row > 0 && connectedDots[row - 1][col] && connectedDots[row - 1][col + 1] && connectedDots[row][col + 1]) {
            System.out.println("Jugador " + currentPlayer + " ha completado un cuadrado en la fila superior.");
            // Puedes agregar aquí la lógica para otorgar un punto al jugador o realizar alguna otra acción.
        }
    
        // Verificar si se ha completado un cuadrado en la fila inferior
        if (row < ROWS - 1 && connectedDots[row][col] && connectedDots[row][col + 1] && connectedDots[row + 1][col + 1]) {
            System.out.println("Jugador " + currentPlayer + " ha completado un cuadrado en la fila inferior.");
            // Puedes agregar aquí la lógica para otorgar un punto al jugador o realizar alguna otra acción.
        }
        
        // Verificar si se ha completado un cuadrado en la columna izquierda
        if (col > 0 && connectedDots[row][col - 1] && connectedDots[row + 1][col - 1] && connectedDots[row + 1][col]) {
            System.out.println("Jugador " + currentPlayer + " ha completado un cuadrado en la columna izquierda.");
            // Puedes agregar aquí la lógica para otorgar un punto al jugador o realizar alguna otra acción.
        }
        
        // Verificar si se ha completado un cuadrado en la columna derecha
        if (col < COLUMNS - 1 && connectedDots[row][col] && connectedDots[row + 1][col] && connectedDots[row + 1][col + 1]) {
            System.out.println("Jugador " + currentPlayer + " ha completado un cuadrado en la columna derecha.");
            // Puedes agregar aquí la lógica para otorgar un punto al jugador o realizar alguna otra acción.
        }
    }    
}
