package utilities;

import java.util.List;

/**
 * This interface provide network abstraction.
 * Except for methods in this interface, the whole system know nothing about network.
 *
 * Not assume FIFO, only reliability.
 *
 * Classes implementing this interface store ID-address mappings,
 * which from the config file.
 *
 * Message boundary design -- 1 byte length
 *
 */
public interface Environment {

    public List<Integer> getReplicas();

    public List<Integer> getLeaders();

    public List<Integer> getAcceptors();

    public void send(int toID, PaxosMsgs.Paxos msg);

    public PaxosMsgs.Paxos receive(int id);

}
