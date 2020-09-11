
DNS - brokers should be able reach each other

internet


k17 -- zookeeper
k18 -- broker0
k19 -- broker1
k20 -- broker2


local intranet

ping 10.20.45.67 [work]

ping system10 [may not work, if no dns]

update /etc/hosts

system10 10.20.45.67

hostname

/etc/hostname


zookeeper-server-start $KAFKA_HOME/etc/kafka/zookeeper.properties


zookeeper.connect=localhost:2181
broker.id=0

K18 setting

kafka-server-start $KAFKA_HOME/etc/kafka/server.properties --override broker.id=0 --override zookeeper.connect=k17.training.sh:2181 

K19 setting

kafka-server-start $KAFKA_HOME/etc/kafka/server.properties --override broker.id=1 --override zookeeper.connect=k17.training.sh:2181 



K20 setting

kafka-server-start $KAFKA_HOME/etc/kafka/server.properties --override broker.id=2 --override zookeeper.connect=k17.training.sh:2181 


```
zookeeper-shell k17.training.sh:2181

ls /

ls /brokers

ls /brokers/ids


now stop and start the broker to see the ls /brokers/ids s tatus

```