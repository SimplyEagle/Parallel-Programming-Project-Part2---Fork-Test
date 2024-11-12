import java.io.*;
import java.net.*;
import java.util.Random;

public class TCPClient {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 5556; // Port where TCPServer is listening
        // Can be 5555 is multiple machines. Needs to be different if localhost because port closed.

        try (Socket socket = new Socket(serverAddress, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Generate two random matrices of size
            int size = 4;
            int[][] matrixA = generateMatrix(size);
            int[][] matrixB = generateMatrix(size);

            // Send both matrices to the server
            System.out.println("Sending Matrix A:");
            printMatrix(matrixA);
            out.writeObject(matrixA);
            out.flush();

            System.out.println("Sending Matrix B:");
            printMatrix(matrixB);
            out.writeObject(matrixB);
            out.flush();
            System.out.println();

            // Receive the result from the server
            System.out.println("Waiting for reply.");
            int[][] result = (int[][]) in.readObject();
            System.out.println("Result received from server:");
            printMatrix(result);
            System.out.println();

            String[] metrics = new String[4];
            for (int i = 0; i < metrics.length; i++) {
                metrics[i] = (String) in.readObject();
            }

            for (String str : metrics) {
                System.out.println(str);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int[][] generateMatrix(int size) {
        Random random = new Random();
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = random.nextInt(9)+1;  // Random values between 1 and 9
            }
        }
        return matrix;
    }

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }
}
