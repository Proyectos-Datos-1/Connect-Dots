import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

public class Server {
    private static final int PORT = 12345;
    private static List<LineInfo> lines = new ArrayList<>();
    private static List<ClientHandler> clients = new ArrayList<>();
    private static Coordinates firstPoint = null;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor listo para recibir conexiones...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde " + clientSocket.getInetAddress());

                // Crea un nuevo hilo para manejar la conexión con el cliente
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
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
        private BufferedReader in;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Coordenadas recibidas del cliente: " + inputLine);

                    // Parsea las coordenadas recibidas desde el cliente
                    Gson gson = new Gson();
                    Coordinates coordinates = gson.fromJson(inputLine, Coordinates.class);

                    if (firstPoint == null) {
                        firstPoint = coordinates;
                    } else {
                        // Agrega la nueva línea a la lista de líneas
                        LineInfo newLine = new LineInfo(firstPoint, coordinates);
                        lines.add(newLine);

                        // Envía la lista de líneas actualizada a todos los clientes
                        broadcastLineInfoToClients();
                        firstPoint = null;
                    }
                }

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void broadcastLineInfoToClients() {
        Gson gson = new Gson();
        String jsonLines = gson.toJson(lines);

        for (ClientHandler client : clients) {
            try {
                client.out.println(jsonLines);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
