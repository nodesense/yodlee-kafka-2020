whatever is earliest
BATCH_SIZE_CONFIG = 1000 bytes
LINGER_MS_CONFIG = 5 seconds

producer send msg 1 [500 bytes] - 12:00:01 [buffered, not send]
producer send msg 2 [500 bytes] - 12:00:02 [buffered, send [msg 1, msg 2] since 1000 bytes reached, two messages batched in single request]
producer send msg 3 [500 bytes] - 12:00:03 [buffered msg 1, 500 bytes]
No more messages for next 5 seconds
                                - 12:00:08 [send msg 3, 500 bytes to the broker]
