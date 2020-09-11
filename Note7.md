# Compaction

```
kafka-topics --create --zookeeper k17.training.sh:2181 --topic latest-product-price --replication-factor 1 --partitions 1 --config "cleanup.policy=compact" --config "delete.retention.ms=100"  --config "segment.ms=100" --config "min.cleanable.dirty.ratio=0.01"
```

```
kafka-console-producer --broker-list k17.training.sh:9092 --topic latest-product-price --property parse.key=true --property key.separator=:
```

mobile1:6000
mobile2:9000
mobile1:5800

```
kafka-console-consumer --bootstrap-server k17.training.sh:9092 --topic latest-product-price --property  print.key=true --property key.separator=: --from-beginning
```