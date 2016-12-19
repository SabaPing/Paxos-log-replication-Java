package protocol;

/**
 * Created by yifan on 12/19/16.
 * immutable
 */
class Ballot implements Comparable<Ballot>{
    private final String prefix;
    private final String conductor;

    public Ballot(String prefix, String conductor) {
        this.prefix = prefix;
        this.conductor = conductor;
    }

    public String getBallotN(){
        return prefix + conductor;
    }

    @Override
    public int compareTo(Ballot o) {
        String mine = prefix + conductor;
        String his = o.prefix + o.conductor;
        return mine.compareTo(his);
    }
}
