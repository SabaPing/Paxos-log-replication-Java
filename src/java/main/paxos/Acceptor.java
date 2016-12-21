package paxos;

import com.google.protobuf.InvalidProtocolBufferException;
import utilities.Transport;

import java.util.*;
import paxos.PaxosMsgs.*;

import static paxos.PaxosMsgs.Paxos.Type.*;

/**
 * Created by yifan on 12/17/16.
 *
 * If process is an acceptor, it only has a single thread.
 */
public class Acceptor {
    private Ballot curBallot;
    Set<PValue> accepted;
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

    public void run() {
        while(true){
            byte[] rawBytes = transport.receive();
            try {
                Paxos message = Paxos.parseFrom(rawBytes);
                switch(message.getType()){
                    case P1A:{
                        P1a body = message.getP1A();
                        if (new BallotComparator().compare(body.getBallot(), curBallot) == 1)
                            curBallot = body.getBallot();
                        transport.send(body.getFrom(),
                                Paxos.newBuilder()
                                .setType(P1B)
                                .setP1B(P1b.newBuilder()
                                    .setFrom(ID)
                                    .setBallot(curBallot)
                                    .setAccepted(Accepted.newBuilder()
                                            .addAllPvalue(accepted)))
                                .build().toByteArray());
                        break;
                    }
                    case P2A:{
                        P2a body = message.getP2A();
                        if (new BallotComparator().compare(body.getPvalue().getBallot(), curBallot) == 0)
                            //assume protobuf library override equals() correctly!
                            accepted.add(body.getPvalue());
                        transport.send(body.getFrom(),
                                Paxos.newBuilder()
                                        .setType(P2B)
                                        .setP2B(P2b.newBuilder()
                                                .setFrom(ID)
                                                .setBallot(curBallot))
                                        .build().toByteArray());
                        break;
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                System.err.println("Cannot parse message: Paxos");
                e.printStackTrace();
            }
        }
    }
}
