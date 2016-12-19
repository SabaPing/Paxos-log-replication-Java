package protocol;

/**
 * Created by yifan on 12/19/16.
 * immutable
 */
class PValue {
    private final Ballot ballot;
    private final int slotNumber;
    private final Command command;

    public PValue(Ballot ballot, int slotNumber, Command command) {
        this.ballot = ballot;
        this.slotNumber = slotNumber;
        this.command = command;
    }

    @Override
    public String toString() {
        return "" + ballot + " " + slotNumber + " " + command;
    }
}
