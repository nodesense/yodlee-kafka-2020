whatever is earliest
BATCH_SIZE_CONFIG = 1000 bytes
LINGER_MS_CONFIG = 5 seconds

producer send msg 1 [500 bytes] - 12:00:01 [buffered, not send]
producer send msg 2 [500 bytes] - 12:00:02 [buffered, send [msg 1, msg 2] since 1000 bytes reached, two messages batched in single request]
producer send msg 3 [500 bytes] - 12:00:03 [buffered msg 1, 500 bytes]
No more messages for next 5 seconds
                                - 12:00:08 [send msg 3, 500 bytes to the broker]


Consumer Group: CG1 / greetings-consumer-group

Consumer Group Offset data maintained by kafka itself for the consumer group
 in topic called __consumer_offsets
 
messages

P0: [M0, M1, M2], write offset: 2
P1: [Q0],  write offset: 0
P2: [K0, K1],  write offset: 1

start consumer on group CG1

__consumer_offsets will have entries

CG1: {
    P0: 1
    P1: 0
    P2: 0
}

CG2: {
    P0: 3
    P1: 0
    P2: 1
}


consumer reads M0 from p0 [offset 0]
consumer commit offset 0 on p0 to the broker after procesing

restart the consumer.

Then it should start from M1,

consumer reads M1 from p0 [offset 1]
consumer commit offset 1 on p0 to the broker after procesing


consumer reads Q0 from p1 [offset 1]
consumer commit offset 0 on p1 to the broker after procesing


consumer reads K0 from p2 [offset 1]
consumer commit offset 0 on p2 to the broker after procesing


write/partition offset is different than consumer offset

Consumer offset: 
  1. the location where the consumer last read the data
  2. This helps consumer to resume where it left last time
  3. the data is maintained by broker, in kafka topic __consumer_offsets
  4. the consumer should commit the offset it has processed
  5. broker send back the ack for commit offset msg (sync/async)
  6. Every consumer group for each topic shall have its own offset
  
  
 JSON - Order/POJO
 
 Custom Serializer - Convert the POJO to JSON string to byte array 
 Custom Deserializer, receive byte array, convert JSON then to POJO
 
 Custom Partitioner 
 
 
 Starting a new consumer,
  the offset always set to latest
  then onwards, it follows the consumer offset settings
  