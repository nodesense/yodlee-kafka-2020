// WordCount.java
package kafka.workshop;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.time.Duration;
import java.util.*;

// kafka-topics --zookeeper k17.training.sh:2181 --create --topic words --replication-factor 1 --partitions 3

// kafka-topics --zookeeper k17.training.sh:2181 --create --topic words-count-output --replication-factor 1 --partitions 3

// kafka-topics --zookeeper k17.training.sh:2181 --create --topic words-count-windowed-output --replication-factor 1 --partitions 3

// producer

// kafka-console-producer --broker-list k17.training.sh:9092 --topic words


//consumer

// kafka-console-consumer --bootstrap-server k17.training.sh:9092 --topic words-count-output --from-beginning --property print.key=true  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer


   //     kafka-console-consumer --bootstrap-server k17.training.sh:9092 --topic words-count-windowed-output --from-beginning --property print.key=true  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer


import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// welcome to kafka, scala, java session
// kafka session starts now
// welcome - 1
// to - 1
// scala - 1
// kafka - 2
// session - 2
// starts - 1
// now - 1

// outputs produced to another kafka topics key (kafka/string, 1/long)

public class WordCountStream {

    public static Properties getConfiguration() {


        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count2-stream");
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "word-count2-stream-client");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Settings.BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1 * 1000);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);


        props.put("schema.registry.url", Settings.SCHEMA_REGISTRY);
        return props;
    }


    public static void main(final String[] args) throws Exception {
        System.out.println("Running WordCount Stream");

        Properties props = getConfiguration();

        // key
        final Serde<String> stringSerde = Serdes.String();
        // value count of the word
        final Serde<Long> longSerde = Serdes.Long();

        // In the subsequent lines we define the processing topology
        // of the Streams application.
        final StreamsBuilder builder = new StreamsBuilder();

        // start building topology
        // Source Processor, a kafka Consumer
        // key is null, value is  a sentence
        final KStream<String, String> lines = builder
                .stream("words");

        // does nothing but to print data coming in  the stream
        lines.foreach(new ForeachAction<String, String>() {
            @Override
            public void apply(String key, String value) {
                System.out.println("Full Line " + key + " Value is  *" + value + "*" );
            }
        });

        // Stream Processor, to filter out all the empty lines, empty lines shall not be processed
        final KStream<String, String> nonEmptyLines = lines.filter( (key, value) -> !value.isEmpty());

        // stream process, split the sentence into word
        // welcome to kafka =>  [welcome, to, kafka]
        // flat map shall convert array into elememnt
        // example  [welcome, to, kafka], to
        //                      welcome
        //                      to
        //                      kafka
        KStream<String, String> splitWords = nonEmptyLines
                .flatMapValues(line -> Arrays.asList(line.toLowerCase().split("\\W+")));

        // receive the words as input
        splitWords.foreach(new ForeachAction<String, String>() {
            @Override
            public void apply(String key, String value) {
                System.out.println("Split Word " + key + " Value is  *" + value + "*" );
            }
        });


        // splitwords has individual words as input, "apple", "orange", "apple"

        // KTable aggregation, called changelog stream from the keyed table
        // | word         |   count
        // | kafka        | 2
        // | to           | 1

        KTable<String, Long> wordCount = splitWords
                .groupBy((_$, word) -> word)
                .count();


        // convert ktable into stream
        // whenver ktable changes, it proces teh change record as output
        KStream<String, Long> wordCountStream = wordCount.toStream();


        wordCountStream.foreach(new ForeachAction<String, Long>() {
            @Override
            public void apply(String key, Long value) {
                System.out.println("Word Count " + key + " Count  *" + value + "*" );
            }
        });


        // Producer, output to kafka topicm"words-count-output" with key string, value long type
        wordCountStream.to("words-count-output", Produced.with(stringSerde, longSerde));

        // done building topology



          splitWords
                .groupBy((_$, word) -> word)
                .windowedBy(TimeWindows.of(Duration.ofSeconds(120)))
                .count()
                .toStream()
                .foreach((windowedWord, count) -> {
                        System.out.println();
                        System.out.print("Window ");
                        System.out.print(" Start " + windowedWord.window().start());
                        System.out.print(" End " + windowedWord.window().end());
                        System.out.println("   word  " + windowedWord.key() + " Count  " + count);
                });

        KStream<String, Long> windowedCount = splitWords
                .groupBy((_$, word) -> word)
                .windowedBy(TimeWindows.of(Duration.ofSeconds(120)))
                .count()
                .toStream()
                .map((windowedKey, value) -> new KeyValue<>(windowedKey.key(), value));

                 windowedCount
                .foreach((word, count) -> {
                    System.out.println();
                    System.out.print("Window ");
                    System.out.println("   word  " + word + " Count  " + count);
                });

        windowedCount.to("words-count-windowed-output", Produced.with(stringSerde, longSerde));


        // create instance
        final KafkaStreams streams = new KafkaStreams(builder.build(), props);

        try {
            // for stateful opeartions, right now not used
            streams.cleanUp();
        }catch(Exception e) {
            System.out.println("Error While cleaning state" + e);
        }

        // start the instance,
        // create threads, create tasks, execute teh tasks
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}