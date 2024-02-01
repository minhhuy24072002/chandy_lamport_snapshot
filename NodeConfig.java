import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class NodeConfig implements Serializable {
	// Node attributes
	int id;
	int[][] adj_matrix;
	ArrayList<Integer> neighbors;
	boolean active;
	int num_message_sent;
	int[] vector_clock;
	String config_file_name;

	// Config attributes
	int num_nodes;
	int min_per_active;
	int max_per_active;
	int min_send_delay;
	int snapshot_delay;
	int max_num;

	// Node's Chandy Lamport Protocol attributes
	String color;
	boolean first_snapshot;
	HashMap<Integer, ArrayList<Message>> channel_state;
	HashMap<Integer, Boolean> received_maker_from_channel;
	Message current_state;
	HashMap<Integer, Message> convergecast_message;
	boolean[] convergecast_message_received;
	ArrayList<int[]> global_snapshot;

	// HashMap to get from node id to Node object
	HashMap<Integer, Node> node_by_id;

	// ArrayList to store all nodes' info
	ArrayList<Node> nodes;

	// HashMap to get from node id to node's listening socket
	HashMap<Integer, Socket> channels;

	// HashMap to get from node id to ObjectOutputStream to the node
	HashMap<Integer, ObjectOutputStream> o_stream;

	// Initialize node config
	public NodeConfig() {
		color = "BLUE";
		first_snapshot = true;
		num_message_sent = 0;
		global_snapshot = new ArrayList<int[]>();
		active = false;
		neighbors = new ArrayList<>();
		nodes = new ArrayList<Node>();
		node_by_id = new HashMap<Integer, Node>();
		channels = new HashMap<Integer, Socket>();
		o_stream = new HashMap<Integer, ObjectOutputStream>();
	}

	// Reset Node's Chandy Lamport Protocol attributes before taking snapshot
	void resetSnapshotAttributes(NodeConfig NodeConfig) {
		NodeConfig.channel_state = new HashMap<Integer, ArrayList<Message>>();
		NodeConfig.received_maker_from_channel = new HashMap<Integer, Boolean>();
		NodeConfig.convergecast_message = new HashMap<Integer, Message>();

		for (Integer e : NodeConfig.channels.keySet()) {
			ArrayList<Message> array_list = new ArrayList<Message>();
			NodeConfig.channel_state.put(e, array_list);
		}

		for (Integer e : NodeConfig.neighbors) {
			// System.out.println("Neighbor is : " + e);
			NodeConfig.received_maker_from_channel.put(e, false);
		}

		NodeConfig.convergecast_message_received = new boolean[NodeConfig.num_nodes];
		NodeConfig.current_state = new Message();
		NodeConfig.current_state.vector_clock = new int[NodeConfig.num_nodes];
	}
}
