/**
 * @author Fabricio Mena, Joseph Murillo, Nathalia Ocampo
 */
import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
/**
 * Esta clase representa el servidor del juego "Connect Dots". Administra la conexión con los clientes,
 * asigna colores únicos a los clientes, maneja el inicio y cierre del servidor, y gestiona la lógica del juego.
 */
public class Server extends Application {
    private static final int PORT = 12345; // Puerto de conexión del servidor
    private static List<ClientHandler> clients = new ArrayList<>(); // Lista de manejadores de clientes conectados
    private static int nextClientId = 1; // ID del próximo cliente
    private static int currentPlayerIndex = 0; // Índice del cliente actual en el juego

    // Define una lista de colores disponibles para asignar a los clientes
    private static final String[] colors = {"blue", "red", "yellow", "purple"};
    private static ServerSocket serverSocket; // Socket del servidor
    private static boolean serverRunning = false; // Indica si el servidor está en ejecución

    
    /** 
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) { // Creacion de la interfaz grafica
        primaryStage.setTitle("Server GUI");

        Button startServerButton = new Button("Iniciar Servidor"); // Se crea el boton para iniciar el servidor
        startServerButton.setOnAction(e -> startServer());

        Button stopServerButton = new Button("Cerrar Servidor"); // Se crea el boton para cerrar el servidor
        stopServerButton.setOnAction(e -> stopServer());

        Button openClientButton = new Button("Abrir Cliente"); // Se crea el boton para abrir clientes
        openClientButton.setOnAction(e -> openClient());

        VBox vbox = new VBox(10); // Distancia entre botones
        vbox.getChildren().addAll(startServerButton, stopServerButton, openClientButton); // Se agregan los botones a la escena
        vbox.setPrefSize(200, 200);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);

        Scene scene = new Scene(vbox); // Se crean las cajitas de los botones
        primaryStage.setScene(scene); // Se crea la ventana
        primaryStage.show();
    }

    /**
     * Inicia el servidor para aceptar conexiones de clientes.
     */
    private void startServer() {
        if (!serverRunning) {
            Thread serverThread = new Thread(() -> {
                try {
                    serverSocket = new ServerSocket(PORT);
                    serverRunning = true;
                    Platform.runLater(() -> System.out.println("Servidor listo para recibir conexiones..."));

                    while (serverRunning) {
                        Socket clientSocket = serverSocket.accept();
                        Platform.runLater(() -> System.out.println("Cliente conectado desde " + clientSocket.getInetAddress()));

                        // Asigna un color único al cliente y crea un manejador de cliente
                        String clientColor = colors[nextClientId - 1]; // Se le asigna el color de la lista al cliente que se conecta
                        int clientId = nextClientId++; // Se incrementa el valor para indicar que se cambia de cliente
                        ClientHandler clientHandler = new ClientHandler(clientSocket, clientId, clientColor);

                        clients.add(clientHandler);
                        Thread clientThread = new Thread(clientHandler); // Inicia nuevo hilo por cliente que se conecta
                        clientThread.start();
                        clientHandler.sendColorToClient(); // Enviar el color al cliente recién conectado
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            serverThread.start();
        }
    }

    /**
     * Detiene el servidor y cierra la conexión con todos los clientes.
     */
    private void stopServer() {
        serverRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                Platform.runLater(() -> System.out.println("Servidor cerrado."));
                // Se puede agregar aquí cualquier otra lógica de limpieza necesaria antes de cerrar el servidor.
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Abre una instancia del cliente.
     */
    private void openClient() {
        // Iniciar la instancia de la clase Client
        Client client = new Client();
        Stage clientStage = new Stage();
        client.start(clientStage);
    }

    /**
     * Clase interna que maneja la comunicación con un cliente.
     */
    private static class ClientHandler implements Runnable {
        private Socket clientSocket; // Socket de comunicacion con el cliente
        private PrintWriter out; // Envia mensajes al cliente
        private int clientId; // Identificador para cada cliente
        private String clientColor; // Color asignado a cada cliente
        private int score = 0; // Puntuación del cliente
        private List<GameData> drawnLines = new ArrayList<>(); // Lista de líneas dibujadas por el cliente

        public ClientHandler(Socket clientSocket, int clientId, String clientColor) {
            this.clientSocket = clientSocket;
            this.clientId = clientId;
            this.clientColor = clientColor;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Coordenadas recibidas del cliente " + clientId + ": " + inputLine);

                    // Parsea las coordenadas recibidas desde el cliente
                    Gson gson = new Gson();
                    GameData data = gson.fromJson(inputLine, GameData.class);

                    // Realiza verificaciones de adyacencia y orientación para las líneas
                    if ("line".equals(data.getType())) {
                        GameData start = GameData.createPointData(data.getStartX(), data.getStartY());
                        GameData end = GameData.createPointData(data.getEndX(), data.getEndY());

                        if (areAdjacent(start, end) && isVerticalOrHorizontal(start, end)
                                && clientId == clients.get(currentPlayerIndex).clientId) {
                            // Establece el color del emisor y reenvía las coordenadas a todos los clientes
                            data.setColor(clientColor);
                            sendToAllClients(gson.toJson(data));
                            // Incrementa el puntaje del cliente actual
                            clients.get(currentPlayerIndex).incrementScore();
                            sendScoreToClient();
                            drawnLines.add(data);
                            // Pasa el turno al siguiente cliente
                            currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
                        }
                    }
                }

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Envía el color asignado al cliente recién conectado.
         */
        private void sendColorToClient() {
            GameData colorData = GameData.createColorData(clientColor);
            sendMessage(new Gson().toJson(colorData));
        }

        /**
         * Envía la puntuación del cliente a todos los clientes.
         */
        private void sendScoreToClient() {
            for (ClientHandler client : clients) {
                GameData scoreData = GameData.createScoreData(client.clientColor, client.score);
                client.sendMessage(new Gson().toJson(scoreData));
            }
        }

        /**
         * Incrementa la puntuación del cliente.
         */
        public synchronized void incrementScore() {
            score++;
        }

        /**
         * Envía un mensaje a todos los clientes.
         */
        private void sendToAllClients(String message) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }

        /**
         * Envía un mensaje al cliente.
         */
        private void sendMessage(String message) {
            out.println(message);
        }
    }

    /**
     * Función para verificar si dos puntos son adyacentes.
     */
    private static boolean areAdjacent(GameData point1, GameData point2) {
        int dx = Math.abs(point1.getX() - point2.getX());
        int dy = Math.abs(point1.getY() - point2.getY());

        // Dos puntos son adyacentes si su diferencia en coordenadas X o Y es igual a 1, pero no ambos
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    /**
     * Función para verificar si la línea es vertical u horizontal.
     */
    private static boolean isVerticalOrHorizontal(GameData start, GameData end) {
        int dx = Math.abs(start.getX() - end.getX());
        int dy = Math.abs(start.getY() - end.getY());

        // La línea es vertical u horizontal si una de las diferencias es 0 y la otra es 1
        return (dx == 0 && dy == 1) || (dx == 1 && dy == 0);
    }
}
