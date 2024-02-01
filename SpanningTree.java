import java.util.*;

public class SpanningTree {
    static int[] parent;

    public static int getParent(int node_id) {
        return parent[node_id];
    }

    static void buildSpanningTree(int[][] adj_matrix) {
        boolean[] visited = new boolean[adj_matrix.length];
        parent = new int[adj_matrix.length];
        Queue<Integer> queue = new LinkedList<Integer>();
        queue.add(0);
        parent[0] = 0;
        visited[0] = true;

        while (!queue.isEmpty()) {
            int current_node = queue.remove();

            for (int i = 0; i < adj_matrix[current_node].length; i++) {
                if (adj_matrix[current_node][i] == 1 && !visited[i]) {
                    queue.add(i);
                    visited[i] = true;
                    parent[i] = current_node;
                }
            }
        }

        for (int i = 0; i < adj_matrix.length; i++) {
            System.out.println("Parent of " + i + " is " + parent[i]);
        }
    }
}
