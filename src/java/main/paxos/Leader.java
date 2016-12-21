package paxos;

import paxos.PaxosMsgs.*;
import utilities.Transport;

import java.util.*;

/**
 * Created by yifan on 12/17/16.
 *
 * todo design concurrent structure
 */
public class Leader {
    private Ballot ballot;
    private boolean active;
    private Set<Propose> proposals;
    private Transport transport;

    private List<Integer> knownReplicas;
    private List<Integer> knownAcceptors;

    private final int ID;

    public Leader(int id, Transport transport) {
        ID = id;
        transport = transport;
        ballot = Ballot.newBuilder().setPrefix(0).setConductor(id).build();
        active = false;
        proposals = new HashSet<>();
    }

    public void run() {

    }

    private void forever(){

    }

    /**
     * todo message queue
     * different threads consume different types of message.
     * maybe need an extra msg distributing thread?
     */
}
