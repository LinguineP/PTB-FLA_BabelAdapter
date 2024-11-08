# PTB-FLA_BabelAdapter
Adapter Plugging PTB-FLA applications onto protocols implemented in Babel communication protocols framework


This project aims to bridge [PTB-FLA](https://github.com/miroslav-popovic/ptbfla) federated learnig applications with comumunication protocols implemented in [Babel](https://codelab.fct.unl.pt/di/research/tardis/wp6/babel) communication protocol framework. It is partly based on [Babel-Swarm](https://codelab.fct.unl.pt/di/research/tardis/wp6/babel-swarm) examples. This allows PTB-FLA based applications to work over the network overcomming their single host limitations.

The PTB-FLA adapter consists of two components: Doppelgangers, which mimic remote PTB-FLA applications, and BabelAdapterApp, which transmits messages using network protocols implemented in Babel. These components communicate via TCP, using JSON as the message format to ensure cross-language compatibility between Python and Java. BabelAdapterApp relies on primitives such as eager push gossip broadcast mechanism to facilitate communication between devices on a local network. It manages both device discovery and end-to-end message delivery to all participants, enabling seamless communication across the network. A Doppelganger instance represents an exact duplicate of the remote PTB-FLA application for communication purposes. It serves as a bridge between Python's multiprocessing communication primitives and BabelAdapterApp, ensuring message delivery to the correct PTB-FLA application instance.


## Usage


After cloning the repo, possition yourself within project directory:
1. In `src/main/resources/adapter.conf` change doppelganger.ports to reflect those of PTB-FLA application instances running on other devices (_ip is formed as 6000 + instance id_). 
2. Build the app by running: `mvn clean package -U` *
3. Run it with: `java -jar .\target\PTBFLA-Babel-adapter-0.0.6.jar babel.address=<your_device_ip>` **
4. Proceed to the startup of PTB-FLA applications

* \* _Requires maven installed_
* ** _First Application instance should be ran with `java -jar .\target\PTBFLA-Babel-adapter-0.0.6.jar babel.address=<your_device_ip> HyParView.Contact=none`_ 
* *** _If some of the ports are occupied when starting the adapter exit it  and tun it again and it should work_
