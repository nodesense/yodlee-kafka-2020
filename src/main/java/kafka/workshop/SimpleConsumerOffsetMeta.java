// SimpleConsumer.java
package kafka.workshop;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

public class SimpleConsumerOffsetMeta {
    public static String TOPIC = "messages";

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();

        props.put(BOOTSTRAP_SERVERS_CONFIG, Settings.BOOTSTRAP_SERVERS);

        // Consumer group id, should be unique, partition allocated to consumers inside teh group
        // consumer group id present, when we restart the program, it will continue from where it left
        props.put(GROUP_ID_CONFIG, "greetings-consumer-group"); // offset, etc, TODO

        // ENABLE_AUTO_COMMIT_CONFIG = true, after receiving message, automatically commit the offset
            // Pros: simple, not recommended
            // Cons: Consumer took the message, not processed yet, faced exception while processing message,
                    // however the commit offset send to broker [mean that message is processed]
        // ENABLE_AUTO_COMMIT_CONFIG = false, developers manually commit the offset
            // pros: Developer control the commit offset based on consumer app behaviour, recommened approach
        props.put(ENABLE_AUTO_COMMIT_CONFIG, "false");
        // Applicable only if ENABLE_AUTO_COMMIT_CONFIG = true
        // props.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        // inactivity  with broker for 3 seconds, it times out
        props.put(SESSION_TIMEOUT_MS_CONFIG, "30000");

        // Consumer receives the message, which contains bytes for key and value
        // convert the bytes to meaniningful domain object, POJO, JSON, string, long etc
        // Deserialization [bytes to other domain types]

        // key deserialize the bytes data into String,  [Long format, JSON,AVRO formats discussed later]
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        // value deserialize the bytes data into String format, [JSON,AVRO formats]
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        // discussed later, if you always wants read from begining
       //  props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
       // props.put(ConsumerConfig.CLIENT_ID_CONFIG, "your_client_id");
       // props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // <Key as string, Value as string>
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // subscribe from one or more topics
        consumer.subscribe(Collections.singletonList(TOPIC));

        System.out.println("Consumer starting..");


        List<PartitionInfo> partitions = consumer.partitionsFor(TOPIC);
        for (PartitionInfo partitionInfo: partitions) {
            // partitionInfo.
            System.out.println("Partition " + partitionInfo);

            TopicPartition topicPartition = new TopicPartition(TOPIC, partitionInfo.partition());
            // what is the commited offset
            OffsetAndMetadata offetMeta = consumer.committed(topicPartition);
            System.out.println("Commited offset " + offetMeta);

            // demonstrated in rebalanced consumer, differ few minutes
            // consumer.seek(topicPartition, 0);

        }

    }

}