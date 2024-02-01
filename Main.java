import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {
        NodeConfig node_config = ReadConfigFile(args[1]);
        node_config.id = Integer.parseInt(args[0]);
        node_config.config_file_name = args[1];

        SpanningTree.buildSpanningTree(node_config.adj_matrix);

        for (int i = 0; i < node_config.nodes.size(); i++) {
            // System.out.println(node_config.nodes.get(i).port);
            node_config.node_by_id.put(node_config.nodes.get(i).node_id, node_config.nodes.get(i));
        }

        // Create a server socket
        TCPServer server = new TCPServer(node_config);

        new TCPClient(node_config, node_config.id);

        node_config.vector_clock = new int[node_config.num_nodes];
        node_config.resetSnapshotAttributes(node_config);

        if (node_config.id == 0) {
            node_config.active = true;
            new Chandy_Lamport_Thread(node_config).start();
            new SendThread(node_config).start();
        } else {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        server.listen();
    }

    private static NodeConfig ReadConfigFile(String file_name) throws Exception {
        NodeConfig node_config = new NodeConfig();
        int node_count = 0, section = 0;
        // Keeps track of current node
        int current_node = 0;

        String file_path = file_name;

        String line = null;
        try {
            BufferedReader buff = new BufferedReader(new FileReader(file_path));

            while ((line = buff.readLine()) != null) {
                if (line.length() == 0 || line.startsWith("#"))
                    continue;
                // Ignore comments and consider only those lines which are not comments
                String[] config_input;
                if (line.contains("#")) {
                    String[] config_input_comment = line.split("#.*$"); // Ignore text after # symbol
                    config_input = config_input_comment[0].split("\\s+");
                } else {
                    config_input = line.split("\\s+");
                }

                if (section == 0 && config_input.length == 6) {
                    node_config.num_nodes = Integer.parseInt(config_input[0]);
                    node_config.min_per_active = Integer.parseInt(config_input[1]);
                    node_config.max_per_active = Integer.parseInt(config_input[2]);
                    node_config.min_send_delay = Integer.parseInt(config_input[3]);
                    node_config.snapshot_delay = Integer.parseInt(config_input[4]);
                    node_config.max_num = Integer.parseInt(config_input[5]);
                    node_config.adj_matrix = new int[node_config.num_nodes][node_config.num_nodes];
                    section++;
                } else if (section == 1 && node_count < node_config.num_nodes) {
                    System.out.println(config_input[0] + " " + config_input[1] + " " + config_input[2]);
                    node_config.nodes.add(new Node(Integer.parseInt(config_input[0]), config_input[1],
                            Integer.parseInt(config_input[2])));
                    node_count++;
                    if (node_count == node_config.num_nodes) {
                        section = 2;
                    }
                } else if (section == 2) {
                    for (String i : config_input) {
                        if (current_node != Integer.parseInt(i)) {
                            node_config.adj_matrix[current_node][Integer.parseInt(i)] = 1;
                            node_config.adj_matrix[Integer.parseInt(i)][current_node] = 1;
                        }
                    }
                    current_node++;
                }
            }
            buff.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return node_config;
    }
}