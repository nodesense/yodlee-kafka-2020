MYSQL

MySQL JDBC not included in Kafka Distribution

Devleopers to download jdbc jars of oracle/mysql/ms sql


wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz

tar xf mysql-connector-java-5.1.47.tar.gz
cp mysql-connector-java-5.1.47/*.jar confluent-5.5.1/share/java/kafka-connect-jdbc


                       
Implmenting MYSQL Source connector
 Whenever new record inserted/updated, publish to kafka
 JDBC Datasources, can use Avro, Schema registry
 It automatically build avro schema from database table
 
 
## Setup the database 

```
mysql -uroot


CREATE USER 'team'@'localhost' IDENTIFIED BY 'team1234';

CREATE DATABASE ecommerce; 

GRANT ALL PRIVILEGES ON ecommerce.* TO 'team'@'localhost';

```

Exit from the shell

```

mysql -uroot

USE ecommerce;

-- detect insert/update changes using timestamp , but HARD delete

create table products (id int, 
                       name varchar(255), 
                       price int, 
                       create_ts timestamp DEFAULT CURRENT_TIMESTAMP, 
                       update_ts timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP );
                       
             
```             
Settings can be done in json or properties file

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
     "_comment": "The Kafka topic will be made up of this prefix, plus the table name, kafka topic shall be db_products  ",
     "topic.prefix": "db_"
   }
 }   

``` 
 
### END PROPERTY JSON CONFIGURATION


 Note: the topic shall be <<PREFIX>>+<<TableName>> example: db_products
 
``` 
 confluent load mysql-product-source -d mysql-product-source.json
 
 confluent status connectors
 
 confluent status mysql-product-source
``` 

### Run below on your system or different putty/ssh

```
 kafka-avro-console-consumer --bootstrap-server k17.training.sh:9092 --topic db_products --from-beginning --property schema.registry.url="http://k17.training.sh:8081"
```

### Use Different Putty 

```
 mysql -uroot
 
 USE ecommerce;


 insert into products (id, name, price) values(1,'google phone', 2000); 
 
 
 insert into products (id, name, price) values(2,'nexus phone', 3000); 
 
 update products set price=3333 where id=1; 

``` 

### CHECK IN Browser for Schema registration


 go and check http://k5.nodesense.ai:8081/subjects  
 to ensure that you have schema for products listed
 
 http://k5.nodesense.ai:8081/subjects/db_products-value/versions/1
 
  
  java -jar ./lib/avro-tools-1.8.2.jar compile schema ./src/main/resources/avro/product.avsc ./src/main/java

## MYSQL SINK Connectors
  Consume from Topics, write to database
  kafka-avro-console-producers
  
  To be demonstrated
  
   
touch  mysql-product-sink.properties

nano  mysql-product-sink.properties

name=mysql-product-sink
connector.class=io.confluent.connect.jdbc.JdbcSinkConnector
tasks.max=1
topics=products
connection.url=jdbc:mysql://localhost:3306/ecommerce?user=team&password=team1234
auto.create=true
key.converter=io.confluent.connect.avro.AvroConverter
key.converter.schema.registry.url=http://k5.nodesense.ai:8081
value.converter=io.confluent.connect.avro.AvroConverter
value.converter.schema.registry.url=http://k5.nodesense.ai:8081


consume from products topics, write to products table

To write  to specific table name,

use belwo property

table.name.format=mst_${topic}

table.name.format=somename_table


confluent load mysql-product-sink -d  mysql-product-sink.properties

Check with below command.

kafka-avro-console-producer --broker-list k5.nodesense.ai:9092 --topic products --property value.schema='{"type":"record","name":"product","fields":[{"name":"id","type":"int"},{"name":"name", "type": "string"}, {"name":"price", "type": "int"}]}'  --property schema.registry.url="http://k5.nodesense.ai:8081"
   

paste below line one after another without new line

{"id": 999, "name": "asus pro", "price": 100}


-- in db,

select * from products;

check whether id 999 appears ot not
