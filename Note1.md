
> confluent local start


development not production setup

Using CONFLUENT_CURRENT: /var/folders/bv/1_wvc3qn3s15dkxpvl71glh80000gn/T/confluent.4vHmsRPf

```

    kafka-topics  --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test


    kafka-topics --list --bootstrap-server localhost:9092


    confluent local current


    confluent local status


    kafka-topics --describe --zookeeper localhost:2181 --topic test


    kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 4 --topic test2


    kafka-topics --describe --zookeeper localhost:2181 --topic test2

```


    ls on /tmp/.../kafka/data/test-0

    00000000000000000000.log        -- contains the messages stored 
                                    -- segment file
                                    -- it grows based on configured file limit
                                    -- then create a new file then grow up to file limit
                                    -- then create a new file
                                    -- fiels are deleted automatically by kafka based on retentions
    00000000000000000000.index  
                                    map file, mapping offset to message location in the log file
    00000000000000000000.timeindex
                                    map file, mapping the time the data inserted to offset index [link to index file]


producer send msg-1 [150 bytes] - offset 0    Sep 8, 2020, 10:45
producer send msg-2 [250 bytes] -- offset 1   Sep 8, 2020, 10:47
00000000000000.log
010100101001010010101010101001001010000
0                                      150
0101001010010100101010101010010010100001111111000000000
151                                                    400

fseek/ftell
open file
 ftell(100) -- the file pointer moves to byte 100


00000000000000000000.index

offset to the file location
0       0
1       151


.timeindex

Sep 8, 2020, 10:45 [in ms]        0   
Sep 8, 2020, 10:47 [in ms]        1   




Broker ready, zookeeper ready
topic ready - test

console-producer
    
```    
    kafka-console-producer --broker-list localhost:9092 --topic test
```

    you can write text messsage, one per line, hit enter key, then
    message send broker, key is null

console-consumer

   open second terminal/putty, ssh into remote system
   
   listen from latest messages
   
```
    kafka-console-consumer --bootstrap-server localhost:9092 --topic test
```

get all the messages from beginging and then listen for new messages, internally kafka consumer uses partition to read data

```
kafka-console-consumer --bootstrap-server localhost:9092 --topic test --from-beginning
```

read from specific partition
```
kafka-console-consumer --bootstrap-server localhost:9092 --topic test --partition 0 --from-beginning
```
read from partiticular offset
```
kafka-console-consumer --bootstrap-server localhost:9092 --topic test --partition 0 --offset 3
```


# With 4 partitions, when key is null, producers uses round robin method to publish data to partitions
```
    kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 4 --topic logs
    
    kafka-console-producer --broker-list localhost:9092 --topic logs
```
read from all partitions [notedown the message ordering]

```
    kafka-console-consumer --bootstrap-server localhost:9092 --topic logs  --from-beginning

read from specific partitions

    kafka-console-consumer --bootstrap-server localhost:9092 --topic logs --partition 0 --from-beginning

kafka-console-consumer --bootstrap-server localhost:9092 --topic logs --partition 1 --from-beginning

kafka-console-consumer --bootstrap-server localhost:9092 --topic logs --partition 2 --from-beginning

kafka-console-consumer --bootstrap-server localhost:9092 --topic logs --partition 3 --from-beginning


``` 

## With Keys, semantic parititioning

Kafks uses the keys to decide the partition. 

Hash(key) % number of patitions = partition id 0 to n - 1

given the same key, the messsages always goes to same parition, 
producer decide the partition


```
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic greetings

```

key value seperator allowed in console producer with command line option. : as seperator
key:value
IN:Order1
IN:Order2
USA:Order3
UK:Order4
CA:Order5
IN:Order6

```
kafka-console-producer --broker-list localhost:9092 --topic greetings --property "parse.key=true" --property "key.separator=:"
```

```
kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings --partition 0 --from-beginning --property print.key=true

kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings --partition 1 --from-beginning --property print.key=true

kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings --partition 2 --from-beginning --property print.key=true
```

## Consumer Group
run below in separate ssh sessions

```
kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings --from-beginning --property print.key=true --group greetings-group
```

```
kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings --from-beginning --property print.key=true --group greetings-group
```

```
kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings --from-beginning --property print.key=true --group greetings-group
```

```
kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings --from-beginning --property print.key=true --group greetings-group
```
