# Paxos Java implementation

## What is this?

Implementation of multi-paxos protocol. 

## How to demo this project?

#### How to build?

I use Intellij to manage my project. To build project, just
import project to Intellij, don't forget to add google-protobuf library, then build.

#### How to run?

* First, in "config" file, decide the system topology(how many leaders, replicas and acceptors in the system).

* The main method is in utilities.SingleNodeEnvironment. It accepts two args: the first is config file path, and the second is the number of requests(RSM commands) that need to be decided by Paxos protocol.

* Run the program, you will see the result in console.

## Understand Paxos protocol

####Just read these papers

* The Part-Time Parliament, by Leslie Lamport
* Paxos Made Simple, by Leslie Lamport
* Paxos Made Live, by Tushar Chandra, Robert Griesemer and Joshua Redstone
* Paxos Made Moderately Complex, by ROBBERT VAN RENESSE and DENIZ ALTINBUKEN

#### Basic distributed computing concepts related to Paxos

* Time models -- synchronous, asynchronous, partial synchronous
* Failure models -- fail-stop, byzantine
* Definition of the consensus problem -- agreement, validity, termination
* Replia state machine(RSM)
* The FLP impossible result
* And other RSM protocols -- Zab(Zookeeper), Viewstamp, Raft (Paxos, Viewstamp and Raft are based on similar ideas)

An excellent must-read book -- Distributed Algorithms, by Nancy A. Lynch

## Understand the code

Here is the class diagram of key classed in the project:
![class diagram](https://github.com/BBQyuki/Paxos-log-replication-Java/blob/master/class-diagram-1.png)

#### Some explanation

* Replica, Leader and Acceptor are three key roles in Paxos protocol.

* Do not confuse with 'Leader' here. The Leader in Paxos is the conductor of each ballot. You may think what is the difference between Leader here and Leader in Raft or Viewstamp.
Leader in Raft or Viewstamp is more like the "prime" leader of all leaders in Paxos -- Raft and Viewstamp do a leader election. In Paxos, every leader can propose, while in Raft and Viewstamp, only the selected 'prime' leader can propose.

* Scout and Commander are two helping roles in Leader. Scout is for phase 1 of protocol and Command for phase 2. They are designed as inner classes in Leader for simplicity.


##### For more infomation, pls see comments in code.
