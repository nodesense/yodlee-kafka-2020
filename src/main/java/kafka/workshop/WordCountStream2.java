// WordCount.java
package kafka.workshop;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

// kafka-topics --zookeeper localhost:2181 --create --topic words --replication-factor 3 --partitions 3
//// kafka-topics --zookeeper localhost:2181 --create --topic words-count-output --replication-factor 3 --partitions 3
// kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic words-count-output --from-beginning --property print.key=true  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
//     kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic words-count-windowed-output --from-beginning --property print.key=true  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

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

public class WordCountStream2 {

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


//          splitWords
//                .groupBy((_$, word) -> word)
//                .windowedBy(TimeWindows.of(Duration.ofSeconds(120)))
//                .count()
//                .toStream()
//                .foreach((windowedWord, count) -> {
//                        System.out.println("Starting " + windowedWord.window().start());
//                        System.out.println("End " + windowedWord.window().end());
//                        System.out.println("Windows word is " + windowedWord.key() + " Count is " + count);
//                });


        //  .windowedBy( SessionWindows.with(TimeUnit.MINUTES.toMillis(5)))) /* session window */
        // TimeWindows.of()  tumbling windows

        /* Hopping Window
        long windowSizeMs = TimeUnit.MINUTES.toMillis(5); // 5 * 60 * 1000L

        long advanceMs =    TimeUnit.MINUTES.toMillis(1); // 1 * 60 * 1000L
        .windowedBy( TimeWindows.of(windowSizeMs).advanceBy(advanceMs))
        */

//        splitWords
//                .groupBy((_$, word) -> word)
//                .windowedBy(TimeWindows.of(Duration.ofSeconds(120)))
//                .count()
//                .toStream()
//                // Convert Windows<String> to <Stirng, Value>
//                .map( (windowedKey, value) -> new KeyValue<>(windowedKey.key(), value) )
//                .foreach((word, count) -> {
//                    System.out.println("Windows word is " + word + " Count is " + count);
//                });
//
//        KStream<String, Long> windowed = splitWords
//                .groupBy((_$, word) -> word)
//                 .windowedBy(TimeWindows.of(Duration.ofSeconds(120)))
//                .count()
//                .toStream()
//                .map( (key, value) -> new KeyValue<>(key.key(), value) );
//
//
//        windowed.to("words-count-windowed-output", Produced.with(stringSerde, longSerde));
//
//
//
//        wordCountStream.foreach(new ForeachAction<String, Long>() {
//            @Override
//            public void apply(String word, Long count) {
//                System.out.println("Word " + word + " Count is  *" + count + "*" );
//            }
//        });


        // STREAM PROCESSSING

        // Producer
        wordCountStream.to("words-count-output", Produced.with(stringSerde, longSerde));




        final KafkaStreams streams = new KafkaStreams(builder.build(), props);

        try {
            streams.cleanUp();
        }catch(Exception e) {
            System.out.println("Error While cleaning state" + e);
        }
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}