import java.io.*;
import java.net.*;

public class SThread extends Thread {
	private Socket clientSocket;
	private Object[][] routingTable;
	private int index;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	private int threadCount;

	// Constructor that retains the RoutingTable and index
	public SThread(Socket clientSocket, Object[][] routingTable, int index, int threadCount) {
		this.clientSocket = clientSocket;
		this.routingTable = routingTable;
		this.index = index;
		this.threadCount = threadCount;

		try {
			outStream = new ObjectOutputStream(clientSocket.getOutputStream());
			inStream = new ObjectInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			System.out.println("Error initializing I/O streams: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			System.out.println("Handling client at index " + index);

			// Process client requests and responses here
			while (true) {
				System.out.println("Waiting for matrices from client...");
				// Read two matrices input from client
				int[][] matrix1 = (int[][]) inStream.readObject();
				int[][] matrix2 = (int[][]) inStream.readObject();

				System.out.println("Received Matrix 1 from client:");
				printMatrix(matrix1);
				System.out.println("Received Matrix 2 from client:");
				printMatrix(matrix2);

				// Perform matrix multiplication with threadCount
				System.out.println("Attempting matrix multiplication.");
				StrassenMatrixMulti matrixMultiplier = new StrassenMatrixMulti(threadCount);
				int[][] result = matrixMultiplier.multiply(matrix1, matrix2);
				System.out.println("Matrix multiplication complete.");

				// Send result back
				System.out.println("Sending result to client...");
				outStream.writeObject(result);
				outStream.flush();  // Ensure data is actually sent to the client
				System.out.println("Result sent to client:");

				printMatrix(result);
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Connection error with client at index " + index + ": " + e.getMessage());
		} finally {
			// Clean up resources
			try {
				inStream.close();
				outStream.close();
				clientSocket.close();
			} catch (IOException e) {
				System.out.println("Error closing resources for client at index " + index);
			}
		}
	}

	private void printMatrix(int[][] matrix) {
		for (int[] row : matrix) {
			for (int val : row) {
				System.out.print(val + " ");
			}
			System.out.println();
		}
	}
}
