package utilities;

import paxos.PaxosMsgs;

import java.util.List;

/**
 * Not assume FIFO
 *
 * Classes implementing this interface store ID-address mappings,
 * which from the configuration file.
 *
 * Message boundary design -- 1 byte length
 *
 */
public interface Environment {

    public List<Integer> getClients();

    public List<Integer> getReplicas();

    public List<Integer> getLeaders();

    public List<Integer> getAcceptors();

    public void send(int toID, PaxosMsgs.Paxos msg);

    public PaxosMsgs.Paxos receive();

}
