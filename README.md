# Paxos Java implementation

## What is this?

Implementation of multi-paxos protocol. 

## How to demo this project?

#### How to build?

I use Intellij to manage my project. To build project, just
import project to idea, add google-protobuf library, then build.

#### How to run?

* First, in config file, decide the system topology(how many leaders, replicas and acceptors in the system).

* The main method is in utilities.SingleNodeEnvironment. And it accepts two args: the first is config file path, and the second is the number of requests(RSM commands) that need to be decided by Paxos protocol.

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

Here is the class diagram of the whole project:


