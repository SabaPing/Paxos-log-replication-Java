package protocol;

/**
 * Created by yifan on 12/19/16.
 */
enum MsgTypes {
    //messages in synod
    P1A,
    P1B,
    P2A,
    P2B,

    //messages between replicas and clients
    REQUEST,
    RESPONSE,

    //messages between replicas and synod
    PROPOSE,
    DECISION;
}
