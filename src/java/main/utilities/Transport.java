package utilities;

import java.net.InetSocketAddress;
import java.util.HashMap;
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
 * todo undone!
 */
public class Transport {

    private Set<Integer> clients;
    private Set<Integer> replicas;
    private Set<Integer> leaders;
    private Set<Integer> acceptors;

    private HashMap<Integer, InetSocketAddress> addressMap;

    public Set<Integer> getClients() {
        return clients;
    }

    public Set<Integer> getReplicas() {
        return replicas;
    }

    public Set<Integer> getLeaders() {
        return leaders;
    }

    public Set<Integer> getAcceptors() {
        return acceptors;
    }

    public void send(int toID, byte[] msg){

    }

    public byte[] receive(){
        byte[] ret = null;
        return ret;
    }

}
