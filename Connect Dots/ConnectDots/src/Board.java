import com.google.gson.Gson;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.net.Socket;

public class Board extends Application {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int GRID_SIZE = 4;
    private static final int POINT_RADIUS = 10;
    private Socket socket;
    private PrintWriter out;
    private Coordinates firstPoint = null;
    private Pane backgroundPane; // Nuevo Pane para los puntos y las líneas

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connect Dots Game");

        backgroundPane = new Pane(); // Crear el nuevo Pane
        backgroundPane.setPrefSize(WIDTH, HEIGHT); // Establecer el tamaño

        String serverAddress = "localhost"; // Cambia esto a la dirección IP del servidor si es necesario
        int serverPort = 12345; // Puerto en el que el servidor está escuchando

        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Circle circle = new Circle(POINT_RADIUS);
                circle.setFill(Color.BLACK);
                circle.setCenterX((col + 1) * 100 + 50); // Posición X del punto
                circle.setCenterY((row + 1) * 100 + 50); // Posición Y del punto

                final int finalRow = row;
                final int finalCol = col;
                circle.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        Coordinates coordinates = new Coordinates(finalCol + 1, finalRow + 1);

                        if (firstPoint == null) {
                            firstPoint = coordinates;
                        } else {
                            sendCoordinatesToServer(firstPoint, coordinates);
                            drawLineBetweenPoints(firstPoint, coordinates); // Dibujar la línea en el cliente
                            firstPoint = null;
                        }
                    }
                });

                backgroundPane.getChildren().add(circle); // Agregar el círculo al nuevo Pane
            }
        }

        Scene scene = new Scene(backgroundPane, WIDTH, HEIGHT); // Utilizar el nuevo Pane como contenido
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void sendCoordinatesToServer(Coordinates coordinates1, Coordinates coordinates2) {
        try {
            Gson gson = new Gson();
            String jsonCoordinates = gson.toJson(new CoordinatesPair(coordinates1, coordinates2));
            out.println(jsonCoordinates);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawLineBetweenPoints(Coordinates start, Coordinates end) {
        if (areAdjacent(start, end) && isVerticalOrHorizontal(start, end)) {
            Line line = new Line(
                    start.getX() * 100 + 50,
                    start.getY() * 100 + 50,
                    end.getX() * 100 + 50,
                    end.getY() * 100 + 50
            );
    
            line.setStroke(Color.BLUE);
            line.setStrokeWidth(2.0);
    
            // Agrega la línea en la posición 0 para que esté en la capa inferior
            backgroundPane.getChildren().add(0, line);
        }
    }

    // Función para verificar si dos puntos son adyacentes
    private boolean areAdjacent(Coordinates point1, Coordinates point2) {
        int dx = Math.abs(point1.getX() - point2.getX());
        int dy = Math.abs(point1.getY() - point2.getY());

        // Dos puntos son adyacentes si su diferencia en coordenadas X o Y es igual a 1, pero no ambos
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    // Función para verificar si la línea es vertical u horizontal
    private boolean isVerticalOrHorizontal(Coordinates start, Coordinates end) {
        int dx = Math.abs(start.getX() - end.getX());
        int dy = Math.abs(start.getY() - end.getY());

        // La línea es vertical u horizontal si una de las diferencias es 0 y la otra es 1
        return (dx == 0 && dy == 1) || (dx == 1 && dy == 0);
    }

    @Override
    public void stop() throws Exception {
        // Cierra la conexión con el servidor al detener la aplicación
        if (socket != null) {
            socket.close();
        }
    }
}
