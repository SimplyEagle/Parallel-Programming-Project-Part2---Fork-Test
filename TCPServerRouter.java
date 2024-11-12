import java.io.*;
import java.net.*;

public class TCPServerRouter {
    public static void main(String[] args) {
        int portNumber = 5555; // Port number to listen for incoming connections
        boolean running = true;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("ServerRouter is listening on port " + portNumber);

            while (running) {
                try {
                    // Accept a new client connection (a TCPServer instance)
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connected to client: " + clientSocket.getInetAddress().getHostAddress());

                    // Forward the connection to TCPServer for processing
                    forwardToTCPServer(clientSocket);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            e.printStackTrace();
        }
    }

    // Forward the client connection
    private static void forwardToTCPServer(Socket clientSocket) {
        try {
            String serverHost = "localhost";
            int serverPort = 5556; // Port that the TCPServer is listening on
            // Can be 5555 is multiple machines. Needs to be different if localhost because port closed.

            Socket serverSocket = new Socket(serverHost, serverPort);
            System.out.println("Forwarding request to TCPServer at " + serverHost + " on port " + serverPort);

            // Create object streams to send/receive matrices between Router and Server
            ObjectOutputStream out = new ObjectOutputStream(serverSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(serverSocket.getInputStream());

            // Transfer the client matrices to the TCPServer
            ObjectOutputStream clientOut = new ObjectOutputStream(clientSocket.getOutputStream());
            clientOut.writeObject(in.readObject());
            clientOut.flush();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
