import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.*;


// Measurements print at the top when run.
// Need to update sequential time to what we measured in Part1? For proper speedup calc and thus efficiency.

public class TCPServer {
    private static final int portNumber = 5556; // Port for the TCPServer to listen on
    // Can be 5555 on multiple machines. Needs to be different if localhost because port closed.

    // I made this threaded at one point. Now if I remove it every thing breaks.
    // I assigned it one thread so it's "serial".
    private static ForkJoinPool forkJoinPool;

    private static Object[][] routingTable;
    private StrassenMatrixMulti matrixMultiplier;
    private static int currentIndex = 0;
    double sequentialTime;
    double speedup;
    double efficiency;

    public static void main(String[] args) {
        int threadCount = 4; // Set the number of threads in the pool. 1, 2, 15, 31, etc
        forkJoinPool = new ForkJoinPool(1);
        routingTable = new Object[10][2];
        boolean running = true;
        Scanner sc = new Scanner(System.in);

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("TCPServer started and listening on port " + portNumber);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected from " + clientSocket.getInetAddress().getHostAddress());

                // Forward the connection to a new thread for handling
                int clientIndex = addClient(clientSocket);
                forkJoinPool.submit(new SThread(clientSocket, routingTable, clientIndex, threadCount));

                System.out.println();
                String input = sc.next().toLowerCase();
                if (input.equals("exit")) {
                    clientSocket.close();
                    serverSocket.close();
                    running = false;
                }
                running = false;
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
