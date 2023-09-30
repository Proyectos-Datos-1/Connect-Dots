/**
 * @author Fabricio Mena, Joseph Murillo, Nathalia Ocampo
 */
import com.google.gson.Gson;
import java.io.*;
import java.net.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;



/**
 * Esta clase representa el servidor del juego "Connect Dots". Administra la conexión con los clientes,
 * asigna colores únicos a los clientes, maneja el inicio y cierre del servidor, y gestiona la lógica del juego.
 */
public class Server extends Application {
    private static final int PORT = 12345; // Puerto de conexión del servidor
    private static ClientHandler[] clients = new ClientHandler[2]; // Lista de manejadores de clientes conectados
    private static int nextClientId = 1; // ID del próximo cliente
    private static int currentPlayerIndex = 0; // Índice del cliente actual en el juego

    // Define una lista de colores disponibles para asignar a los clientes
    private static final String[] colors = {"blue", "red", "yellow", "purple"};
    private static ServerSocket serverSocket; // Socket del servidor
    private static boolean serverRunning = false; // Indica si el servidor está en ejecución
    private static GameData[] drawnLines = new GameData[100]; // Lista global de líneas dibujadas
    private static javafx.scene.control.Label resultLabel;
    private static Stage[] clientStages = new Stage[100]; // Lista de ventanas de clientes

    
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

        // Crear el ChoiceBox para seleccionar el número de clientes
        ChoiceBox<Integer> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll(1, 2, 3, 4);
        choiceBox.setValue(2); // Valor predeterminado
        choiceBox.setOnAction(e -> {
            int numClients = choiceBox.getValue();
            clients = new ClientHandler[numClients];
            openClients(numClients); // Llama al método para abrir la cantidad de clientes seleccionados
        });
        // Crear el botón para iniciar el juego
        Button startGameButton = new Button("Iniciar Juego");
        startGameButton.setOnAction(e -> {
            int numClients = choiceBox.getValue();
            startGame(numClients); // Llama al método para iniciar el juego con la cantidad de clientes seleccionados
        });
        

        Button restartButton = new Button("Reiniciar"); // Se crea el boton para reiniciar partida
        restartButton.setOnAction(e -> restartServer());

        VBox vbox = new VBox(10); // Distancia entre botones
        vbox.getChildren().addAll(startServerButton, restartButton, choiceBox, startGameButton);
        vbox.setPrefSize(300, 350);
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

                        for (int i = 0; i < clients.length; i++) {
                            if (clients[i] == null) {
                                clients[i] = clientHandler;
                                break;
                            }
                        }
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
            if (clientStage != null) {
                clientStage.close();
            }
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
        for (int i = 0; i < clients.length; i++) {
            clients[i] = null;
        }
        for (int i = 0; i < drawnLines.length; i++) {
            drawnLines[i] = null;
        }

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
     * Abre la cantidad de clientes seleccionados.
     *
     * @param numClients Cantidad de clientes a abrir.
     */
    private void openClients(int numClients) {
        for (int i = 0; i < numClients; i++) {
            Client client = new Client();
            Stage clientStage = new Stage();
            client.start(clientStage);
            clientStages[i] = clientStage;
        }
    }

    /**
     * Inicia el juego con la cantidad de clientes seleccionados.
     *
     * @param numClients Cantidad de clientes para iniciar el juego.
     */
    private void startGame(int numClients) {
        // Abrir la cantidad de clientes seleccionados
        openClients(numClients);
        // Iniciar el servidor
        startServer();
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
        
        /**
         * Realiza todas la verificaciones sobre lineas y cuadrados.
         */
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
                                && clients[currentPlayerIndex] != null && clientId == clients[currentPlayerIndex].clientId) {
                            // Establece el color del emisor y reenvía las coordenadas a todos los clientes
                            data.setColor(clientColor);
                            sendToAllClients(gson.toJson(data));

                            // Verificar si ya existe una línea en las mismas coordenadas
                            boolean coordinatesOccupied = false;
                            synchronized (drawnLines) {
                                for (int i = 0; i < drawnLines.length; i++) {
                                    if (drawnLines[i] != null && "line".equals(drawnLines[i].getType()) && isLineOverlapping(data, drawnLines[i])) {
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
                                    for (int i = 0; i < drawnLines.length; i++) {
                                        if (drawnLines[i] == null) {
                                            drawnLines[i] = data;
                                            // Llama al metodo para verificar cuadrados
                                            checkForSquare();
                                            // Pasa el turno al siguiente cliente
                                            currentPlayerIndex = (currentPlayerIndex + 1) % clients.length;
                                            break;
                                        }
                                    }
                                    // Si se lleno la cuadricula de lineas, devuelve puntuaciones
                                    int count = 0;
                                    for (GameData line : drawnLines) {
                                        if (line != null) {
                                            count++;
                                        }
                                    }

                                    if (count == 24) {
                                        showResults(); // Llama al método para mostrar los resultados
                                    }
                                }
                                
                            }
                        }
                    }
                }


                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Verifica si dos líneas se superponen completamente en espacio.
         *
         * @return true si se superponen las lineas.
         */
        private boolean isLineOverlapping(GameData newLine, GameData existingLine) {
            // Verificar si las líneas tienen los mismos puntos de inicio y fin
            return (newLine.getStartX() == existingLine.getStartX() && newLine.getStartY() == existingLine.getStartY() &&
                    newLine.getEndX() == existingLine.getEndX() && newLine.getEndY() == existingLine.getEndY()) ||
                (newLine.getStartX() == existingLine.getEndX() && newLine.getStartY() == existingLine.getEndY() &&
                    newLine.getEndX() == existingLine.getStartX() && newLine.getEndY() == existingLine.getStartY());
        }
        
        private GameData[] foundSquares = new GameData[100]; // Lista de cuadrados encontrados

        /**
         * Verifica si se han formado cuadrados en la malla de puntos.
         *
         * @return true si se han formado cuadrados, false en caso contrario.
         */
        private boolean checkForSquare() {
            boolean squareFound = false;
            // Verifica si es cuadrado nuevo o viejo
            for (int row = 1; row <= 3; row++) {
                for (int col = 1; col <= 3; col++) {
                    if (isSquare(row, col)) {
                        boolean squareAlreadyFound = false;
                        // Verifica si ya se detecto el cuadrado
                        for (GameData square : foundSquares) {
                            if (square != null && areEqualSquares(square, row, col) && square.getColor().equals(clientColor)) {
                                squareAlreadyFound = true; // Si es cuadrado viejo
                                break;
                            }
                        }
                        // Si se encuentra un cuadrado nuevo
                        if (!squareAlreadyFound) {
                            for (int i = 0; i < foundSquares.length; i++) {
                                if (foundSquares[i] == null) {
                                    foundSquares[i] = GameData.createSquareData(row, col);
                                    squareFound = true;
                                    incrementScore(); // Aumenta el score
                                    sendScoreToClient(); // Envia el score a todos los clientes
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        
            return squareFound;
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
            for (GameData line : drawnLines) {
                if (line != null && "line".equals(line.getType())) { // Agregar verificación para line no sea null
                    int startX = line.getStartX();
                    int startY = line.getStartY();
                    int endX = line.getEndX();
                    int endY = line.getEndY();
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
            // Ordena el arreglo de clientes por puntuación
            for (int i = 0; i < clients.length - 1; i++) {
                for (int j = i + 1; j < clients.length; j++) {
                    if (clients[i] != null && clients[j] != null) {
                        if (clients[i].getScore() < clients[j].getScore()) {
                            ClientHandler temp = clients[i];
                            clients[i] = clients[j];
                            clients[j] = temp;
                        }
                    }
                }
            }
            // Crea un mensaje con los resultados
            StringBuilder message = new StringBuilder("!JuegoTerminado!:\nResultados:\n");
            for (int i = 0; i < clients.length; i++) {
                if (clients[i] != null) {
                    message.append("Puesto ").append(i + 1).append(": Cliente ").append(clients[i].getClientId()).append(" - Puntuación ").append(clients[i].getScore()).append("\n");
                    
                }
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
                if (client != null) {
                    GameData scoreData = GameData.createScoreData(client.clientColor, client.score);
                    client.sendMessage(new Gson().toJson(scoreData));
                }
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
                if (client != null) {
                    client.sendMessage(message);
                }
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
