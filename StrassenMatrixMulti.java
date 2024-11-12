// There are lots of debugging comments as I could not find the problems.
// You may comment them out or remove if needed. I have my copy.
// The "depth" param in Strassen method is removable. I used in debug.
// ForkJoinPool utilizes work-stealing between the threads.

import java.util.concurrent.*;

public class StrassenMatrixMulti {
    private final int threadCount;
    private static ForkJoinPool forkJoinPool;

    // Constructor
    public StrassenMatrixMulti(int threadCount) {
        this.threadCount = threadCount;
        this.forkJoinPool = new ForkJoinPool(threadCount);
    }

    // The method that starts the recursion. Is publicly available.
    public int[][] multiply(int[][] A, int[][] B) {
        int n = A.length;
        return forkJoinPool.invoke(new StrassenTask(A, B, n, 0));
    }

    // RecursiveTask to handle Strassen's multiplication via forkJoinPool
    // Allows for work steal because otherwise the threads are overworked and stall.
    // Leaving the program to hang on large matrices and small thread counts.
    private class StrassenTask extends RecursiveTask<int[][]> {
        private int[][] A, B;
        private int size, depth;

        public StrassenTask(int[][] A, int[][] B, int size, int depth) {
            this.A = A;
            this.B = B;
            this.size = size;
            this.depth = depth;
        }

        // Overrides RecursiveTasks compute method to start the threads
        @Override
        protected int[][] compute() {
            System.out.println("Recursion depth " + depth + ": Matrix size = " + size);

            // Base case: if the matrix is small enough, perform direct multiplication
            if (size <= 1) {
                System.out.println("Recursion depth " + depth + ": Performing direct multiplication");
                return multiplyDirectly(A, B);  // Direct multiplication for single elements
            }

            // I kept getting weird results but adding a secondary base case fixed it? If it works, it works.
            int newSize = size / 2;
            if (newSize == 1) {
                // This prevents an infinite recursion issue
                System.out.println("Recursion depth " + depth + ": Performing direct multiplication");
                return multiplyDirectly(A, B);  // Base case for recursion, or handle the base case directly
            }

            // Divide matrices into submatrices
            int[][] A11 = new int[newSize][newSize];
            int[][] A12 = new int[newSize][newSize];
            int[][] A21 = new int[newSize][newSize];
            int[][] A22 = new int[newSize][newSize];
            int[][] B11 = new int[newSize][newSize];
            int[][] B12 = new int[newSize][newSize];
            int[][] B21 = new int[newSize][newSize];
            int[][] B22 = new int[newSize][newSize];

            split(A, A11, 0, 0);
            split(A, A12, 0, newSize);
            split(A, A21, newSize, 0);
            split(A, A22, newSize, newSize);
            split(B, B11, 0, 0);
            split(B, B12, 0, newSize);
            split(B, B21, newSize, 0);
            split(B, B22, newSize, newSize);

            System.out.println("Recursion depth " + depth + ": Performing additions and subtractions");

            // Fork parallel tasks for the Strassen 7 main steps
            ForkJoinTask<int[][]> future1 = new StrassenTask(add(A11, A22), add(B11, B22), newSize, depth + 1).fork();
            ForkJoinTask<int[][]> future2 = new StrassenTask(add(A21, A22), B11, newSize, depth + 1).fork();
            ForkJoinTask<int[][]> future3 = new StrassenTask(A11, subtract(B12, B22), newSize, depth + 1).fork();
            ForkJoinTask<int[][]> future4 = new StrassenTask(A22, subtract(B21, B11), newSize, depth + 1).fork();
            ForkJoinTask<int[][]> future5 = new StrassenTask(add(A11, A12), B22, newSize, depth + 1).fork();
            ForkJoinTask<int[][]> future6 = new StrassenTask(subtract(A21, A11), add(B11, B12), newSize, depth + 1).fork();
            ForkJoinTask<int[][]> future7 = new StrassenTask(subtract(A12, A22), add(B21, B22), newSize, depth + 1).fork();

            // Wait for all tasks to complete and collect results
            int[][][] results = new int[7][][];
            try {
                // Join threads
                results[0] = future1.join();
                results[1] = future2.join();
                results[2] = future3.join();
                results[3] = future4.join();
                results[4] = future5.join();
                results[5] = future6.join();
                results[6] = future7.join();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Combine the results for Strassen
            System.out.println("Recursion depth " + depth + ": Combining results");
            int[][] C11 = add(subtract(add(results[0], results[3]), results[4]), results[6]);
            int[][] C12 = add(results[2], results[4]);
            int[][] C21 = add(results[1], results[3]);
            int[][] C22 = add(subtract(add(results[0], results[2]), results[1]), results[5]);

            // Join the submatrices into the final result and original size.
            int[][] result = new int[size][size];
            joinMatrices(result, C11, 0, 0);
            joinMatrices(result, C12, 0, newSize);
            joinMatrices(result, C21, newSize, 0);
            joinMatrices(result, C22, newSize, newSize);

            return result;
        }
    }

    // Direct multiplication for small matrices. Used for base case which is 2x2 because 1x1 is problematic and
    // redundant as they'd be joined together again any ways.
    private int[][] multiplyDirectly(int[][] A, int[][] B) {
        int n = A.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    result[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return result;
    }

    // Utility methods for splitting, adding, subtracting matrices
    private void split(int[][] source, int[][] target, int row, int col) {
        int n = target.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                target[i][j] = source[row + i][col + j];
            }
        }
    }

    private void joinMatrices(int[][] target, int[][] source, int row, int col) {
        int n = source.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                target[row + i][col + j] = source[i][j];
            }
        }
    }

    private int[][] add(int[][] A, int[][] B) {
        int n = A.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = A[i][j] + B[i][j];
            }
        }
        return result;
    }

    private int[][] subtract(int[][] A, int[][] B) {
        int n = A.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = A[i][j] - B[i][j];
            }
        }
        return result;
    }

    // Shutdown the executor when no more work
    public static void shutdownExecutor() {
        if (forkJoinPool != null && !forkJoinPool.isShutdown()) {
            forkJoinPool.shutdown();
            try {
                if (!forkJoinPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    forkJoinPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                forkJoinPool.shutdownNow();
            }
        }
    }
}
