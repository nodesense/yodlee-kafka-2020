Run all below in remote server.

Data Generator, KAfka provide data generators for demo/learning

open ssh1 shell

generate users every 5 seconds, publish to topic users, in avro format

```
ksql-datagen quickstart=users format=avro topic=users maxInterval=5000
```

open ssh2 shell,

below produce the records every 5 seconds, write to topic pageviews

```
ksql-datagen quickstart=pageviews format=avro topic=pageviews maxInterval=5000

```

http://k17.training.sh:8081/subjects/users-value/versions/1

http://k17.training.sh:8081/subjects/pageviews-value/versions/1


Open 3rd SSH Shell


# KSQL Shell /Putty/SSH

```
ksql 
```

prompt will appear

```
SHOW STREAMS;

SHOW TABLES;

CREATE STREAM users_stream (userid varchar, regionid varchar, gender varchar) WITH (kafka_topic='users', value_format='AVRO');

SHOW STREAMS;

DESCRIBE users_stream;

NON_PERSISTED QUERIES [Means, the output/result is not stored into KAfka Brokers]

select userid, regionid, gender from users_stream emit changes;

select userid, regionid, gender from users_stream where gender='FEMALE' emit changes;

select userid, regionid, gender from users_stream where gender='MALE' emit changes;

```

PERSISTED QUERIES [CREATE STREAM AS ] results written to Kafka
Will be runnign automatically, need to use TERMINATE command to stop them

```
CREATE STREAM users_female AS SELECT userid AS userid, regionid FROM users_stream where gender='FEMALE';

CREATE STREAM users_male AS SELECT userid AS userid, regionid FROM users_stream where gender='MALE';


 CREATE STREAM pageviews_stream (userid varchar, pageid varchar) WITH (kafka_topic='pageviews', value_format='AVRO');
 
 select * from pageviews_stream emit changes;

```
JOIN

```
CREATE STREAM user_pageviews_enriched_stream AS SELECT users_stream.userid AS userid, pageid, regionid, gender FROM pageviews_stream LEFT JOIN users_stream WITHIN 1 HOURS ON pageviews_stream.userid = users_stream.userid;

select * from user_pageviews_enriched_stream emit changes;

```
Ctrl +C to exit

```
CREATE TABLE pageviews_region_table WITH (VALUE_FORMAT='AVRO') AS SELECT gender, regionid, COUNT() AS numusers FROM user_pageviews_enriched_stream WINDOW TUMBLING (size 60 second) GROUP BY gender, regionid HAVING COUNT() >= 1;

select * from pageviews_region_table emit changes;

```

```
kafka-avro-console-consumer --bootstrap-server k17.training.sh:9092 --topic USERS_FEMALE --from-beginning --property schema.registry.url="http://k17.training.sh:8081"

kafka-avro-console-consumer --bootstrap-server k17.training.sh:9092 --topic PAGEVIEWS_REGION_TABLE --from-beginning --property schema.registry.url="http://k17.training.sh:8081"
```

List the persisted queries
```
SHOW QUERIES;
```
List of queries with Query ID



C***** - QUERY ID
```
EXPLAIN CTAS_PAGEVIEWS_REGION_TABLE_3; 

```
To stop the query / once stopped, cannot be restarted, need to run fresh query
```
TERMINATE  CTAS_PAGEVIEWS_REGION_TABLE_3;

DROP STREAM  users_male; 


DROP TABLE  pageviews_region;
```
 


[Example for Invoices]
 
 ```
CREATE STREAM invoices_stream (id varchar, qty int, amount int, customerId varchar, state varchar, country varchar) WITH (kafka_topic='invoices', value_format='AVRO');

SELECT * FRom invoices_stream emit changes;

CREATE TABLE invoices_state_count WITH (VALUE_FORMAT='AVRO') AS SELECT state,  COUNT() AS numorders FROM invoices_stream WINDOW TUMBLING (size 60 second) GROUP BY state  HAVING COUNT() >= 1;
SELECT * FRom invoices_state_count emit changes;
```


```
kafka-avro-console-consumer --bootstrap-server k17.training.sh:9092 --topic INVOICES_STATE_COUNT  --from-beginning --property schema.registry.url="http://k17.training.sh:8081"

```

Ctrl +C to exit