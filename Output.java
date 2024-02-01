import java.io.*;

//Print the global_snapshot to the output File
public class Output {
	NodeConfig node_config;

	public Output(NodeConfig node_config) {
		this.node_config = node_config;
	}

	public void printSnapshotsToFile() {
		String output_prefix = node_config.config_file_name.substring(0, node_config.config_file_name.lastIndexOf('.'));
		String file_name = output_prefix + "-" + node_config.id + ".out";
		synchronized (node_config.global_snapshot) {
			try {
				File file = new File(file_name);
				FileWriter out_file;
				if (file.exists()) {
					out_file = new FileWriter(file, true);
				} else {
					out_file = new FileWriter(file);
				}
				BufferedWriter buff_reader = new BufferedWriter(out_file);

				for (int i = 0; i < node_config.global_snapshot.size(); i++) {
					for (int j : node_config.global_snapshot.get(i)) {
						buff_reader.write(j + " ");

					}
					if (i < (node_config.global_snapshot.size() - 1)) {
						buff_reader.write("\n");
					}
				}
				node_config.global_snapshot.clear();
				buff_reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
