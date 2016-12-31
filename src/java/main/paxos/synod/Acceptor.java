package paxos.synod;

import utilities.BallotComparator;
import utilities.Environment;

import java.util.*;

import utilities.PaxosMsgs.*;

import static utilities.PaxosMsgs.Paxos.Type.*;

/**
 * If process is an acceptor, it only has a single thread.
 */
public class Acceptor extends Thread {

    //Paxos related states
    private Ballot acceptorBallot;
    private final Set<PValue> accepted;
    private final int ID;

    //For sending and receiving messages
    private final Environment environment;

    //Initial states
    public Acceptor(int id, Environment environment) {
        acceptorBallot = Ballot.newBuilder()
                .setPrefix(0)
                .setConductor(0)
                .build();
        accepted = new HashSet<>();
        this.environment = environment;
        this.ID = id;
    }

    @Override
    public void run() {
        while (true) {
            //For each message received, do something
            Paxos message = environment.receive(ID);

            //Acceptor can receive two types of messages
            switch (message.getType()) {
                //received a p1a message from a scout
                case P1A: {
                    P1a body = message.getP1A();

                    //if received ballot is larger than acceptor's ballot
                    if (new BallotComparator().compare(body.getBallot(), acceptorBallot) == 1)
                        acceptorBallot = body.getBallot();

                    // reply to the scout
                    environment.send(body.getFromLeader(),
                            Paxos.newBuilder()
                                    .setType(P1B)
                                    .setP1B(P1b.newBuilder()
                                            .setFrom(ID)
                                            .setToScout(body.getFromScout())
                                            .setBallot(acceptorBallot)
                                            .setAccepted(Accepted.newBuilder()
                                                    .addAllPvalue(accepted)))
                                    .build());
                    break;
                }
                //p2a from a commander, code almost the same
                case P2A: {
                    P2a body = message.getP2A();
                    if (new BallotComparator().compare(body.getPvalue().getBallot(), acceptorBallot) == 0)
                        accepted.add(body.getPvalue());

                    environment.send(body.getFromLeader(),
                            Paxos.newBuilder()
                                    .setType(P2B)
                                    .setP2B(P2b.newBuilder()
                                            .setFrom(ID)
                                            .setToCommander(body.getFromCommander())
                                            .setBallot(acceptorBallot))
                                    .build());
                    break;
                }
            }
        }
    }
}
