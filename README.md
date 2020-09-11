# KAFKA Workshop Yodlee

```
  java -jar ./lib/avro-tools-1.9.1.jar compile schema ./src/main/resources/avro/ ./src/main/java
```


http -v POST k17.training.sh:8081/subjects/customer_invoices-value/versions \
  Accept:application/vnd.schemaregistry.v1+json \
  schema=@src/main/resources/avro/customer-invoice.avsc
