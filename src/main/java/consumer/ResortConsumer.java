package consumer;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import consumer.listener.ResortListener;
import consumer.model.LiftRide;
import redis.clients.jedis.JedisPool;

import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class ResortConsumer {
  private static final String QUEUE_NAME = "SkierQueue";
  private static final ConcurrentHashMap<String, List<LiftRide>> resortDayToLiftRides = new ConcurrentHashMap<>();
  private static final ReentrantLock lock = new ReentrantLock();

  public static void main(String[] args) throws Exception {
    int numThreads = Integer.parseInt(args[0]);
    ExecutorService executorService = new ThreadPoolExecutor(
            numThreads,
            numThreads,
            10000L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>()
    );
    SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
            new ProviderConfiguration(),
            AmazonSQSClientBuilder.defaultClient()
    );
    JedisPool jedisPool = new JedisPool("ec2-34-221-47-73.us-west-2.compute.amazonaws.com", 6379);
//    JedisPool jedisPool = new JedisPool("localhost", 6379);
    Runnable startSqsConsumerTask = () -> {
      try {
        startSqsConsumer(connectionFactory, jedisPool);
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
    for (int i = 0; i < numThreads; i++) {
      executorService.execute(startSqsConsumerTask);
    }
    System.out.println("Listening for messages...");
  }

  private static void startSqsConsumer(SQSConnectionFactory connectionFactory, JedisPool jedisPool) throws Exception {
    SQSConnection connection = connectionFactory.createConnection();
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Queue queue = session.createQueue(QUEUE_NAME);
    MessageConsumer consumer = session.createConsumer(queue);
    consumer.setMessageListener(new ResortListener(resortDayToLiftRides, jedisPool, lock));
    connection.start();
  }
}
