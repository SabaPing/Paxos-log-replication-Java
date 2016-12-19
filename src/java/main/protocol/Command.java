package protocol;

/**
 * Created by yifan on 12/19/16.
 * immutable
 */
class Command {
    private final int issuer;
    private final int cmdID;
    private final String operation;

    public Command(int issuer, int cmdID, String operation) {
        this.issuer = issuer;
        this.cmdID = cmdID;
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "" + issuer + " " + cmdID + " " + operation;
    }
}
