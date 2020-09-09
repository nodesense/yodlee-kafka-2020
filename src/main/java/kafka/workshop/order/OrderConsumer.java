package kafka.workshop.order;

import kafka.workshop.Settings;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;

import static java.time.Duration.ofSeconds;
import static java.util.Collections.singletonList;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

// kafka-topics --zookeeper kx.nodesense.ai:2181 --create --topic orders --replication-factor 1 --partitions 3



public class OrderConsumer {

    public static String TOPIC = "orders";


    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, Settings.BOOTSTRAP_SERVERS);

        props.put(GROUP_ID_CONFIG, "order-consumer-group"); // offset, etc, TODO

        props.put(ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

        props.put(SESSION_TIMEOUT_MS_CONFIG, "30000");

        props.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, OrderDeserializer.class);

        // <Key as string, Value as OrderConfirmation>
        KafkaConsumer<String, Order> consumer = new KafkaConsumer<>(props);

        // Subscribe for topic(s)
        consumer.subscribe(singletonList(TOPIC));

        System.out.println("Consumer Starting!");


        while (true) {
            // poll (timeout value), wait for 1 second, get all the messages
            // <Key, Value>
            // poll method get bytes from Kafka broker
            // poll method shall invoke deserialize method internally
            // that converts bytes to Java Object [Order]
            // return order object/POJO as output
            ConsumerRecords<String, Order> records = consumer.poll(ofSeconds(1));
            // if no messages
            if (records.count() == 0)
                continue;


            // Iterating over each record
            for (ConsumerRecord<String, Order> record : records) {

                System.out.printf("partition= %d, offset= %d, key= %s, value= %s\n",
                        record.partition(),
                        record.offset(),
                        record.key(),
                        record.value());

                Order order = record.value();
                System.out.println(("order no " + order.orderId));

            }

            consumer.commitSync();
        }
    }
}