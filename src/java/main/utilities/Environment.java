package utilities;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by yifan on 12/19/16.
 *
 * Not assume FIFO
 *
 * Need a blocking queue
 * Need a listener thread
 *
 * Processes only have a single thread, except for leader.
 * There are three types of threads in leader. Connection handler of leader need to know it is in a leader:
 * each connection handler distributed message to three? blocking queue.
 *
 * Store ID-address mapping, directly from configuration file.
 *
 * Message boundary design -- 1 byte length
 *
 */
public interface Environment {

    public List<Integer> getClients();

    public List<Integer> getReplicas();

    public List<Integer> getLeaders();

    public List<Integer> getAcceptors();

    public void send(int toID, byte[] msg);

    public byte[] receive();

}
