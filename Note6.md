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

