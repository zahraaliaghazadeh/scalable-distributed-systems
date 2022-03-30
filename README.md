# distributed-systems-NU

1. Run server on IntelliJ
2. Run client: `mvn exec:java -Dexec.mainClass=client.SkierClient -Dexec.args="64 2000 40 10 skieralb-1580553055.us-west-2.elb.amazonaws.com"`
3. Run Skier Consumer: `mvn exec:java -Dexec.mainClass=consumer.SkierConsumer -Dexec.args="<num_threads>"`
  * e.g. `mvn exec:java -Dexec.mainClass=consumer.SkierConsumer -Dexec.args="2"`
4. Run Resort Consumer: `mvn exec:java -Dexec.mainClass=consumer.ResortConsumer -Dexec.args="<num_threads>"`
  * e.g. `mvn exec:java -Dexec.mainClass=consumer.ResortConsumer -Dexec.args="2"`


## Misc

To Run Redis on EC2

1. SSH onto EC2: `ssh -i yalda.pem ec2-user@ec2-54-68-111-95.us-west-2.compute.amazonaws.com`
2. `sudo redis-server /etc/redis/redis.conf`