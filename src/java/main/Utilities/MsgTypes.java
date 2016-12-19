package Utilities;

/**
 * Created by yifan on 12/19/16.
 * immutable
 */
public enum MsgTypes {
    //messages in synod
    P1A("P1A"),
    P1B("P1B"),
    P2A("P2A"),
    P2B("P2B"),

    //messages between replicas and clients
    REQUEST("REQUEST"),
    RESPONSE("RESPONSE"),

    //messages between replicas and synod
    PROPOSE("PROPOSE"),
    DECISION("DECISION");

    private String name;

    MsgTypes(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
