# PTB-FLA_BabelAdapter

Adapter Plugging PTB-FLA applications onto protocols implemented in Babel communication protocols framework
This adapter was implemented for the TaRDIS GMV use case.
This project aims to bridge PTB-FLA federated learnig applications with comumunication protocols implemented in Babel communication protocol framework. It is based on Babel-Swarm examples. This allows PTB-FLA based applications to work over the network overcoming their single host limitations.

The PTB-FLA adapter consists of two components: Doppelgangers, which mimic remote PTB-FLA applications, and BabelAdapterApp, which transmits messages using network protocols implemented in Babel. These components communicate via TCP, using JSON as the message format to ensure cross-language compatibility between Python and Java. BabelAdapterApp relies on primitives such as eager push gossip broadcast mechanism to facilitate communication between devices on a local network. It manages both device discovery and end-to-end message delivery to all participants, enabling seamless communication across the network. A Doppelganger instance represents an exact duplicate of the remote PTB-FLA application for communication purposes. It serves as a bridge between Python's multiprocessing communication primitives and BabelAdapterApp, ensuring message delivery to the correct PTB-FLA application instance.
