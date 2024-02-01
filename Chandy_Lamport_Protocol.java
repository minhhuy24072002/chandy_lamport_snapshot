import java.io.*;
import java.util.*;

public class Chandy_Lamport_Protocol {

	public static void startCLP(NodeConfig node_config) {
		synchronized (node_config) {
			System.out.println("Start new snapshot");
			node_config.convergecast_message_received[node_config.id] = true;
			sendMarkerMessage(node_config, node_config.id);
		}
	}

	public static void sendMarkerMessage(NodeConfig node_config, int incoming_channel_id) {
		synchronized (node_config) {
			if (node_config.color.equals("BLUE")) {
				// Node turns RED if it is currently BLUE
				System.out.println("Node " + node_config.id + " turned RED");
				node_config.color = "RED";
				// Record the current local state (just after it turns red) for the global
				// snapshot
				node_config.current_state.active = node_config.active;
				node_config.current_state.node_id = node_config.id;
				int[] vector_clockCopy = new int[node_config.vector_clock.length];
				for (int i = 0; i < vector_clockCopy.length; i++) {
					vector_clockCopy[i] = node_config.vector_clock[i];
				}
				node_config.global_snapshot.add(vector_clockCopy);
				node_config.current_state.vector_clock = vector_clockCopy;

				// Send marker messages to all its neighbors
				for (int i : node_config.neighbors) {
					Message message = new Message();
					message.str = "MarkerMessage";
					message.node_id = node_config.id;
					ObjectOutputStream oos = node_config.o_stream.get(i);
					System.out.println("Sent MARKER Message from node " + node_config.id + " to node " + i);
					try {
						oos.writeObject((Message) message);
						oos.flush();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			// Finished recording incoming channel state
			node_config.received_maker_from_channel.put(incoming_channel_id, true);

			// Check if the current node has received marker messages from all channels
			int channel = 0;
			while (channel < node_config.neighbors.size()
					&& node_config.received_maker_from_channel.get(node_config.neighbors.get(channel)) == true) {
				channel++;
			}
			if (channel == node_config.neighbors.size()) {
				if (node_config.id != 0) {
					int parent = SpanningTree.getParent(node_config.id);
					// Record the channel state and foward ConvergeCast message (with final
					// destination is node 0) to parent
					node_config.current_state.channel_state = node_config.channel_state;
					node_config.current_state.str = "ConvergeCastMessage" + node_config.global_snapshot.size();
					node_config.color = "BLUE";
					// System.out.println("Parent of node " + node_config.id + "is " + parent);
					System.out.println("Node " + node_config.id + " turned BLUE");
					System.out.println("Sent ConvergeCast Message from node " + node_config.id + " to node " + parent);
					System.out.print("ConvergeCast Message vector clock is ");
					for (Integer i: node_config.current_state.vector_clock) {
						System.out.print(i + " ");
					}
					System.out.println();
					ObjectOutputStream oos = node_config.o_stream.get(parent);
					try {
						// System.out.println("Current state object is: " + node_config.current_state);
						oos.writeObject((Message) node_config.current_state);
						oos.flush();
					} catch (Exception e) {
						e.printStackTrace();
					}
					node_config.resetSnapshotAttributes(node_config);
				} else {
					// Reset node 0 has receives if it has received all marker messages
					node_config.current_state.channel_state = node_config.channel_state;
					node_config.convergecast_message.put(node_config.id, node_config.current_state);
					node_config.color = "BLUE";
				}
			}
		}
	}

	public static boolean checkSnapshotConsistent(ArrayList<int[]> local_states) {
		System.out.println("Local snapshots from all process: ")
		for (int i = 0; i < local_states.size(); i++) {
			for (int entry = 0; entry < local_states.get(i).length; entry++) {
				System.out.print(local_states.get(i)[entry] + " ");
			}
			System.out.println();
		}

		int[] vector_max = new int[local_states.get(0).length];
		for (int i = 0; i < local_states.get(0).length; i++) {
			vector_max[i] = local_states.get(0)[i];
		} 

		for (int i = 0; i < local_states.size(); i++) {
			for (int entry = 0; entry < local_states.get(i).length; entry++) {
				vector_max[entry] = Math.max(vector_max[entry], local_states.get(i)[entry]);
			}
		}

		// for (int i = 0; i < local_states.get(0).length; i++) {
		// 	System.out.print(vector_max[i] + " ");
		// }
		// System.out.println();

		for (int i = 0; i < local_states.size(); i++) {
			if (vector_max[i] > local_states.get(i)[i]) {
				return false;
			}
		}

		return true;
	}

	public static boolean detectTermination(NodeConfig node_config, Message message) throws Exception {
		int channel = 0, state = 0, node = 0;
		synchronized (node_config) {
			// Check if node 0 has received ConvergeCast message from all the nodes
			while (node < node_config.convergecast_message_received.length
					&& node_config.convergecast_message_received[node] == true) {
				node++;
			}
			if (node != node_config.convergecast_message_received.length) {
				return false;
			}
			System.out.println("Received all state message");

			// Check if the global snapshot is consistent
			ArrayList<int[]> local_states = new ArrayList<int[]>();
			for (state = 0; state < node_config.convergecast_message.size(); state++) {
					local_states.add(node_config.convergecast_message.get(state).vector_clock);
			}
			if (checkSnapshotConsistent(local_states)) {
				System.out.println("Global snapshot is consistent");
			}
			
			// Check if any process is active
			for (state = 0; state < node_config.convergecast_message.size(); state++) {
				if (node_config.convergecast_message.get(state).active == true) {
					return false;
				}
			}
			System.out.println("All nodes are inactive");

			// Check if all channels are empty
			for (channel = 0; channel < node_config.num_nodes; channel++) {
				Message value = node_config.convergecast_message.get(channel);
				for (ArrayList<Message> channel_state : value.channel_state.values()) {
					if (!channel_state.isEmpty()) {
						return false;
					}
				}
			}
			System.out.println("All channels are empty");

			// If channels are empty and nodes are passive then send terminate message to
			// all nodes for
			// termination
			sendTerminateMessage(node_config);
		}
		return true;
	}

	public static void recordChannelState(int incoming_channel_id, Message message, NodeConfig node_config) {
		synchronized (node_config) {
			if (node_config.received_maker_from_channel.get(incoming_channel_id) == false) {
				if ((node_config.channel_state.get(incoming_channel_id).isEmpty())) {
					ArrayList<Message> message_list = node_config.channel_state.get(incoming_channel_id);
					message_list.add(message);
					node_config.channel_state.put(incoming_channel_id, message_list);
				} else if (!(node_config.channel_state.get(incoming_channel_id).isEmpty())) {
					node_config.channel_state.get(incoming_channel_id).add(message);
				}
				System.out.println("Node " + node_config.id + " save ApplicationMessage from node " + message.node_id);
			}
		}
	}

	public static void sendToParent(NodeConfig node_config, Message message) {
		synchronized (node_config) {
			int parent = SpanningTree.getParent(node_config.id);
			ObjectOutputStream oos = node_config.o_stream.get(parent);
			System.out.println("Forwarded State Message from node " + message.node_id + " to node " + parent);
			try {
				oos.writeObject((Message) message);
				oos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void sendTerminateMessage(NodeConfig node_config) {
		synchronized (node_config) {
			new Output(node_config).printSnapshotsToFile();
			for (int neighbor : node_config.neighbors) {
				Message message = new Message();
				message.str = "TerminateMessage";
				ObjectOutputStream oos = node_config.o_stream.get(neighbor);
				System.out.println("Sent Terminate Message from node " + node_config.id + " to node " + neighbor);
				try {
					oos.writeObject((Message) message);
					oos.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("Node : " + node_config.id + " - Successfully written to output file");
			System.exit(0);
		}
	}
}
