import java.net.*;

public class TCPServer {
    int server_port;
    Socket connection_socket = null;
    ServerSocket listener = null;
    private NodeConfig node_config;

    public TCPServer(NodeConfig node_config) {
        server_port = node_config.node_by_id.get(node_config.id).port;
        this.node_config = node_config;
        try {
            listener = new ServerSocket(server_port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        try {
            while (true) {
                try {
                    // Create new socket associates with the new connection request from client
                    connection_socket = listener.accept();
                    // Create new handler thread for the new client
                    Thread new_server_thread = new ReceiveThread(connection_socket, node_config);
                    new_server_thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            try {
                listener.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}