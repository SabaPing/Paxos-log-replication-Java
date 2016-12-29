package paxos;

import utilities.PaxosMsgs;

import java.util.Comparator;


public class BallotComparator implements Comparator<PaxosMsgs.Ballot>{
    @Override
    public int compare(PaxosMsgs.Ballot o1, PaxosMsgs.Ballot o2) {
        String b1 = "" + o1.getPrefix() + o1.getConductor();
        String b2 = "" + o2.getPrefix() + o2.getConductor();
        return b1.compareTo(b2);
    }
}
