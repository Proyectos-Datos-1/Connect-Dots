/**
 * @author Fabricio Mena, Joseph Murillo, Nathalia Ocampo
 */
import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    private static List<GameData> drawnLines = new ArrayList<>(); // Lista global de lineas dibujadas
    private static javafx.scene.control.Label resultLabel;
    private static List<Stage> clientStages = new ArrayList<>();


    
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

        

        Button openClientButton = new Button("Abrir Cliente"); // Se crea el boton para abrir clientes
        openClientButton.setOnAction(e -> openClient());

        Button restartButton = new Button("Reiniciar"); // Se crea el boton para reiniciar partida
        restartButton.setOnAction(e -> restartServer());

        VBox vbox = new VBox(10); // Distancia entre botones
        vbox.getChildren().addAll(startServerButton, openClientButton, restartButton);
        vbox.setPrefSize(200, 200);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);

        Scene scene = new Scene(vbox); // Se crean las cajitas de los botones
        primaryStage.setScene(scene); // Se crea la ventana
        primaryStage.show();

        // Etiqueta de resultLabel
        resultLabel = new Label("");
        vbox.getChildren().add(resultLabel);
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
     * Cierra y vuelve a abrir el servidor, y cierra los clientes.
     */
    private void restartServer() {
        // Cerrar las ventanas de los clientes
        for (Stage clientStage : clientStages) {
            clientStage.close();
        }
        
        // Detener el servidor
        stopServer();
    
        // Cerrar los clientes
        for (ClientHandler client : clients) {
            try {
                client.clientSocket.close(); // Cerrar el socket del cliente
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        // Limpiar la lista de clientes y la lista de líneas dibujadas
        clients.clear();
        drawnLines.clear();

        // Restablecer el índice del siguiente jugador
        nextClientId = 1;

    
        // Restablecer el índice del jugador actual
        currentPlayerIndex = 0;
    
        // Actualizar la etiqueta de resultados a vacío
        resultLabel.setText("");
    
        // Iniciar el servidor nuevamente
        startServer();
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

        // Agregar la ventana del cliente a la lista
        clientStages.add(clientStage);
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

        /**
         * Constructor de ClientHandler.
         *
         * @param clientSocket Socket de comunicación con el cliente.
         * @param clientId     Identificador único del cliente.
         * @param clientColor  Color asignado al cliente.
         * @param out          Texto de salida.
         */
        public ClientHandler(Socket clientSocket, int clientId, String clientColor) {
            try {
                this.clientSocket = clientSocket;
                this.clientId = clientId;
                this.clientColor = clientColor;
                this.out = new PrintWriter(clientSocket.getOutputStream(), true); // Inicializa el PrintWriter
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
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

                            // Verificar si ya existe una línea en las mismas coordenadas
                            boolean coordinatesOccupied = false;
                            synchronized (drawnLines) {
                                for (GameData line : drawnLines) {
                                    
                                    if ("line".equals(line.getType()) && isLineOverlapping(data, line)) {
                                        coordinatesOccupied = true;
                                        break;
                                    }
                                }
                            }

                            // Si las coordenadas están ocupadas, no se permite dibujar la línea
                            if (coordinatesOccupied) {
                                System.out.println("El cliente " + clientId + " no puede dibujar una línea en coordenadas ocupadas.");
                            } else {
                                // Agregar la línea a la lista drawnLines compartida
                                synchronized (drawnLines) {
                                    drawnLines.add(data);
                                    System.out.println(data);
                                    checkForSquare();
                                    // Si se lleno la cuadricula de lineas, devuelve puntuaciones
                                    if(drawnLines.size()==24){
                                        showResults(); // Llama al método para mostrar los resultados
                                    }
                                }
                            // Pasa el turno al siguiente cliente
                            currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
                        }
                    }
                }
            }

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Método para verificar si dos líneas se superponen completamente en espacio
        private boolean isLineOverlapping(GameData newLine, GameData existingLine) {
            // Verificar si las líneas tienen los mismos puntos de inicio y fin
            return (newLine.getStartX() == existingLine.getStartX() && newLine.getStartY() == existingLine.getStartY() &&
                    newLine.getEndX() == existingLine.getEndX() && newLine.getEndY() == existingLine.getEndY()) ||
                (newLine.getStartX() == existingLine.getEndX() && newLine.getStartY() == existingLine.getEndY() &&
                    newLine.getEndX() == existingLine.getStartX() && newLine.getEndY() == existingLine.getStartY());
        }
        
        private List<GameData> foundSquares = new ArrayList<>();

        /**
         * Verifica si se han formado cuadrados en la malla de puntos.
         *
         * @return true si se han formado cuadrados, false en caso contrario.
         */
        private boolean checkForSquare() {
            boolean squareFound = false; // Variable para rastrear si se ha encontrado un cuadrado en este ciclo

            // Coordenadas de los puntos de la malla (columna 1, fila 1) hasta (columna 4, fila 4)
            for (int row = 1; row <= 3; row++) {
                for (int col = 1; col <= 3; col++) {
                    // Verificar si los puntos forman un cuadrado
                    if (isSquare(row, col)) {
                        // Verifica si el cuadrado ya ha sido encontrado previamente
                        boolean squareAlreadyFound = false;
                        for (GameData square : foundSquares) {
                            if (areEqualSquares(square, row, col)) {
                                squareAlreadyFound = true;
                                break;
                            }
                        }

                        if (!squareAlreadyFound) {
                            foundSquares.add(GameData.createSquareData(row, col)); // Agrega el cuadrado a la lista de cuadrados encontrados
                            squareFound = true; // Marca que se ha encontrado un cuadrado en este ciclo
                            clients.get(currentPlayerIndex).incrementScore();
                            sendScoreToClient();
                        }
                    }
                }
            }
            if (squareFound) {
                squareFound = false; // Si se encontró un cuadrado, restablece squareFound a false
            }
            return squareFound; // Devuelve true si se encontró al menos un cuadrado en este ciclo
        }

        /**
         * Verifica si dos cuadrados son iguales.
         *
         * @param squareData Los datos del cuadrado a comparar.
         * @param row        La fila del punto superior izquierdo del cuadrado.
         * @param col        La columna del punto superior izquierdo del cuadrado.
         * @return true si los cuadrados son iguales, false en caso contrario.
         */
        private boolean areEqualSquares(GameData squareData, int row, int col) {
            return squareData.getStartX() == col && squareData.getStartY() == row;
        }
                

        /**
         * Verifica si los puntos en las coordenadas (row, col) forman un cuadrado.
         *
         * @param row La fila del punto superior izquierdo del cuadrado.
         * @param col La columna del punto superior izquierdo del cuadrado.
         * @return true si los puntos forman un cuadrado, false en caso contrario.
         */
        private boolean isSquare(int row, int col) {
            // Verificar si los cuatro lados del cuadrado están formados por líneas
            return hasLine(row, col, row, col + 1) &&
                hasLine(row, col, row + 1, col) &&
                hasLine(row, col + 1, row + 1, col + 1) &&
                hasLine(row + 1, col, row + 1, col + 1);
        }

        /**
         * Verifica si hay una línea entre dos puntos en la malla.
         *
         * @param row1 Fila del primer punto.
         * @param col1 Columna del primer punto.
         * @param row2 Fila del segundo punto.
         * @param col2 Columna del segundo punto.
         * @return true si hay una línea entre los dos puntos, false en caso contrario.
         */
        private boolean hasLine(int row1, int col1, int row2, int col2) {
            // Recorre todas las líneas dibujadas para buscar una línea entre los puntos dados
            for (GameData line : drawnLines) {
                if ("line".equals(line.getType())) {
                    int startX = line.getStartX();
                    int startY = line.getStartY();
                    int endX = line.getEndX();
                    int endY = line.getEndY();

                    // Verifica si la línea conecta los puntos (row1, col1) y (row2, col2)
                    if ((startX == col1 && startY == row1 && endX == col2 && endY == row2) ||
                        (startX == col2 && startY == row2 && endX == col1 && endY == row1)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Agrega los score finales a la interfaz.
         */
        private void showResults() {
            // Ordena los clientes por puntuación
            clients.sort(Comparator.comparingInt(ClientHandler::getScore).reversed());

            // Crea un mensaje con los resultados
            StringBuilder message = new StringBuilder("Resultados:\n");
            for (int i = 0; i < clients.size(); i++) {
                ClientHandler client = clients.get(i);
                message.append("Puesto ").append(i + 1).append(": Cliente ").append(client.getClientId()).append(" - Puntuación ").append(client.getScore()).append("\n");
            }

            // Actualiza la etiqueta en la interfaz gráfica
            Platform.runLater(() -> resultLabel.setText(message.toString()));
        }

        /**
         * Obtiene el score.
         *
         * @return score del cliente.
         */
        public int getScore() {
            return score;
        }

        /**
         * Obtiene cliente actual.
         *
         * @return Numero de cliente.
         */
        public int getClientId() {
            return clientId;
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
         *
         * @param message Mensaje a enviar a todos los clientes.
         */
        private void sendToAllClients(String message) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }

        /**
         * Envía un mensaje al cliente.
         *
         * @param message Mensaje a enviar al cliente.
         */
        private void sendMessage(String message) {
            out.println(message);
        }
    }

    /**
     * Función para verificar si dos puntos son adyacentes.
     *
     * @param point1 Primer punto.
     * @param point2 Segundo punto.
     * @return true si los puntos son adyacentes, false en caso contrario.
     */
    private static boolean areAdjacent(GameData point1, GameData point2) {
        int dx = Math.abs(point1.getX() - point2.getX());
        int dy = Math.abs(point1.getY() - point2.getY());

        // Dos puntos son adyacentes si su diferencia en coordenadas X o Y es igual a 1, pero no ambos
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    /**
     * Función para verificar si la línea es vertical u horizontal.
     *
     * @param start Punto de inicio de la línea.
     * @param end   Punto de fin de la línea.
     * @return true si la línea es vertical u horizontal, false en caso contrario.
     */
    private static boolean isVerticalOrHorizontal(GameData start, GameData end) {
        int dx = Math.abs(start.getX() - end.getX());
        int dy = Math.abs(start.getY() - end.getY());

        // La línea es vertical u horizontal si una de las diferencias es 0 y la otra es 1
        return (dx == 0 && dy == 1) || (dx == 1 && dy == 0);
    }
}
