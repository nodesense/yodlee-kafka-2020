package kafka.workshop.invoice;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import kafka.workshop.Settings;
import kafka.workshop.models.Customer;
import kafka.workshop.models.CustomerInvoice;
import kafka.workshop.models.Invoice;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

// kafka-topics --zookeeper k17.training.sh:2181 --create --topic statewise-amount --replication-factor 1 --partitions 1
// kafka-topics --zookeeper k17.training.sh:2181 --create --topic statewise-invoices-count --replication-factor 1 --partitions 1
// kafka-console-consumer --bootstrap-server k17.training.sh:9092 --topic statewise-invoices-count --from-beginning --property print.key=true --property print.value=true --formatter kafka.tools.DefaultMessageFormatter --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
// kafka-console-consumer --bootstrap-server k17.training.sh:9092 --topic statewise-amount --from-beginning --property print.key=true --property print.value=true --formatter kafka.tools.DefaultMessageFormatter --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

// kafka-avro-console-consumer --bootstrap-server k17.training.sh:9092 --topic customer_invoices --from-beginning --property print.key=true --property schema.registry.url="http://k17.training.sh:8081"

public class InvoiceStreamJoin {

    public static void main(String[] args) throws  Exception {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "product-invoice2-stream");
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "product-invoice-stream-client2");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Settings.BOOTSTRAP_SERVERS);

        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);

        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1 * 1000);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp");

        props.put("schema.registry.url", Settings.SCHEMA_REGISTRY);

        // Custom Serializer if we have avro schema InvoiceAvroSerde
        final Serde<Invoice> InvoiceAvroSerde = new SpecificAvroSerde<>();
        final Serde<Customer> CustomerAvroSerde = new SpecificAvroSerde<>();
        final Serde<CustomerInvoice> CustomerInvoiceAvroSerde = new SpecificAvroSerde<>();

        // part of Schema Registry

        // When you want to override serdes explicitly/selectively
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url",
                Settings.SCHEMA_REGISTRY);
        // registry schema in the schema registry if not found
        InvoiceAvroSerde.configure(serdeConfig, true); // `true` for record keys
        CustomerAvroSerde.configure(serdeConfig, true); // `true` for record keys
        CustomerInvoiceAvroSerde.configure(serdeConfig, true); // `true` for record keys

        // In the subsequent lines we define the processing topology of the Streams application.
        final StreamsBuilder builder = new StreamsBuilder();
        // a Stream is a consumer
        final KStream<String, Invoice> invoiceStream = builder.stream("invoices");

        final GlobalKTable<String, Customer>
                customers =
                builder.globalTable("customers", Materialized.<String, Customer, KeyValueStore<Bytes, byte[]>>as("customers-store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(CustomerAvroSerde));

        final KStream<String, CustomerInvoice> customerOrdersStream = invoiceStream.join(customers,
                (invoiceId, invoice) -> invoice.getCustomerId().toString(),
                (invoice, customer) -> new CustomerInvoice(invoice.getId(), customer.getId(), invoice.getAmount(), customer.getAge(), invoice.getState(), invoice.getCountry(), customer.getGender(), invoice.getInvoiceDate()));

        customerOrdersStream.foreach(new ForeachAction<String, CustomerInvoice>() {
            @Override
            public void apply(String key, CustomerInvoice customerInvoice) {
                System.out.println("Invoice Key " + key + "  value id  " + customerInvoice.getCustomerId() + ":" + customerInvoice.getAmount() );
                System.out.println("received invoice " + customerInvoice);
            }
        });

        customerOrdersStream.to("customer_invoices", Produced.with(Serdes.String(), CustomerInvoiceAvroSerde));

        // collection of streams put together
        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

//
//        ReadOnlyKeyValueStore view = streams.store("customers-store", QueryableStoreTypes.keyValueStore());
//        Object o = view.get("1000"); // can be done on any key, as all keys are present
//        System.out.println("Value " + o);
//
//         KeyValueIterator<Object, Customer> all = view.all();
//
//         all.forEachRemaining(keyValue -> System.out.println(keyValue));

        System.out.println("Stream started");

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}