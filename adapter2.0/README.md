# Presentation Application

A application that interfaces with Babel Web Services to enable the evaluation of presentations. The application allows to generate new presentations and create new questions. Questions can be updated or deleted.

The application can be interacted with through a REST API and receive automatic reactive updates through Web Sockets. Moreover, a optional parameter can be passed to enable autonomous operation execution.

## How to Run

After building the jar file with:

```console
mvn clean package -U
```
The first node can be executed (assuming a _en0_ interface) in the following manner:

```console
java -DlogFileName=nodeA -jar target/nimbus-presentation.jar  babel.interface=en0 babel.address=127.0.0.1 application.autonomous=true application.bootstrap=true HyParView.contact=none
```

Other nodes can be launched by associating different IPS to the different processes as depicted below:

```console
java -DlogFileName=nodeA -jar target/nimbus-presentation.jar  babel.interface=en0 babel.address=127.0.0.2 application.autonomous=true
```


## Authors

Diogo Jesus (da.jesus@fct.unl.pt)