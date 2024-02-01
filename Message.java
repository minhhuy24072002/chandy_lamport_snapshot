import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Message implements Serializable {
    String str;
    int node_id;
    int[] vector_clock;
    boolean active;
    HashMap<Integer, ArrayList<Message>> channel_state;
}
