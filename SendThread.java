import java.io.*;
import java.util.*;

public class SendThread extends Thread {
    NodeConfig node_config;

    public SendThread(NodeConfig node_config) {
        this.node_config = node_config;
    }

    void sendMessage() throws Exception {
        // Get a random number between min_per_active to max_per_active
        int rand_num_messages = 1;
        int min_send_delay = 0;
        synchronized (node_config) {
            rand_num_messages = this.getRandomNumber(node_config.min_per_active,
                    node_config.max_per_active);
            min_send_delay = node_config.min_send_delay;
        }
        for (int i = 0; i < rand_num_messages; i++) {
            synchronized (node_config) {
                // Get a random neighbour
                int rand_index = this.getRandomNumber(0, node_config.neighbors.size() - 1);
                int neighbor_id = node_config.neighbors.get(rand_index);

                if (node_config.active == true) {
                    // Create new message
                    Message message = new Message();
                    message.str = "ApplicationMessage";
                    node_config.vector_clock[node_config.id]++;
                    message.vector_clock = new int[node_config.vector_clock.length];
                    System.arraycopy(node_config.vector_clock, 0, message.vector_clock, 0,
                            node_config.vector_clock.length);
                    message.node_id = node_config.id;
                    // Send object data to the neighbor
                    try {
                        ObjectOutputStream oos = node_config.o_stream.get(neighbor_id);
                        System.out.println("Sent from node " + node_config.id + " to node " + neighbor_id
                                + ": Application Message " + node_config.num_message_sent);

                        oos.writeObject(message);
                        oos.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // Increment num_message_sent
                    node_config.num_message_sent++;
                }

                // Wait for minimum sending delay before sending another message
                try {
                    Thread.sleep(min_send_delay);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        synchronized (node_config) {
            node_config.active = false;
        }
    }

    @Override
    public void run() {
        try {
            this.sendMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function to generate random number in a given range
    int getRandomNumber(int min, int max) {
        Random rand = new Random();
        int random_num = rand.nextInt((max - min) + 1) + min;
        return random_num;
    }
}
