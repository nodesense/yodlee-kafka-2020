// SimpleConsumer.java
package kafka.workshop;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

public class SimpleConsumer {
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
        }

        while (true) {
            // Consumer poll/read/pull the data with wait time
            // poll for msgs for 1 second, any messges within second, group together
            // if no msg, exit in 1 second, records length is 0

            // data is already deserialized into string by the poll method
            // key/value is in domain model key: string, value: string
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

            if (records.count() == 0)
                continue; // wait for more msg


            // Iterate record and print the record
            for (ConsumerRecord<String, String> record: records) {

                System.out.printf("partition=%d, offset=%d\n", record.partition(),
                        record.offset());

                System.out.printf("key=%s, value=%s\n", record.key(), record.value());


                // discuss post break
                // manual commit if ENABLE_AUTO_COMMIT_CONFIG is "false"
                // technically consumer send a message to broker about commited offset against consumer group
               consumer.commitSync();
               // Thread.sleep(3000);
            }

            // Thread.sleep(2000);
        }

    }

}