package protocol;

import java.util.HashSet;

/**
 * Created by yifan on 12/17/16.
 */
public class Acceptor {
    private Ballot curBallot;
    private HashSet<PValue> accepted;

    public Acceptor(){
        curBallot = new Ballot("0", "0");
        accepted = new HashSet<>();
    }

    private void foreverRun() {

    }
}
