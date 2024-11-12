import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class TCPServer {
    private static final int portNumber = 5556; // Port for the TCPServer to listen on
    // Can be 5555 is multiple machines. Needs to be different if localhost because port closed.
    private static ForkJoinPool forkJoinPool;
    private static Object[][] routingTable;
    private StrassenMatrixMulti matrixMultiplier;
    private static int currentIndex = 0;

    public static void main(String[] args) {
        int threadCount = 4; // Set the number of threads in the pool. 1, 2, 15, 31, etc
        forkJoinPool = new ForkJoinPool(1);
        routingTable = new Object[10][2];

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("TCPServer started and listening on port " + portNumber);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected from " + clientSocket.getInetAddress().getHostAddress());

                // Forward the connection to a new thread for handling
                int clientIndex = addClient(clientSocket);
                forkJoinPool.submit(new SThread(clientSocket, routingTable, clientIndex, threadCount));
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            e.printStackTrace();
        }
    }

    // Method to add a new client to the routing table
    private static int addClient(Socket clientSocket) {
        int index = currentIndex++;
        routingTable[index][0] = clientSocket;
        routingTable[index][1] = true;
        return index;
    }

    // Shuts down the pool
    public static void shutdownServer() {
        try {
            System.out.println("Shutting down Executor.");
            forkJoinPool.shutdown();
            StrassenMatrixMulti.shutdownExecutor();
        } catch (Exception e) {
            System.err.println("Error shutting down server: " + e.getMessage());
        }
    }
}
