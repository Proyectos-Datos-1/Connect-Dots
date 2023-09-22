import com.google.gson.Gson;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.scene.shape.Circle;
import javafx.scene.input.KeyCode; // Importar KeyCode para el manejo de teclas

import com.fazecast.jSerialComm.*;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Client extends Application {
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
    private int playerRow = 0;
    private int playerCol = 0;
    private Scene scene; // Declara la variable scene como una variable de instancia
    private SerialPort serialPort; // Variable miembro para la comunicación serial


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connect Dots Game");

        backgroundPane = new Pane(); // Crear el nuevo Pane
        backgroundPane.setPrefSize(WIDTH, HEIGHT); // Establecer el tamaño
        scene = new Scene(backgroundPane, WIDTH, HEIGHT);


        // Agregar una etiqueta para mostrar el score del cliente
        Label scoreLabel = new Label("Score: 0");
        scoreLabel.setLayoutX(10);
        scoreLabel.setLayoutY(10);
        backgroundPane.getChildren().add(scoreLabel);

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
                        } else if ("score".equals(receivedData.getType())) {
                            // Actualizar el score del cliente
                            if (receivedData.getColor().equals(clientColor)) {
                                int score = receivedData.getScore();
                                Platform.runLater(() -> {
                                    scoreLabel.setText("Score: " + score);
                                });
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Configurar y abrir el puerto serial
        serialPort = SerialPort.getCommPort("COM3"); // Reemplaza "COMx" con el nombre de tu puerto serial
        serialPort.openPort();

        // Configurar el lector de datos serial en un hilo
        Thread serialReaderThread = new Thread(() -> {
            while (true) {
                byte[] data = new byte[1];
                int bytesRead = serialPort.readBytes(data, data.length);
                if (bytesRead > 0) {
                    byte receivedByte = data[0];
                    // Interpreta el byte recibido y realiza acciones en la aplicación
                    handleSerialData(receivedByte);
                }
            }
        });

        serialReaderThread.start();
        
        for (int row = 0; row < GRID_SIZE; row++) {
            List<Circle> rowList = new ArrayList<>();
            for (int col = 0; col < GRID_SIZE; col++) {
                Circle circle = new Circle(POINT_RADIUS);
                circle.setFill(Color.BLACK);
                circle.setCenterX((col + 1) * 100 + 50); // Posición X del punto
                circle.setCenterY((row + 1) * 100 + 50); // Posición Y del punto


                scene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.W && playerRow > 0) {
                        playerRow--;
                        updatePlayerPosition();
                    } else if (event.getCode() == KeyCode.S && playerRow < GRID_SIZE - 1) {
                        playerRow++;
                        updatePlayerPosition();
                    } else if (event.getCode() == KeyCode.A && playerCol > 0) {
                        playerCol--;
                        updatePlayerPosition();
                    } else if (event.getCode() == KeyCode.D && playerCol < GRID_SIZE - 1) {
                        playerCol++;
                        updatePlayerPosition();
                    } else if (event.getCode() == KeyCode.SPACE) {
                        selectPoint(playerCol, playerRow);
                    }
                });

                rowList.add(circle);
                backgroundPane.getChildren().add(circle); // Agregar el círculo al nuevo Pane
            }
            grid.add(rowList);
        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleSerialData(byte receivedByte) {
        Platform.runLater(() -> {
            switch (receivedByte) {
                case 0: // Mover hacia arriba
                    if (playerRow > 0) {
                        playerRow--;
                        updatePlayerPosition();
                    }
                    break;
                case 1: // Mover hacia abajo
                    if (playerRow < GRID_SIZE - 1) {
                        playerRow++;
                        updatePlayerPosition();
                    }
                    break;
                case 2: // Mover hacia la izquierda
                    if (playerCol > 0) {
                        playerCol--;
                        updatePlayerPosition();
                    }
                    break;
                case 3: // Mover hacia la derecha
                    if (playerCol < GRID_SIZE - 1) {
                        playerCol++;
                        updatePlayerPosition();
                    }
                    break;
                case 4: // Seleccionar un punto
                    selectPoint(playerCol, playerRow);
                    break;
                default:
                    // Acción no reconocida
                    break;
            }
        });
    }
    
    
    private void updatePlayerPosition() {
        // Actualiza la posición visual del jugador en la cuadrícula
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Circle circle = grid.get(row).get(col);
                if (row == playerRow && col == playerCol) {
                    // Cambia el color del punto en la ubicación actual
                    circle.setFill(Color.web(clientColor));
                } else {
                    // Restablece el color de los demás puntos a negro
                    circle.setFill(Color.BLACK);
                }
            }
        }
    }

    private void selectPoint(int col, int row) {
        // Maneja la selección de un punto
        if (firstPoint == null) {
            firstPoint = GameData.createPointData(col + 1, row + 1);
        } else {
            GameData secondPoint = GameData.createPointData(col + 1, row + 1);
            sendGameDataToServer(firstPoint, secondPoint);
            firstPoint = null;
        }
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
        if (serialPort != null) {
            serialPort.closePort();
        }
    }
}
