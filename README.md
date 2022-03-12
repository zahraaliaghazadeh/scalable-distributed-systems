# distributed-systems-NU

1. Run server on IntelliJ
2. Run client: `mvn exec:java -Dexec.mainClass=client.SkierClient -Dexec.args="64 2000 40 10 skieralb-1580553055.us-west-2.elb.amazonaws.com"`
3. Run SQS Consumer: `mvn exec:java -Dexec.mainClass=consumer.SqsConsumer -Dexec.args="<num_threads>"`
  * e.g. `mvn exec:java -Dexec.mainClass=consumer.SqsConsumer -Dexec.args="2"`
