import java.io.*;
import java.net.*;

public class TCPClient {
    public TCPClient(NodeConfig node_config, int current_node) {
        for (int i = 0; i < node_config.num_nodes; i++) {
            if (node_config.adj_matrix[current_node][i] == 1) {
                String host_name = node_config.node_by_id.get(i).host;
                int port = node_config.node_by_id.get(i).port;
                // System.out.println("Current_node and port " + current_node + " X " + port);
                InetAddress address = null;
                try {
                    address = InetAddress.getByName(host_name);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                Socket client_socket = null;
                try {
                    client_socket = new Socket(address, port);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Send client request to all neighboring nodes
                node_config.channels.put(i, client_socket);
                node_config.neighbors.add(i);
                ObjectOutputStream outToServer = null;
                try {
                    outToServer = new ObjectOutputStream(client_socket.getOutputStream());
                } catch (Exception e) {

                    e.printStackTrace();
                }
                node_config.o_stream.put(i, outToServer);
            }
        }
    }
}
