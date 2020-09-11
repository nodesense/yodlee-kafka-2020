package kafka.workshop;

// SimpleProducer.java
// kafka-console-consumer --bootstrap-server k17.training.sh:9092 --topic messages  --from-beginning --property print.key=true --property print.timestamp=true

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

// kafka-topics --create --zookeeper k17.training.sh:2181 --replication-factor 1 --partitions 3 --topic messages



public class SimpleProducer {

    public static String TOPIC = "greetings";


    public static String[] greetingMessages = new String[] {
            "Good afternoon!!",
            "Good morning!!",
            "How are you?",
            "Hope this email finds you well!!",
            "I hope you enjoyed your weekend!!",
            "I hope you're doing well!!",
            "I hope you're having a great week!!",
            "I hope you're having a wonderful day!!",
            "It's great to hear from you!!",
            "I'm eager to get your advice on...",
            "I'm reaching out about...",
            "Thank you for your help",
            "Thank you for the update",
            "Thanks for getting in touch",
            "Thanks for the quick response",
            "Happy Diwali",
            "Happy New Year",
            "Wish you a merry Christmas",
            "Good afternoon!!",
            "Good morning!!",
            "How are you?",
            "Hope this email finds you well!!",
            "I hope you enjoyed your weekend!!",
            "I hope you're doing well!!",
            "I hope you're having a great week!!",
            "I hope you're having a wonderful day!!",
            "It's great to hear from you!!",
            "I'm eager to get your advice on...",
            "I'm reaching out about...",
            "Thank you for your help",
            "Thank you for the update",
            "Thanks for getting in touch",
            "Thanks for the quick response",
            "Happy Diwali",
            "Happy New Year",
            "Wish you a merry Christmas",
            "Good afternoon!!",
            "Good morning!!",
            "How are you?",
            "Hope this email finds you well!!",
            "I hope you enjoyed your weekend!!",
            "I hope you're doing well!!",
            "I hope you're having a great week!!",
            "I hope you're having a wonderful day!!",
            "It's great to hear from you!!",
            "I'm eager to get your advice on...",
            "I'm reaching out about...",
            "Thank you for your help",
            "Thank you for the update",
            "Thanks for getting in touch",
            "Thanks for the quick response",
            "Happy Diwali",
            "Happy New Year",
            "Wish you a merry Christmas",
            "Good afternoon!!",
            "Good morning!!",
            "How are you?",
            "Hope this email finds you well!!",
            "I hope you enjoyed your weekend!!",
            "I hope you're doing well!!",
            "I hope you're having a great week!!",
            "I hope you're having a wonderful day!!",
            "It's great to hear from you!!",
            "I'm eager to get your advice on...",
            "I'm reaching out about...",
            "Thank you for your help",
            "Thank you for the update",
            "Thanks for getting in touch",
            "Thanks for the quick response",
            "Happy Diwali",
            "Happy New Year",
            "Wish you a merry Christmas",
            "Good afternoon!!",
            "Good morning!!",
            "How are you?",
            "Hope this email finds you well!!",
            "I hope you enjoyed your weekend!!",
            "I hope you're doing well!!",
            "I hope you're having a great week!!",
            "I hope you're having a wonderful day!!",
            "It's great to hear from you!!",
            "I'm eager to get your advice on...",
            "I'm reaching out about...",
            "Thank you for your help",
            "Thank you for the update",
            "Thanks for getting in touch",
            "Thanks for the quick response",
            "Happy Diwali",
            "Happy New Year",
            "Wish you a merry Christmas",
            "Good afternoon!!",
            "Good morning!!",
            "How are you?",
            "Hope this email finds you well!!",
            "I hope you enjoyed your weekend!!",
            "I hope you're doing well!!",
            "I hope you're having a great week!!",
            "I hope you're having a wonderful day!!",
            "It's great to hear from you!!",
            "I'm eager to get your advice on...",
            "I'm reaching out about...",
            "Thank you for your help",
            "Thank you for the update",
            "Thanks for getting in touch",
            "Thanks for the quick response",
            "Happy Diwali",
            "Happy New Year",
            "Wish you a merry Christmas",
            "Good afternoon!!",
            "Good morning!!",
            "How are you?",
            "Hope this email finds you well!!",
            "I hope you enjoyed your weekend!!",
            "I hope you're doing well!!",
            "I hope you're having a great week!!",
            "I hope you're having a wonderful day!!",
            "It's great to hear from you!!",
            "I'm eager to get your advice on...",
            "I'm reaching out about...",
            "Thank you for your help",
            "Thank you for the update",
            "Thanks for getting in touch",
            "Thanks for the quick response",
            "Happy Diwali",
            "Happy New Year",
            "Wish you a merry Christmas",
    };


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


        int counter = 0;
        for (int i = 0 ; i < 1000; i++) {
            for (String message:greetingMessages) {
                // producer record, topic, key (null), value (message)
                // send message, not waiting for ack
                // Key shall be Message0, Message1, Message2, .... so on
                String key = "Message" + counter ;
                // Value Message0 Good Morning
                String value = counter + " " + message;

                //record is a message to kafka
                ProducerRecord record = new ProducerRecord<>(TOPIC, key, value);

                // sending message to broker,
                    // 1. sync, blocking call, send message, wait for ack from broker, then proceed
                    // 2. async, send message in async way, doens't wait for broker response,
                    // ack received in callback


                // Send message using sync way
                // producer send the record to a topic
                // waiting for producer response, RecordMetadata
                // meta shall have offset, timestamp, other information
                // .send() basically uses separate worker thread to send message to broker
                // .send() ensure that it decides the partition before sending message using partitioner class
                // try/catch if the writing to producer fails
                RecordMetadata metadata = (RecordMetadata) producer.send(record).get(); // sync, blocking

                System.out.printf("Greeting %d - %s sent\n", counter, message);
                System.out.println("Ack offset " + metadata.offset() + " partition " + metadata.partition());

                Thread.sleep(5000); // Demo only,
                counter++;
            }
        }

        producer.close();
    }

}