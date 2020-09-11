

confluent local list connectors

confluent local status connectors

File Connector, File Source connector
    input file, read from teh file stocks.csv, watch the file change,
    publish to kafka topic called stocks
 
Create the file 


```
touch stocks.csv
```
 
 

Load the source connector / run the connector

touch stock-file-source.json

nano stock-file-source.json

and below content 

```

{
 "name": "stock-file-source",
 "config": {
     "connector.class": "FileStreamSource",
     "tasks.max": "1",
    "file": "/root/stocks.csv",
    "topic": "stocks"
     }
 }
```


```
confluent local load stock-file-source -- -d stock-file-source.json
```

Check whether connector is running or not

```
confluent local status connectors
```

check specific connector status 

```
confluent local status stock-file-source

```

start consumer on stocks topic

``` 
kafka-console-consumer --bootstrap-server k17.training.sh:9092 --topic stocks --from-beginning

```

Put some data into csv file

```
echo "1234,10" >> stocks.csv

echo "1235,20" >> stocks.csv

echo "1236,30" >> stocks.csv


cat stocks.csv
```


to unload kafka connector running? 

```
confluent local unload stock-file-source
```



# File Sink connector


Ensure simpleproducer.java topic should be greetings

```
public class SimpleProducer {

    public static String TOPIC = "greetings";

}
```

```
touch greetings.txt


touch greetings-file-sink.json

nano greetings-file-sink.json

```

paste below content

```
{
 "name": "greetings-file-sink",
 "config": {
     "connector.class": "FileStreamSink",
     "tasks.max": "1",
    "file": "/root/greetings.txt",
    "topics": "greetings",
"key.converter": "org.apache.kafka.connect.storage.StringConverter",
"value.converter": "org.apache.kafka.connect.storage.StringConverter"
     }
 }
```
 
## Done

```
confluent local load greetings-file-sink -- -d greetings-file-sink.json
confluent local status greetings-file-sink
```

```
cat greetings.txt
```


```
confluent local  unload greetings-file-sink

```


# Invoices to file sink



## One last example Avro and file sink

```
touch invoices.txt


touch invoices-file-sink.json

nano invoices-file-sink.json

```

paste below content



 

```
{
 "name": "invoices-file-sink",
 "config": {
     "connector.class": "FileStreamSink",
     "tasks.max": "1",
    "file": "/root/invoices.txt",
    "topics": "invoices",
    "key.converter": "io.confluent.connect.avro.AvroConverter",
    "value.converter": "io.confluent.connect.avro.AvroConverter",
    "key.converter.schema.registry.url": "http://k17.training.sh:8081",
    "value.converter.schema.registry.url": "http://k17.training.sh:8081"
     }
 }
```


```
confluent local load invoices-file-sink -- -d invoices-file-sink.json

confluent local status invoices-file-sink

```
 

## DONE


confluent load invoices-file-sink -d invoices-file-sink.properties
confluent status invoices-file-sink
 
cat invoices.txt

================

# MYSQL


### START PROPERTY JSON CONFIGURATION

```
touch mysql-product-source.json
 
nano  mysql-product-source.json
```
   paste below
```
   {
   "name": "mysql-product-source",
   "config": {
     "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
     "key.converter": "io.confluent.connect.avro.AvroConverter",
     "key.converter.schema.registry.url": "http://k17.training.sh:8081",
     "value.converter": "io.confluent.connect.avro.AvroConverter",
     "value.converter.schema.registry.url": "http://k17.training.sh:8081",
     "connection.url": "jdbc:mysql://localhost:3306/ecommerce?user=team&password=team1234",
     "_comment": "Which table(s) to include",
     "table.whitelist": "products",
     "mode": "timestamp",
      "timestamp.column.name": "update_ts",
     "validate.non.null": "false",
     "_comment": "The Kafka topic will be made up of this prefix, plus the table name  ",
     "topic.prefix": "db_"
   }
 }
```
 
### END PROPERTY JSON CONFIGURATION


 Note: the topic shall be <<PREFIX>>+<<TableName>> example: db_products
 
 ```
 confluent local load mysql-product-source -- -d mysql-product-source.json
 
 confluent local status connectors
 
 confluent local status mysql-product-source
 
 ```



 kafka-avro-console-consumer --bootstrap-server k17.training.sh:9092 --topic db_products --from-beginning --property schema.registry.url="http://k17.training.sh:8081"
 
 
 # MySQL Sink connectors
 
 
## MYSQL SINK Connectors
  Consume from Topics, write to database
  kafka-avro-console-producers
  
  
```
touch  mysql-product-sink.json

nano  mysql-product-sink.json
```


```
   {
   "name": "mysql-product-sink",
   "config": {
     "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
     "topics": "products",
    
    "key.converter": "io.confluent.connect.avro.AvroConverter",
    "key.converter.schema.registry.url" : "http://k17.training.sh:8081",
    "value.converter" : "io.confluent.connect.avro.AvroConverter",
    "value.converter.schema.registry.url" : "http://k17.training.sh:8081",   
     
     "connection.url": "jdbc:mysql://localhost:3306/ecommerce?user=team&password=team1234",
     "auto.create": true
   }
 }
```

 confluent local load mysql-product-sink -- -d  mysql-product-sink.json


kafka-avro-console-producer --broker-list k17.training.sh:9092 --topic products --property value.schema='{"type":"record","name":"product","fields":[{"name":"id","type":"int"},{"name":"name", "type": "string"}, {"name":"price", "type": "int"}]}'  --property schema.registry.url="http://k17.training.sh:8081"
   







 
 