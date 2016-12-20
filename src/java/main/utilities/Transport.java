package utilities;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by yifan on 12/19/16.
 *
 * Not assume FIFO
 * Need a blocking queue
 * Need a listener thread
 * Store ID-address mapping, directly from configuration file.
 *
 * todo undone!
 */
public class Transport {

    private Set<Integer> clients;
    private Set<Integer> replicas;
    private Set<Integer> leaders;
    private Set<Integer> accepter;

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

    public Set<Integer> getAccepter() {
        return accepter;
    }

    public void send(int toID, byte[] msg){

    }

    public byte[] receive(){
        byte[] ret = null;
        return ret;
    }

}
