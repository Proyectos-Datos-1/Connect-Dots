import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.scene.shape.Circle;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Board extends Application {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int GRID_SIZE = 4;
    private static final int POINT_RADIUS = 10;
    private Socket socket;
    private PrintWriter out;
    private GameData firstPoint = null;
    private Pane backgroundPane; // Nuevo Pane para los puntos y las líneas
    private BufferedReader in;
    private String clientColor;
    private List<List<Circle>> grid = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connect Dots Game");

        backgroundPane = new Pane(); // Crear el nuevo Pane
        backgroundPane.setPrefSize(WIDTH, HEIGHT); // Establecer el tamaño

        String serverAddress = "localhost"; // Cambia esto a la dirección IP del servidor si es necesario
        int serverPort = 12345; // Puerto en el que el servidor está escuchando

        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Agregar esta línea
            // Agregar esto después de crear el socket y el PrintWriter en el método start()
            Thread receiveThread = new Thread(() -> {
                try {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        Gson gson = new Gson();
                        GameData receivedData = gson.fromJson(inputLine, GameData.class);
                        if ("line".equals(receivedData.getType())) {
                            drawLineFromReceivedData(receivedData);
                        } else if ("color".equals(receivedData.getType())) {
                            clientColor = receivedData.getColor(); // Establecer el color del cliente
                        }
                        System.out.println("Coordenadas recibidas del servidor: " + inputLine);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int row = 0; row < GRID_SIZE; row++) {
            List<Circle> rowList = new ArrayList<>();
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
                        if (firstPoint == null) {
                            firstPoint = GameData.createPointData(finalCol + 1, finalRow + 1);
                        } else {
                            GameData secondPoint = GameData.createPointData(finalCol + 1, finalRow + 1);
                            sendGameDataToServer(firstPoint, secondPoint);
                            firstPoint = null;
                        }
                    }
                });

                rowList.add(circle);
                backgroundPane.getChildren().add(circle); // Agregar el círculo al nuevo Pane
            }
            grid.add(rowList);
        }

        Scene scene = new Scene(backgroundPane, WIDTH, HEIGHT); // Utilizar el nuevo Pane como contenido
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void sendGameDataToServer(GameData gameData1, GameData gameData2) {
        try {
            Gson gson = new Gson();

            // Crear un objeto GameData que represente una línea
            GameData lineData = GameData.createLineData(
                    gameData1.getX(), gameData1.getY(),
                    gameData2.getX(), gameData2.getY(),
                    "clientColor"
            );

            String jsonLineData = gson.toJson(lineData);

            out.println(jsonLineData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawLineFromReceivedData(GameData receivedData) {
        int startX = receivedData.getStartX() * 100 + 50;
        int startY = receivedData.getStartY() * 100 + 50;
        int endX = receivedData.getEndX() * 100 + 50;
        int endY = receivedData.getEndY() * 100 + 50;

        Line line = new Line(startX, startY, endX, endY);
        String color = receivedData.getColor(); // Obtener el color de los datos
        line.setStroke(Color.web(color)); // Utilizar el color especificado
        line.setStrokeWidth(2.0);

        Platform.runLater(() -> {
            backgroundPane.getChildren().add(0, line);
        });
    }

    public void stop() throws Exception {
        // Cierra la conexión con el servidor al detener la aplicación
        if (socket != null) {
            socket.close();
        }
    }
}
