import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Cliente2 extends Application {
    private static final int GRID_SIZE = 4;
    private static final double POINT_RADIUS = 10.0;
    private ObjectMapper objectMapper;
    private Pane pane;
    private Circle[] circles = new Circle[GRID_SIZE * GRID_SIZE];
    private Circle selectedCircle;
    private List<Line> lines = new ArrayList<>(); // Lista para mantener las líneas dibujadas
    private Socket socket; // Agregar una instancia de Socket
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        objectMapper = new ObjectMapper();
        pane = new Pane();
        createPointGrid();
        Scene scene = new Scene(pane, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Cliente2");
        primaryStage.show();
        connectToServer();
    }

    private void createPointGrid() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Circle circle = new Circle((col + 0.5) * 600 / GRID_SIZE, (row + 0.5) * 600 / GRID_SIZE, POINT_RADIUS, Color.BLACK);
                circles[row * GRID_SIZE + col] = circle;
                pane.getChildren().add(circle);

                circle.setOnMouseClicked(event -> {
                    if (selectedCircle == null) {
                        selectedCircle = circle;
                    } else if (selectedCircle != circle) {
                        drawLine(selectedCircle, circle);
                        selectedCircle = null; // Reiniciar la selección
                    }
                });
            }
        }
    }

    private void drawLine(Circle startCircle, Circle endCircle) {
        double startX = startCircle.getCenterX();
        double startY = startCircle.getCenterY();
        double endX = endCircle.getCenterX();
        double endY = endCircle.getCenterY();

        // Verificar si los puntos son adyacentes en sentido horizontal o vertical
        double gridSize = 600.0 / GRID_SIZE;
        if ((Math.abs(startX - endX) == gridSize && Math.abs(startY - endY) < 1) ||
            (Math.abs(startY - endY) == gridSize && Math.abs(startX - endX) < 1)) {
            Line line = new Line(startX, startY, endX, endY);

            // Utilizar Platform.runLater para ejecutar esto en el hilo de JavaFX
            Platform.runLater(() -> {
                lines.add(line); // Agregar la línea a la lista
                pane.getChildren().add(line); // Agregar la línea a la escena
                enviarCoordenadas(startX, startY, endX, endY);
                connectToServer();
            });
        }
    }
    
    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 12346); // Inicializar el socket aquí
                InputStream inputStream = socket.getInputStream();
                ObjectMapper objectMapper = new ObjectMapper();
                
                while (true) {
                    while (inputStream.available() == 0) {
                        Thread.sleep(100);
                    }
                
                    // Leer las coordenadas del servidor
                    ArrayNode receivedCoordinates = objectMapper.readValue(inputStream, ArrayNode.class);
                
                    // Procesar las coordenadas y dibujar la línea utilizando tu método existente
                    double x1 = receivedCoordinates.get(0).asDouble();
                    double y1 = receivedCoordinates.get(1).asDouble();
                    double x2 = receivedCoordinates.get(2).asDouble();
                    double y2 = receivedCoordinates.get(3).asDouble();
                    
                    // Encuentra los círculos correspondientes a las coordenadas
                    Circle startCircle = findCircleByCoordinates(x1, y1);
                    Circle endCircle = findCircleByCoordinates(x2, y2);
                    
                    if (startCircle != null && endCircle != null) {
                        drawLine(startCircle, endCircle); // Utiliza tu método drawLine existente
                    }
                }
                // No cerramos el socket aquí para mantenerlo abierto.
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Circle findCircleByCoordinates(double x, double y) {
        for (Circle circle : circles) {
            if (circle.getCenterX() == x && circle.getCenterY() == y) {
                return circle;
            }
        }
        return null;
    }
    
    private void enviarCoordenadas(double startX, double startY, double endX, double endY) {
        try {
            // Crear un array JSON de coordenadas
            ArrayNode coordenadas = objectMapper.createArrayNode();
            coordenadas.add(startX);
            coordenadas.add(startY);
            coordenadas.add(endX);
            coordenadas.add(endY);
            // Enviar el array JSON al servidor utilizando el flujo de salida del socket
            objectMapper.writeValue(socket.getOutputStream(), coordenadas);
            System.out.println("Coordenadas enviadas al servidor: " + startX + ", " + startY + " y " + endX + ", " + endY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}