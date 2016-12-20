package paxos;

import com.google.protobuf.InvalidProtocolBufferException;
import utilities.Transport;

import java.util.*;
import paxos.PaxosMsgs.*;

/**
 * Created by yifan on 12/17/16.
 */
public class Acceptor {
    private Ballot curBallot;
    private HashSet<PValue> accepted;
    private Transport transport;
    private List<Integer> knownLeaders;
    private final int ID;

    public Acceptor(int id, Transport transport){
        curBallot = Ballot.newBuilder()
                .setPrefix(0)
                .setConductor(0)
                .build();
        accepted = new HashSet<>();
        this.transport = transport;
        this.ID = id;
        knownLeaders = new ArrayList<>(transport.getLeaders());
    }

    private void foreverRun() {
        while(true){
            byte[] rawBytes = transport.receive();
            try {
                Paxos message = Paxos.parseFrom(rawBytes);
                switch(message.getType()){
                    case P1A:{

                    }
                    case P2A:{

                    }
                }
            } catch (InvalidProtocolBufferException e) {
                System.err.println("Cannot parse message: Paxos");
                e.printStackTrace();
            }
        }
    }
}
