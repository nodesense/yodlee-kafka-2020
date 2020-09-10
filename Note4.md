
JSON

{
 "orderId": 1000001,
 "amount": 5000,
 "customerId": 5324234343,
 "timestamp": 324344343434,
 "quantity": 15
} 

payload - 112 chars x 2 = 224 bytes of data

transmit 224 bytes to broker, store 224 bytes, retrive and serve consumer
HDD --> SSD/NVMe [Expensive]

JSON, every time, 
    serialize to json 
    deserialize from json to POJO

100 million of 224 bytes = 20.861625671386719 GB

AVRO
    orderId: Int,  4 bytes     1 st item
    amount: Double, 8 bytes  2nd item
    customerId: Long, 8 bytes  3rd item
    timestamp: Long, 8 bytes 4th item
    quanity: Int, 4 bytes  5 items
    
    
    attributes can be null or non null etc
    nested schemas
    
    Sum all bytes = 32 bytes
    
    shall consume 2.8 GB for 100 million records
     