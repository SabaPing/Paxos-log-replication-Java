package paxos.synod;

import utilities.BallotComparator;
import utilities.PaxosMsgs.*;
import utilities.Environment;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import static utilities.PaxosMsgs.Paxos.Type.*;


/**
 * The Leader class has two Paxos related inner classes, which are Scout and Commander.
 * The leader is the main thread. Scouts and Commanders are launched by the leader.
 * Use inner classes to simplify state sharing.
 *
 * Inter threads communication is done by message queues and MessageHandler thread.
 * For one-to-one mapping between each thread and its message queue, each thread need a local id,
 * Use ThreadLocal to implement thread local id.
 */
public class Leader extends Thread{

    //Paxos related states, no one is shared, thus thread safe.
    private Ballot leaderBallot;
    private final Map<Integer, Command> proposals;
    private final int ID;
    private boolean active;

    //Message handler thread
    private final MessageHandler msgHandler;

    //shared states for inter-threads communication, need thread-safe mechanism
    private final Map<Integer, BlockingQueue<Paxos>> messageQueues;
    private final BlockingQueue<Paxos> incomingMessages;

    //sending and receiving messages
    private final Environment environment;

    public Leader(int id, Environment environment) {
        ID = id;
        leaderBallot = Ballot.newBuilder().setPrefix(0).setConductor(id).build();
        active = false;
        proposals = new HashMap<>();

        this.environment = environment;

        //to avoid this reference escape, don's start inner class thread in constructor
        msgHandler = new MessageHandler(ThreadID.get());

        //initial and register message queue
        messageQueues = new ConcurrentHashMap<>();
        this.incomingMessages = new ArrayBlockingQueue<>(100);
        messageQueues.putIfAbsent(ThreadID.get(), this.incomingMessages);
    }

    @Override
    public void run() {
        msgHandler.start();

        //To ensure states is not shared, build a new ballot
        new Scout(Ballot.newBuilder(leaderBallot).build()).start();

        try {
            while (true)
                forever();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //leader never end. not necessary to de-register its message Q.
        }
    }

    /**
     * this method transit shared states, need careful concurrent design
     * @throws InterruptedException
     */
    private void forever() throws InterruptedException{
        Paxos incMsg = incomingMessages.take();
        switch (incMsg.getType()) {
            case PROPOSE: {
                Propose temp_propose = incMsg.getPropose();
                if (!proposals.containsKey(temp_propose.getSlotIn())){
                    proposals.put(temp_propose.getSlotIn(), temp_propose.getC());
                    if (active) {
                        /**
                         * spawn a Commander
                         * have some concerns! Is protobuf really immutable???
                         * Assume it is!
                         */
                        new Commander(PValue.newBuilder()
                                .setBallot(leaderBallot)
                                .setSlotNum(temp_propose.getSlotIn())
                                .setCmd(temp_propose.getC())
                                .build()).start();
                    }
                }
            }
            case ADOPTED: {
                Adopted temp_adopted = incMsg.getAdopted();
                if (leaderBallot.equals(temp_adopted.getBallot())){
                    /**
                     * updates the set of proposals, replacing for each slot number
                     * the command corresponding to the maximum pvalue in pvals, if any.
                     */
                    Map<Integer, Ballot> pmax = new HashMap<>();
                    for(PValue pv : temp_adopted.getPvalues().getPvalueList()){
                        if(!pmax.containsKey(pv.getSlotNum()) ||
                                new BallotComparator().compare(pmax.get(pv.getSlotNum()), pv.getBallot()) == -1)
                            proposals.put(pv.getSlotNum(), pv.getCmd());
                    }
                    for(Map.Entry<Integer, Command> entry : proposals.entrySet()) {
                        PValue temp_pv = PValue.newBuilder()
                                .setBallot(leaderBallot)
                                .setSlotNum(entry.getKey())
                                .setCmd(entry.getValue())
                                .build();
                        new Commander(temp_pv).start();
                    }
                    active = true;
                }
            }
            case PREEMPTED: {
                Preempted temp_preempted = incMsg.getPreempted();
                if (new BallotComparator().compare(temp_preempted.getBallot(), leaderBallot) == 1) {
                    active = false;
                    leaderBallot = Ballot.newBuilder()
                            .setPrefix(temp_preempted.getBallot().getPrefix() + 1)
                            .setConductor(ID)
                            .build();
                    //ensure states are not shared
                    new Scout(Ballot.newBuilder(leaderBallot).build()).start();
                }
            }
        }
    }

    /**
     * 这里scouts由leader id + leaderBallot 唯一确定，commanders由leader+leaderBallot+slot唯一确定。
     * 对每个不同的scout和commander threads，里面都要有个blocking Q。
     * MessageHandler 必须知道msg type，leaderBallot，和proposal，把对应的msg put到对应的Q里。
     * 以上保证了reliable。
     * 注意：见paper笔记。需要在msg里加额外的from，to fields
     * And give scout and commander 一个额外的 int id, use ThreadLocal
     */
    private class MessageHandler extends Thread{

        /**
         * message handler must know its leader's id.
         * No need to know scouts and commanders, their IDs are in message body.
         */
        private final int leadLocalId;

        public MessageHandler (int leadLocalId) {
            this.leadLocalId = leadLocalId;
        }

        @Override
        public void run() {
            while(true) {

                //This statement will be blocked if there is no incoming message.
                Paxos message = environment.receive();

                try {
                    switch (message.getType()) {
                        case PROPOSE: {
                            messageQueues.get(leadLocalId).put(message);
                            break;
                        }
                        case P1B: {
                            int scoutId = message.getP1B().getToScout();
                            messageQueues.get(scoutId).put(message);
                        }
                        case P2B: {
                            int commanderId = message.getP2B().getToCommander();
                            messageQueues.get(commanderId).put(message);
                        }
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * inner class Scout
     * inter-thread communication -- use message queue
     * do synod phase 1
     */
    private class Scout extends Thread{

        //Paxos related states, must ensure not shared.
        private final Set<PValue> pValueSet;
        private final Ballot scoutBallot;
        private final List<Integer> waitforAccetpors;

        private final BlockingQueue<Paxos> incomingMessages;

        public Scout(Ballot ballot) {
            scoutBallot = ballot;
            pValueSet = new HashSet<>();
            this.incomingMessages = new ArrayBlockingQueue<>(100);
            messageQueues.putIfAbsent(ThreadID.get(), this.incomingMessages);
            waitforAccetpors = new ArrayList<>(environment.getAcceptors());
        }

        @Override
        public void run() {

            Paxos p1a = Paxos.newBuilder()
                    .setType(P1A)
                    .setP1A(P1a.newBuilder()
                        .setFromLeader(ID)
                        .setFromScout(ThreadID.get())
                        .setBallot(scoutBallot))
                    .build();

            for(int acceptor : waitforAccetpors){
                environment.send(acceptor, p1a);
            }

            try {
                while(true){

                    //because of message handler, scout can only get p1b msg
                    P1b incMsg = incomingMessages.take().getP1B();

                    if(incMsg.getBallot().equals(scoutBallot)){
                        pValueSet.addAll(incMsg.getAccepted().getPvalueList());
                        //use Integer.valueOf to use internal cache
                        waitforAccetpors.remove(Integer.valueOf(incMsg.getFrom()));

                        if(waitforAccetpors.size() < environment.getAcceptors().size()/2){
                            //偷懒实现了，由于inner class知道leader的Q。。。
                            Leader.this.incomingMessages.put(Paxos.newBuilder()
                                    .setType(ADOPTED)
                                    .setAdopted(Adopted.newBuilder()
                                        .setBallot(scoutBallot)
                                        .setPvalues(Accepted.newBuilder()
                                            .addAllPvalue(pValueSet)))
                                    .build());
                            //thread exit
                            return;
                        }
                    } else {
                        Leader.this.incomingMessages.put(Paxos.newBuilder()
                                .setType(PREEMPTED)
                                .setPreempted(Preempted.newBuilder()
                                        .setBallot(incMsg.getBallot()))
                                .build());
                        return;
                    }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                //clean up, remove its message queue from map.
                messageQueues.remove(ThreadID.get());
            }
        }
    }

    /**
     * inner class Commander
     * do synod phase 2
     */
    private class Commander extends Thread{
        private final List<Integer> waitforAccetpors;
        private final PValue pvalue;

        private final BlockingQueue<Paxos> incomingMessages;

        public Commander(PValue pvalue) {
            this.pvalue = pvalue;
            waitforAccetpors = new ArrayList<>(environment.getAcceptors());
            incomingMessages = new ArrayBlockingQueue<>(100);
            messageQueues.putIfAbsent(ThreadID.get(), this.incomingMessages);
        }

        @Override
        public void run() {
            Paxos p2a = Paxos.newBuilder()
                    .setType(P2A)
                    .setP2A(P2a.newBuilder()
                            .setFromLeader(ID)
                            .setFromCommander(ThreadID.get())
                            .setPvalue(pvalue))
                    .build();

            for(int acceptor : waitforAccetpors){
                //de-register its message queue
                environment.send(acceptor, p2a);
            }

            try {
               while(true){
                    //because of message handler, scout can only get p2b msg
                    P2b incMsg = incomingMessages.take().getP2B();

                    if(incMsg.getBallot().equals(pvalue.getBallot())){
                        //use Integer.valueOf to use internal cache
                        waitforAccetpors.remove(Integer.valueOf(incMsg.getFrom()));
                        if(waitforAccetpors.size() < environment.getAcceptors().size()/2){
                            Paxos decisionToReplicas = Paxos.newBuilder()
                                    .setType(DECISION)
                                    .setDecision(Decision.newBuilder()
                                            .setSlotNum(pvalue.getSlotNum())
                                            .setC(pvalue.getCmd()))
                                    .build();

                            for(int replica : environment.getReplicas()) {
                                environment.send(replica, decisionToReplicas);
                            }
                            return;
                        }
                    } else {
                        Leader.this.incomingMessages.put(Paxos.newBuilder()
                                .setType(PREEMPTED)
                                .setPreempted(Preempted.newBuilder()
                                        .setBallot(incMsg.getBallot()))
                                .build());
                        return;
                    }
               }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                messageQueues.remove(ThreadID.get());
            }
        }
    }


    public static class ThreadID {
        private static final AtomicInteger nextID = new AtomicInteger(0);

        /**
         * 所以这里把ThreadLocal encapsulate的目的是实现auto-increment
         * note: 整个class是thread safe的，遵循了书上将的模式
         *
         * 仔细想想，下面这个obj，其实只有一个，所有threads的threadlocalmap的key都是指向这个obj的
         * 知道lead.class不被gc，那么他就是strongly reachable.
         */
        private static final ThreadLocal<Integer> threadID = new ThreadLocal<Integer>(){
            @Override
            protected Integer initialValue() {
                return nextID.getAndIncrement();
            }
        };

        public static int get() {
            return threadID.get();
        }
    }
}