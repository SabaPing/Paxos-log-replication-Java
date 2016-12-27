package paxos;

import com.google.protobuf.InvalidProtocolBufferException;
import utilities.Environment;

import java.util.*;
import paxos.PaxosMsgs.*;

import static paxos.PaxosMsgs.Paxos.Type.*;

/**
 * Created by yifan on 12/17/16.
 *
 * If process is an acceptor, it only has a single thread.
 */
public class Acceptor extends Thread{
    private Ballot curBallot;
    private final Set<PValue> accepted;
    private final Environment environment;
    private final int ID;

    public Acceptor(int id, Environment environment){
        curBallot = Ballot.newBuilder()
                .setPrefix(0)
                .setConductor(0)
                .build();
        accepted = new HashSet<>();
        this.environment = environment;
        this.ID = id;
    }

    @Override
    public void run() {
        while(true){
            byte[] rawBytes = environment.receive();
            try {
                Paxos message = Paxos.parseFrom(rawBytes);
                switch(message.getType()){
                    case P1A:{
                        P1a body = message.getP1A();
                        if (new BallotComparator().compare(body.getBallot(), curBallot) == 1)
                            curBallot = body.getBallot();
                        environment.send(body.getFromLeader(),
                                Paxos.newBuilder()
                                .setType(P1B)
                                .setP1B(P1b.newBuilder()
                                    .setFrom(ID)
                                    .setToScout(body.getFromScout())
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
                        environment.send(body.getFromLeader(),
                                Paxos.newBuilder()
                                        .setType(P2B)
                                        .setP2B(P2b.newBuilder()
                                                .setFrom(ID)
                                                .setToCommander(body.getFromCommander())
                                                .setBallot(curBallot))
                                        .build().toByteArray());
                        break;
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                System.err.println("Acceptor " + ID + ": cannot parse message.");
                e.printStackTrace();
            }
        }
    }
}
