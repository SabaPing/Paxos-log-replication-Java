package paxos;

import paxos.PaxosMsgs.*;
import utilities.Environment;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import static paxos.PaxosMsgs.Paxos.Type.*;


/**
 * Created by yifan on 12/17/16.
 *
 * todo design concurrent structure, check if thread safe
 */
public class Leader extends Thread{
    private final Ballot ballot;
    private final boolean active;
    private final Set<Propose> proposals;

    //Message handler thread
    private final MessageHandler msgHandler;

    //shared states, need thread-safe mechanism
    private final Map<Integer, BlockingQueue<Paxos>> messageQueues;
    private final BlockingQueue<Paxos> incomingMessages;

    //this object provide sending function, network topology is stored here
    private final Environment environment;

    private final int ID;

    //This id is for distinguishing different message queues
    //private final int localThreadID;
    //to get local thread id, call ThreadID.get()!

    public Leader(int id, Environment environment) {
        ID = id;
        ballot = Ballot.newBuilder().setPrefix(0).setConductor(id).build();
        active = false;
        proposals = new HashSet<>();

        this.environment = environment;

        //to avoid this reference escape, don's start inner class thread in constructor
        msgHandler = new MessageHandler();

        messageQueues = new ConcurrentHashMap<>();

        //initial and register message queue
        this.incomingMessages = new ArrayBlockingQueue<Paxos>(100);
        messageQueues.putIfAbsent(ThreadID.get(), this.incomingMessages);
    }

    @Override
    public void run() {

    }

    private void forever(){

    }





    /**
     * 这里scouts由leader id + ballot 唯一确定，commanders由leader+ballot+slot唯一确定。
     * 对每个不同的scout和commander threads，里面都要有个block Q。
     * MessageHandler 必须知道msg type，ballot，和proposal，把对应的msg put到对应的Q里。
     * 以上保证了reliable。
     * 注意：见paper笔记。需要在msg里加额外的from，to fields
     * And give scout and commander 一个额外的 int id, use ThreadLocal
     */
    private class MessageHandler extends Thread{


        public MessageHandler () {
        }

        @Override
        public void run() {
            while(true) {

            }
        }

    }

    /**
     * inner class Scout
     * inter-thread communication -- use message queue
     * do synod phase 1
     */
    private class Scout extends Thread{

        //save pvalues from acceptors
        private final Set<PValue> pValueSet;

        private final BlockingQueue<Paxos> incomingMessages;
        private final Ballot b;
        private final List<Integer> waitforAccetpors;

        public Scout(Ballot ballot) {
            b = ballot;
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
                        .setBallot(b))
                    .build();
            byte[] p1aBytes = p1a.toByteArray();

            for(int acceptor : waitforAccetpors){
                environment.send(acceptor, p1aBytes);
            }

            while(true){
                try {
                    //because of message handler, scout can only get p1b msg
                    P1b incMsg = incomingMessages.take().getP1B();

                    if(incMsg.getBallot().equals(b)){
                        pValueSet.addAll(incMsg.getAccepted().getPvalueList());
                        //use Integer.valueOf to use internal cache
                        waitforAccetpors.remove(Integer.valueOf(incMsg.getFrom()));

                        if(waitforAccetpors.size() < environment.getAcceptors().size()/2){
                            //偷懒实现了，由于inner class知道leader的Q。。。
                            Leader.this.incomingMessages.put(Paxos.newBuilder()
                                    .setType(ADOPTED)
                                    .setAdopted(Adopted.newBuilder()
                                        .setBallot(b)
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
            byte[] p2aBytes = p2a.toByteArray();

            //todo what is this acceptor set, need check
            for(int acceptor : waitforAccetpors){
                environment.send(acceptor, p2aBytes);
            }

            while(true){
                try {
                    //because of message handler, scout can only get p2b msg
                    P2b incMsg = incomingMessages.take().getP2B();

                    if(incMsg.getBallot().equals(pvalue.getBallot())){
                        //use Integer.valueOf to use internal cache
                        waitforAccetpors.remove(Integer.valueOf(incMsg.getFrom()));

                        if(waitforAccetpors.size() < environment.getAcceptors().size()/2){

                            Paxos outMsg = Paxos.newBuilder()
                                    .setType(DECISION)
                                    .setDecision(Decision.newBuilder()
                                            .setSlotNum(pvalue.getSlotNum())
                                            .setC(pvalue.getCmd()))
                                    .build();
                            byte[] outMsgBytes = outMsg.toByteArray();

                            for(int replica : environment.getReplicas()) {
                                environment.send(replica, outMsgBytes);
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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