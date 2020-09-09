# Clusters

1 zoo keeper
3 brokers

### Broker

Each broker has unique id [number] starting from 0...(2^64 - 1)
Each broker in separate machine for failover/vm, just ensure that physical copies are exist, not all vms in same physical system

Single system cluster - running brokers on different port numbers

Broker ID - unique
Port number - unique within the same computer
location of log file - unique on the same computer


#### Turn off confluent systems

```
confluent local stop

```

#### Cluster Configuration

Start Zoo Keeper, run on port 2181, run on ssh 1

```
zookeeper-server-start $KAFKA_HOME/etc/kafka/zookeeper.properties
```


```
Broker-0 
 Id: 0
 port: 9092
 logs.dir:/tmp/kafka-logs
```

 run on ssh 2

to start broker-0

``` 
 kafka-server-start $KAFKA_HOME/etc/kafka/server.properties
```

 run on ssh 3

```
Broker-1
 Id: 1
 port: 9093
 logs.dir:/tmp/kafka-logs-1
```

Use --override broker.id=1
    --override listeners=PLAINTEXT://:9093
    --override log.dirs=/tmp/kafka-logs-1
    --override confluent.metadata.server.listeners=http://0.0.0.0:8091
    
    
To start broker-1, use this command

``` 
 kafka-server-start $KAFKA_HOME/etc/kafka/server.properties --override broker.id=1 --override listeners=PLAINTEXT://:9093 --override log.dirs=/tmp/kafka-logs-1 --override confluent.metadata.server.listeners=http://0.0.0.0:8091
```

 run on ssh 4
 
 
```
Broker-2
 Id: 2
 port: 9094
 logs.dir:/tmp/kafka-logs-2
```

Use --override broker.id=2
    --override listeners=PLAINTEXT://:9094
    --override log.dirs=/tmp/kafka-logs-2
     --override confluent.metadata.server.listeners=http://0.0.0.0:8092
 
To start broker-2, use this command

``` 
 kafka-server-start $KAFKA_HOME/etc/kafka/server.properties --override broker.id=2 --override listeners=PLAINTEXT://:9094 --override log.dirs=/tmp/kafka-logs-2 --override confluent.metadata.server.listeners=http://0.0.0.0:8092
```
 
 ### Create topics, to see whether partitions, replicas goes into different broker
 
 replicas <= brokers size
 
 login into ssh-5
 
 ```
 kafka-topics --create --zookeeper localhost:2181 --replication-factor 3 --partitions 3 --topic messages
 
  kafka-topics --describe --zookeeper localhost:2181  --topic messages

 ```

produce data

```
kafka-console-producer --broker-list localhost:9092 --topic messages
```


 
 