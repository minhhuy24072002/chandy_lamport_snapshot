import java.io.*;
import java.net.*;

public class ReceiveThread extends Thread {
    final Socket connection_socket;
    private NodeConfig node_config;

    public ReceiveThread(Socket connection_socket, NodeConfig node_config) {
        this.connection_socket = connection_socket;
        this.node_config = node_config;
    }

    @Override
    public void run() {
        // Input stream
        ObjectInputStream input_from_client = null;
        try {
            input_from_client = new ObjectInputStream(connection_socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                // System.out.println("Node " + node_config.id + " is waiting");
                Message message = (Message) input_from_client.readObject();
                synchronized (node_config) {
                    System.out.println("Node " + node_config.id + " received message value: " + message.str
                            + " from node " + message.node_id);

                    if (message.str.contains("MarkerMessage")) {
                        // Send marker messages to neighbors
                        Chandy_Lamport_Protocol.sendMarkerMessage(node_config, message.node_id);
                    } else if (message.str.contains("ApplicationMessage")) {
                        if (node_config.color.equals("RED")) {
                            // Record channel state when the node is currently RED
                            int incoming_channel_id = message.node_id;
                            Chandy_Lamport_Protocol.recordChannelState(incoming_channel_id, message,
                                    node_config);
                        }

                        if (!message.active && node_config.num_message_sent < node_config.max_num) {
                            // Send new application messages
                            node_config.active = true;
                            new SendThread(node_config).start();
                        }
                    } else if (message.str.contains("ConvergeCastMessage")) {
                        if (node_config.id == 0) {
                            node_config.convergecast_message.put(message.node_id, message);
                            System.out.println("Received ConvergeCast Message from " + message.node_id);
                            for (Integer i : message.vector_clock) {
                                System.out.print(i + " ");
                            }
                            System.out.println();
                            node_config.convergecast_message_received[message.node_id] = true;
                            if (node_config.convergecast_message.size() == node_config.num_nodes) {
                                System.out.println("Check termination");
                                // Check for termination or take a new snapshot
                                boolean is_terminated = Chandy_Lamport_Protocol.detectTermination(node_config,
                                        message);
                                if (!is_terminated) {
                                    System.out.println("Not terminated");
                                    node_config.resetSnapshotAttributes(node_config);
                                    new Chandy_Lamport_Thread(node_config).start();
                                }
                            }
                        } else {
                            Chandy_Lamport_Protocol.sendToParent(node_config, message);
                        }
                    } else if (message.str.contains("TerminateMessage")) {
                        // Tell neighbor nodes to halt their execution
                        Chandy_Lamport_Protocol.sendTerminateMessage(node_config);
                    }

                    if (message.str.contains("ApplicationMessage")) {
                        // Update vector clock
                        for (int i = 0; i < node_config.num_nodes; i++) {
                            node_config.vector_clock[i] = Math.max(node_config.vector_clock[i],
                                    message.vector_clock[i]);
                        }
                        node_config.vector_clock[node_config.id]++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}