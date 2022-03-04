package consumer;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import consumer.model.LiftRide;

import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import java.util.concurrent.*;

public class SqsConsumer {
  private static final String QUEUE_NAME = "SkierQueue";
  private static final ConcurrentHashMap<Integer, LiftRide> skierToLiftRide = new ConcurrentHashMap<>();

  public static void main(String[] args) throws Exception {
    int numThreads = Integer.parseInt(args[0]);
    ExecutorService executorService = new ThreadPoolExecutor(
            numThreads,
            numThreads,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>()
    );
    SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
            new ProviderConfiguration(),
            AmazonSQSClientBuilder.defaultClient()
    );
    Runnable startSqsConsumerTask = () -> {
      try {
        startSqsConsumer(connectionFactory);
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
    for (int i = 0; i < numThreads; i++) {
      executorService.execute(startSqsConsumerTask);
    }
  }

  private static void startSqsConsumer(SQSConnectionFactory connectionFactory) throws Exception {
    SQSConnection connection = connectionFactory.createConnection();
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Queue queue = session.createQueue(QUEUE_NAME);
    MessageConsumer consumer = session.createConsumer(queue);
    consumer.setMessageListener(new SkierListener(skierToLiftRide));
    connection.start();
  }
}
