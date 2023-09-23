/**
 * Esta clase implementa un cliente para el juego "Connect Dots". El cliente se comunica con un servidor
 * para dibujar líneas entre puntos en una cuadrícula y registrar puntajes.
 * 
 * @author Fabricio Mena, Joseph Murillo, Nathalia Ocampo
 */
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
import javafx.scene.input.KeyCode;
import com.fazecast.jSerialComm.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client extends Application {

    // Tamaño de la ventana del juego
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;

    // Tamaño de la cuadrícula y radio de los puntos
    private static final int GRID_SIZE = 4;
    private static final int POINT_RADIUS = 10;

    private Socket socket; // Socket para la comunicación con el servidor
    private PrintWriter out; // Escritor para enviar datos al servidor
    private GameData firstPoint = null; // Primer punto seleccionado por el cliente
    private Pane backgroundPane; // Pane que contiene puntos y líneas
    private BufferedReader in; // Lector para recibir datos del servidor
    private String clientColor; // Color asignado al cliente
    private List<List<Circle>> grid = new ArrayList<>(); // Representación de la cuadrícula de puntos
    private int playerRow = 0; // Fila actual del jugador en la cuadrícula
    private int playerCol = 0; // Columna actual del jugador en la cuadrícula
    private Scene scene; // Escena del juego
    private SerialPort serialPort; // Puerto serial para la comunicación

    /**
     * Método principal de la aplicación.
     * 
     * @param args Los argumentos de la línea de comandos (no se utilizan en este caso).
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Inicializa la interfaz gráfica y establece la conexión con el servidor.
     * 
     * @param primaryStage El escenario principal de la aplicación.
     */
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

        String serverAddress = "localhost"; // Dirección del servidor (cambia si es necesario)
        int serverPort = 12345; // Puerto del servidor

        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Hilo para recibir datos del servidor
            Thread receiveThread = new Thread(() -> {
                try {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        Gson gson = new Gson();
                        GameData receivedData = gson.fromJson(inputLine, GameData.class);
                        if ("line".equals(receivedData.getType())) { // Se llama al metodo para dibujar la linea de las coordenadas recividas
                            drawLineFromReceivedData(receivedData);
                        } else if ("color".equals(receivedData.getType())) { // Se verifica el color recibido para dibujar la linea
                            clientColor = receivedData.getColor();
                        } else if ("score".equals(receivedData.getType())) {
                            if (receivedData.getColor().equals(clientColor)) { // Se selecciona a que cliente sumarle puntos segun su color
                                int score = receivedData.getScore();
                                Platform.runLater(() -> {
                                    scoreLabel.setText("Score: " + score); // Se cambia el valor de score de la etiqueta
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

        // Hilo para leer datos del puerto serial
        Thread serialReaderThread = new Thread(() -> {
            while (true) {
                byte[] data = new byte[1];
                int bytesRead = serialPort.readBytes(data, data.length);
                if (bytesRead > 0) {
                    byte receivedByte = data[0];
                    handleSerialData(receivedByte); // Se llama a la funcion para que lea el input del arduino
                }
            }
        });

        serialReaderThread.start();

        // Crear la cuadrícula de puntos
        for (int row = 0; row < GRID_SIZE; row++) {
            List<Circle> rowList = new ArrayList<>();
            for (int col = 0; col < GRID_SIZE; col++) {
                Circle circle = new Circle(POINT_RADIUS);
                circle.setFill(Color.BLACK);
                circle.setCenterX((col + 1) * 100 + 50); // Posición X del punto
                circle.setCenterY((row + 1) * 100 + 50); // Posición Y del punto

                // Manejar eventos de teclado para el movimiento del jugador
                scene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.W && playerRow > 0) { // Mover hacia arriba
                        playerRow--;
                        updatePlayerPosition();
                    } else if (event.getCode() == KeyCode.S && playerRow < GRID_SIZE - 1) { // Mover hacia abajo
                        playerRow++;
                        updatePlayerPosition();
                    } else if (event.getCode() == KeyCode.A && playerCol > 0) { // Mover hacia la izquierda
                        playerCol--;
                        updatePlayerPosition();
                    } else if (event.getCode() == KeyCode.D && playerCol < GRID_SIZE - 1) { // Mover hacia la derecha
                        playerCol++;
                        updatePlayerPosition();
                    } else if (event.getCode() == KeyCode.SPACE) { // Seleccionar punto
                        selectPoint(playerCol, playerRow);
                    }
                });

                rowList.add(circle);
                backgroundPane.getChildren().add(circle);
            }
            grid.add(rowList);
        }

        primaryStage.setScene(scene); // Volver a mostrar la malla de puntos para que no las sobreescriba la linea
        primaryStage.show();
    }

    /**
     * Maneja los datos recibidos desde el puerto serial y actualiza la posición del jugador.
     * 
     * @param receivedByte El byte recibido desde el puerto serial.
     */
    private void handleSerialData(byte receivedByte) {
        Platform.runLater(() -> {
            String receivedChar = String.valueOf((char) receivedByte);
            if (receivedChar.equals("B") && playerRow > 0) { // Mover hacia arriba
                playerRow--;
                updatePlayerPosition();
            } else if (receivedChar.equals("G") && playerRow < GRID_SIZE - 1) { // Mover hacia abajo
                playerRow++;
                updatePlayerPosition();
            } else if (receivedChar.equals("A") && playerCol > 0) { //Mover hacia la izquierda
                playerCol--;
                updatePlayerPosition();
            } else if (receivedChar.equals("F") && playerCol < GRID_SIZE - 1) { // Mover hacia la derecha
                playerCol++;
                updatePlayerPosition();
            } else if (receivedChar.equals("J")) { // Seleccionar punto
                selectPoint(playerCol, playerRow);
            }
        });
    }

    /**
     * Actualiza la posición visual del jugador en la cuadrícula.
     */
    private void updatePlayerPosition() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Circle circle = grid.get(row).get(col);
                if (row == playerRow && col == playerCol) {
                    circle.setFill(Color.web(clientColor)); // Indicador de color del cliente que se mueve
                } else {
                    circle.setFill(Color.BLACK); // Volver al color original del punto
                }
            }
        }
    }

    /**
     * Maneja la selección de un punto en la cuadrícula.
     * 
     * @param col La columna del punto seleccionado.
     * @param row La fila del punto seleccionado.
     */
    private void selectPoint(int col, int row) {
        if (firstPoint == null) {
            firstPoint = GameData.createPointData(col + 1, row + 1); // Crea el punto de inicio de la linea
        } else {
            GameData secondPoint = GameData.createPointData(col + 1, row + 1); // Crea el punto de final de linea
            sendGameDataToServer(firstPoint, secondPoint); // Envia las coordenadas de los puntos al servidor
            firstPoint = null; // Se quita el punto de inicio para seleccionar otro
        }
    }

    /**
     * Envía datos de juego al servidor, como la selección de puntos y líneas dibujadas.
     * 
     * @param gameData1 Los datos del primer punto.
     * @param gameData2 Los datos del segundo punto.
     */
    private void sendGameDataToServer(GameData gameData1, GameData gameData2) {
        try {
            Gson gson = new Gson(); // Crea nuevo Gson
            GameData lineData = GameData.createLineData( // Envia coordenadas de inicio y fin de la linea
                    gameData1.getX(), gameData1.getY(),
                    gameData2.getX(), gameData2.getY(),
                    "clientColor" // Envia el color del cliente que la dibujo
            );
            Object jsonLineData = gson.toJson(lineData);
            out.println(jsonLineData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Dibuja una línea en el juego basada en los datos recibidos del servidor.
     * 
     * @param receivedData Los datos de la línea recibidos del servidor.
     */
    private void drawLineFromReceivedData(GameData receivedData) { // Multiplica por 100+50 para ajustar la linea a la posicion del punto
        int startX = receivedData.getStartX() * 100 + 50;
        int startY = receivedData.getStartY() * 100 + 50;
        int endX = receivedData.getEndX() * 100 + 50;
        int endY = receivedData.getEndY() * 100 + 50;

        Line line = new Line(startX, startY, endX, endY);
        String color = receivedData.getColor(); // Obtiene el color del cliente que la envia
        line.setStroke(Color.web(color));
        line.setStrokeWidth(2.0);

        Platform.runLater(() -> {
            backgroundPane.getChildren().add(0, line); // Actualiza la interfaz grafica
        });
    }

    /**
     * Cierra la conexión con el servidor y el puerto serial al detener la aplicación.
     */
    public void stop() throws Exception {
        if (socket != null) {
            socket.close();
        }
        if (serialPort != null) {
            serialPort.closePort();
        }
    }
}
