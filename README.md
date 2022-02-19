# scalable-distributed-systems

1. Run server on IntelliJ
2. Run client: `mvn exec:java -Dexec.mainClass=client.SkierClient -Dexec.args="16 128 20 20 ec2-34-222-164-46.us-west-2.compute.amazonaws.com:8080"`
   

## write up

Since the client is relatively small, all the client logic is in SkierClient class. Each phase is split into its own function for better modularity. On each phase, there is a wait with CountDownLatch to signify when the next phase could start. There are 2 Plain-Old-Java-Object (POJO) classes within the SkierClient, which are RequestBodySkier and RequestPerformance. RequestBodySkier is responsible for modeling the request payload to the server (which is transformed into JSON), while RequestPerformance is used for tracking the performance of each individual request for the required calculations.

For predicting with Little’s Law, I ran a piece of code that sends 10,000 requests to the server in a single thread. After running it 3 times, the average runtime is 685 seconds.

Using Little’s law, the predicted throughput is 14.60 requests/s for a single threaded client

λ = L / W

λ: Throughput
L: Number of requests
W: Latency

λ = 10,000 / 685 = 14.60 requests/s

Assuming 8 threads, expected throughput is 8 * 14.60 = 116.80 requests/s

Here’s the results from running the client with the following command

mvn exec:java -Dexec.mainClass=client.SkierClient -Dexec.args="32 512 20 20 ec2-34-222-164-46.us-west-2.compute.amazonaws.com:8080"

Successful Requests: 7955
Failed Requests: 0
Total run time: 111448 ms
Total throughput: 71.37858014500037 requests/s
Mean response time = 368.64472361809044 ms
Median response time = 384.0 ms
Throughput = 71.42344411743593 requests/sec
Min response time = 59 ms
Max response time = 22656 ms
p99 response time = 3423 ms

As shown, the actual throughput is similar to the predicted throughput.

