//Thread to start chandy lamport protocol
public class Chandy_Lamport_Thread extends Thread {

	NodeConfig node_config;

	public Chandy_Lamport_Thread(NodeConfig node_config) {
		this.node_config = node_config;
	}

	public void run() {
		if (node_config.first_snapshot) {
			node_config.first_snapshot = false;
		} else {
			try {
				Thread.sleep(node_config.snapshot_delay);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// System.out.println("Take new snapshot");
		Chandy_Lamport_Protocol.startCLP(node_config);
	}
}
