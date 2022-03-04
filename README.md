# scalable-distributed-systems

1. Run server on IntelliJ
2. Run client: `mvn exec:java -Dexec.mainClass=client.SkierClient -Dexec.args="16 512 10 10 ec2-54-68-111-95.us-west-2.compute.amazonaws.com:8080"`
3. Run SQS Consumer: `mvn exec:java -Dexec.mainClass=consumer.SqsConsumer -Dexec.args="<num_threads>"`
* e.g. `mvn exec:java -Dexec.mainClass=consumer.SqsConsumer -Dexec.args="2"`
