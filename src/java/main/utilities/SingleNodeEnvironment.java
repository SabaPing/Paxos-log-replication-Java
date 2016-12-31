package utilities;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import utilities.PaxosMsgs.Paxos;

/**
 * non-distributed mode implementation.
 * is used for debugging.
 */
public class SingleNodeEnvironment implements Environment {

    private final List<Integer> replicas;
    private final List<Integer> leaders;
    private final List<Integer> acceptors;

    private final Map<Integer, BlockingQueue<Paxos>> msgQueueMap;

    private final BlockingQueue<Paxos> result;

    public SingleNodeEnvironment(String config) {
        replicas = new ArrayList<>();
        leaders = new ArrayList<>();
        acceptors = new ArrayList<>();
        msgQueueMap = new ConcurrentHashMap<>();
        result = new ArrayBlockingQueue<>(100);
        parseConfig(config);
        buildMsgQueue();
    }

    private void parseConfig (String filePath) {
        Path config = Paths.get(filePath);
        try (Scanner sc = new Scanner(Files.newBufferedReader(config))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.length() == 0 || line.charAt(0) == '*') continue;
                char role = line.charAt(0);
                int id = Integer.parseInt(line.substring(2));

                switch (role) {
                    case 'R': {
                        replicas.add(id);
                        break;
                    }
                    case 'L': {
                        leaders.add(id);
                        break;
                    }
                    case 'A': {
                        acceptors.add(id);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot parse config file");
            e.printStackTrace();
        }
    }

    private void buildMsgQueue () {
        for (int i : replicas) {
            msgQueueMap.put(i, new ArrayBlockingQueue<>(100));
        }
        for (int i : leaders) {
            msgQueueMap.put(i, new ArrayBlockingQueue<>(100));
        }
        for (int i : acceptors) {
            msgQueueMap.put(i, new ArrayBlockingQueue<>(100));
        }
    }

    @Override
    public List<Integer> getReplicas() {
        return replicas;
    }

    @Override
    public List<Integer> getLeaders() {
        return leaders;
    }

    @Override
    public List<Integer> getAcceptors() {
        return acceptors;
    }

    @Override
    public void send(int toID, PaxosMsgs.Paxos msg) {
        try {
            msgQueueMap.get(toID).put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Paxos receive(int who) {
        //todo null is bad, need to define a default Paxos msg
        Paxos msg = null;
        try {
            msgQueueMap.get(who).take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return msg;
    }

    //act as a pseudo-client
    public static void main(String[] args) {
        Environment env = new SingleNodeEnvironment (args[0]);
    }

}
