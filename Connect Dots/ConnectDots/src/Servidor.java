import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor {
    private static List<ClientHandler> clientHandlers = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        List<Integer> puertosDisponibles = new ArrayList<>();
        puertosDisponibles.add(12345);
        puertosDisponibles.add(12346);
        puertosDisponibles.add(12347);
        puertosDisponibles.add(12348);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        ExecutorService executor = Executors.newFixedThreadPool(4);

        while (!puertosDisponibles.isEmpty()) {
            int puerto = puertosDisponibles.remove(0); // Tomar el primer puerto disponible

            try (ServerSocket serverSocket = new ServerSocket(puerto)) {
                System.out.println("Servidor escuchando en el puerto " + puerto);

                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Cliente conectado desde " + socket.getInetAddress().getHostAddress());

                    // Crear un nuevo manejador de cliente y agregarlo a la lista
                    ClientHandler clientHandler = new ClientHandler(socket);
                    clientHandlers.add(clientHandler);

                    // Iniciar un nuevo hilo para manejar al cliente
                    executor.submit(clientHandler);
                }
                // No cerramos el serverSocket aquí para mantenerlo abierto
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Clase ClientHandler como una clase interna
    static class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectMapper objectMapper;
        private InputStream inputStream;
        private OutputStream outputStream;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.objectMapper = new ObjectMapper();
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
        }

        @Override
        public void run() {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                OutputStream outputStream = socket.getOutputStream();
                
                while (true) {
                    // Esperar hasta que haya datos disponibles en el flujo de entrada del socket
                    while (inputStream.available() == 0) {
                        Thread.sleep(100); // Esperar 100 milisegundos antes de volver a verificar
                    }
            
                    // Leer un array JSON de coordenadas
                    ArrayNode receivedCoordinates = objectMapper.readValue(inputStream, ArrayNode.class);
            
                    // Procesar las coordenadas
                    double x1 = receivedCoordinates.get(0).asDouble();
                    double y1 = receivedCoordinates.get(1).asDouble();
                    double x2 = receivedCoordinates.get(2).asDouble();
                    double y2 = receivedCoordinates.get(3).asDouble();
            
                    System.out.println("Coordenadas recibidas: (" + x1 + ", " + y1 + ") y (" + x2 + ", " + y2 + ")");
            
                    // Enviar las coordenadas a todos los clientes conectados
                    broadcastCoordinates(receivedCoordinates);
                    objectMapper.writeValue(outputStream, receivedCoordinates);
                    outputStream.flush(); // Asegurarse de que los datos se envíen inmediatamente
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Puedes agregar un mensaje de error aquí si lo deseas
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaurar la interrupción
                e.printStackTrace();
            }
        }            

        // Método para enviar coordenadas al cliente
        public void sendCoordinates(ArrayNode coordinates) {
            try {
                objectMapper.writeValue(outputStream, coordinates);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para enviar coordenadas a todos los clientes conectados
    public static void broadcastCoordinates(ArrayNode coordinates) {
        for (ClientHandler handler : clientHandlers) {
            handler.sendCoordinates(coordinates);
        }
    }
}