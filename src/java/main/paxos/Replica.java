package paxos;

import utilities.Environment;
import utilities.PaxosMsgs.*;
import static utilities.PaxosMsgs.Paxos.Type.*;
import java.util.*;

/**
 * Invariants for replicas:
 * R1: There are no two different commands decided for the same slot.
 * R2: All commands up to slot out are in the set of decisions.
 * R3: For all replicas ρ, ρ.state is the result of applying
 * the commands⟨s,cs⟩∈ρ.decisions to initial_state for all s up to slot_out,
 * in order of slot number.
 * R4: For each ρ, the variable ρ.slot out cannot decrease over time.
 * R5: A replica proposes commands only for slots for which it knows the configuration.
 *
 * And R1 is ensured by synod(i.e., simple paxos)
 *
 * Did not implement reconfig function!!!
 */
public class Replica extends Thread {
    //paxos related thread
    private  int slot_in;
    private  int slot_out;
    private final Queue<Command>  requests;
    private final Map<Integer, Command> proposals;
    private final Map<Integer, Command> decisions;
    private final int ID;

    //for configuration change
    private final int WINDOW;

    //For sending and receiving messages
    private final Environment environment;

    public Replica(int ID, Environment environment) {
        this.ID = ID;
        this.environment = environment;
        this.decisions = new HashMap<>();
        this.proposals = new HashMap<>();
        this.requests = new ArrayDeque<>();
        this.slot_in = 1;
        this.slot_out = 1;

        //???ease conflicts between ballot???
        this.WINDOW = 3;
    }

    /**
     * Transfer requests from requests to proposals
     */
    private void propose () {
        while((slot_in < slot_out + WINDOW) && !requests.isEmpty()) {
            //if not already proposed for slot_in
            if (!decisions.containsKey(slot_in)) {
                Command polledCmd = requests.poll();
                proposals.put(slot_in, polledCmd);

                Paxos temp_propose = Paxos.newBuilder()
                        .setType(PROPOSE)
                        .setPropose(Propose.newBuilder()
                                .setSlotIn(slot_in)
                                .setC(polledCmd))
                        .build();

                for (int leader : environment.getLeaders()) {
                    environment.send(leader, temp_propose);
                }
            }
            slot_in++;
        }
    }

    /**
     * The function perform() is invoked with the same sequence of commands at all replicas.
     * Different replicas may end up proposing the same command for different slots,
     * and thus the same command may be decided multiple times.
     */
    private void perform (Command cmd) {
        //if it has already performed the command
        for (int i = 1; i < slot_out; i++) {
            if (decisions.get(i).equals(cmd)){
                slot_out++;
                return;
            }
        }

        System.out.println("Replica " + ID + ": perform slot " + slot_out + " command " + cmd.getOperation());
        slot_out++;

        Paxos temp_response = Paxos.newBuilder()
                .setType(RESPONSE)
                .setResponse(Response.newBuilder()
                        .setCid(cmd.getCid())
                        .setResult("Performed"))
                .build();

        environment.send(cmd.getClient(), temp_response);
    }

    public void run() {
        while (true) {
            Paxos incMsg = environment.receive();
            switch (incMsg.getType()) {
                case REQUEST:{
                    requests.offer(incMsg.getRequest().getC());
                    break;
                }

                /**
                 * Decisions may arrive out of order and multiple times
                 */
                case DECISION:{
                    //adds the decision to the set decisions
                    Decision body = incMsg.getDecision();
                    decisions.put(body.getSlotNum(), body.getC());

                    /**
                     * first try to run decisions that are ready for execution
                     * before trying to receive more messages
                     */
                    for (Map.Entry<Integer, Command> entry : decisions.entrySet()) {
                        if (proposals.containsKey(entry.getKey())) {
                            Command temp_cmd = proposals.remove(entry.getKey());
                            if (!temp_cmd.equals(entry.getValue())) {
                                requests.offer(temp_cmd);
                            }
                        }
                        perform(entry.getValue());
                    }
                }
            }
            propose();
        }
    }
}
