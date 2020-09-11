package kafka.workshop.invoice;


import kafka.workshop.Settings;
import kafka.workshop.models.Customer;
import kafka.workshop.models.Invoice;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
//

// kafka-topics --zookeeper k17.training.sh:2181 --create --topic customers --replication-factor 1 --partitions 3
// kafka-console-consumer --bootstrap-server k17.training.sh:9092 --topic customers  --from-beginning --property print.key=true


// kafka-avro-console-consumer --bootstrap-server k17.training.sh:9092 --topic customers --from-beginning --property print.key=true --property schema.registry.url="http://k17.training.sh:8081"


public class CustomerProducer {

    public static String TOPIC = "customers";

    static Random random = new Random();
    static int[] categories = {1, 2, 3, 4};
    static int[] customerIds = {1000, 2000, 3000, 4000, 5000, 6000};
    static String[] customerNames = {"Krish", "Gayathri", "Nila", "Venkat", "Hari", "Ravi"};

    static String[] stateIds = {"KA", "TN", "KL", "MH", "DL", "AP"};



    public static void main(String[] args) throws ExecutionException, InterruptedException {


        List<Customer> customers = new ArrayList<>();
        customers.add(new Customer("1000", 38, "Krish", "male"));
        customers.add(new Customer("2000", 35, "Gayathri", "female"));
        customers.add(new Customer("3000", 23, "Nila", "female"));
        customers.add(new Customer("4000", 42, "Venkat", "male"));
        customers.add(new Customer("5000", 43, "Hari", "male"));
        customers.add(new Customer("6000", 40, "Ravi", "male"));


        Properties props = new Properties();
        // hardcoding the Kafka server URI for this example
        props.put("bootstrap.servers", Settings.BOOTSTRAP_SERVERS);
        props.put("acks", "all");
        props.put("retries", 0);


         // Key/Value serializer for Avro format
        props.put(KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");

        // PATH to SCHEMA REGISTRY
        // Very first, The producer automatically register the avro schema with Schema Registry
        // what is the schema name?
        // <<topicname>>-value, <<topicname>>-key
        props.put("schema.registry.url", Settings.SCHEMA_REGISTRY);

        Producer<String, Customer> producer = new KafkaProducer<String, Customer>(props);

        Random rnd = new Random();
        for (Customer customer:customers) {
            String key = customer.getId().toString();
            // Invoice ID as key
            ProducerRecord<String, Customer> record = new ProducerRecord<String, Customer>(TOPIC,
                    key,
                    customer);

            // avro serializer called internally, convert the object to avro format
            producer.send(record).get(); // get() sync wait

            System.out.println("Sent Customer data" + customer);
          //  Thread.sleep(5000);
        }

        producer.close();

    }
}