import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 12345;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static int nextClientId = 1;

    // Define una lista de colores disponibles
    private static final String[] colors = {"blue", "red", "yellow", "purple"};

    public static void main(String[] args) {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor listo para recibir conexiones...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde " + clientSocket.getInetAddress());

                // Asigna un color único al cliente
                String clientColor = colors[nextClientId - 1];
                int clientId = nextClientId++;
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientId, clientColor);

                clients.add(clientHandler);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
                clientHandler.sendColorToClient(); // Enviar el color al cliente
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private int clientId;
        private String clientColor;

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

                    // Realiza las verificaciones de adyacencia y orientación
                    if ("line".equals(data.getType())) {
                        GameData start = GameData.createPointData(data.getStartX(), data.getStartY());
                        GameData end = GameData.createPointData(data.getEndX(), data.getEndY());

                        if (areAdjacent(start, end) && isVerticalOrHorizontal(start, end)) {
                            // Establece el color del emisor y reenvía las coordenadas a todos los clientes
                            data.setColor(clientColor);
                            sendToAllClients(gson.toJson(data));
                        }
                    }
                }
                
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendColorToClient() {
            GameData colorData = GameData.createColorData(clientColor);
            sendMessage(new Gson().toJson(colorData));
        }

        private void sendToAllClients(String message) {
            for (ClientHandler client : clients) {
                if (client != this) {
                    client.sendMessage(message);
                }
            }
        }

        private void sendMessage(String message) {
            out.println(message);
        }
    }

    // Función para verificar si dos puntos son adyacentes
    private static boolean areAdjacent(GameData point1, GameData point2) {
        int dx = Math.abs(point1.getX() - point2.getX());
        int dy = Math.abs(point1.getY() - point2.getY());

        // Dos puntos son adyacentes si su diferencia en coordenadas X o Y es igual a 1, pero no ambos
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    // Función para verificar si la línea es vertical u horizontal
    private static boolean isVerticalOrHorizontal(GameData start, GameData end) {
        int dx = Math.abs(start.getX() - end.getX());
        int dy = Math.abs(start.getY() - end.getY());

        // La línea es vertical u horizontal si una de las diferencias es 0 y la otra es 1
        return (dx == 0 && dy == 1) || (dx == 1 && dy == 0);
    }
}
