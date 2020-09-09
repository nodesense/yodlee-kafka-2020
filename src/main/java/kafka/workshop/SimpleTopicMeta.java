package kafka.workshop;

// SimpleProducer.java
// kafka-console-consumer --bootstrap-server k17.training.sh:9092 --topic messages  --from-beginning --property print.key=true --property print.timestamp=true

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Properties;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

// kafka-topics --create --zookeeper k17.training.sh:2181 --replication-factor 1 --partitions 3 --topic messages


public class SimpleTopicMeta {

    public static String TOPIC = "messages";



    public static void main(String[] args) throws  Exception {
        Properties props = new Properties();

        props.put(BOOTSTRAP_SERVERS_CONFIG, Settings.BOOTSTRAP_SERVERS); // broker address

        // delivery ack demanded by producer from broker
        // acks 0
            // Producer send message to broker
            // Broker receive the message, message still in memory, not written to disk yet
            // Broker acks to producer that message received
            // Pros, fastest response
            // Cons, messgae still in memory, not persisted to disk, if broker fails between,
                  // then message is log
        // acks 1
            //   Producer send message to broker
           // Broker receive the message, message written to disk
           // Broker acks to producer that message received
           // Pros, fast [due to single disk/broker] response, message is persisted to disk
           // Cons, Replicas not yet updated,  if broker disk/system fails, message is lost, no replica updated yet

        // acks all
            //   Producer send message to broker
            // Broker receive the message, message written to disk, broker ensure that all replicas updated
            // then Broker acks to producer that message received
            // Pros, Persisted in all replicas
            // Cons, slow due to all system disk/io operation
        props.put(ACKS_CONFIG, "all"); // acknowledge level "0", "1", "all"

        // for producer, if the message is failed to write,
        // how many times producer should attempt to write the same message
        props.put(RETRIES_CONFIG, 3); // how many retry when msg failed to send

        // Optimization of payload between producer and broker
        // by calling send(record), won't send the messsage immediately,
                //  instead it adds the message to the buffer until the buffer reaches BATCH_SIZE_CONFIG

        // based on BATCH_SIZE_CONFIG, LINGER_MS_CONFIG the message is delivered by background thread
        // whatever first condition reached,
        // group messages by max byte size BATCH_SIZE_CONFIG, 16 KB, dispatch when it reaches 16000 bytes
        props.put(BATCH_SIZE_CONFIG, 16000); // bytes

        // send(msg), message won't send immdiately
        // until the LINGER_MS_CONFIG time come
        // group the messages by max wait time LINGER_MS_CONFIG the message can be in buffer,
        // when 100 ms reached, dispatch the message
        props.put(LINGER_MS_CONFIG, 100); // milli second

        // Reserved memory, pre-alloted in bytes
        props.put(BUFFER_MEMORY_CONFIG, 33554432);

        // Message => Key/Value, key/value any type, string,long, boolean, POJO object
        // Kafka accept only bytes for key/value, key is optional, means can be null

        // in this example,
        // Key is a string, producer should convert
                // string to byte array [serialized data] before sending to kafka

        // Value is string, producer should converte to byte array [serialized data]
        // StringSerializer accept string as input, produce byte array of that string
       // producer.send method shall call serializer internally
        props.put(KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        System.out.println("PRoducer Setup ");
        // Key as string, value as string
        Producer<String, String> producer = new KafkaProducer<>(props);

        // get list of paritions for the topic
        List<PartitionInfo> partitions = producer.partitionsFor(TOPIC);
        for (PartitionInfo partitionInfo: partitions) {
            // partitionInfo.
            System.out.println("Partition " + partitionInfo);
            System.out.println("Leader for partition: " + partitionInfo.leader());
        }




        producer.close();
    }

}