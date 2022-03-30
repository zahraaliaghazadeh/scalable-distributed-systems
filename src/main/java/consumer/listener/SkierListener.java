package consumer.listener;

import com.google.gson.Gson;
import consumer.model.LiftRide;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkierListener implements MessageListener {
  private Map<String, List<LiftRide>> skierToLiftRides;
  private JedisPool jedisPool;

  public SkierListener(Map<String, List<LiftRide>> skierToLiftRides, JedisPool jedisPool) {
    this.skierToLiftRides = skierToLiftRides;
    this.jedisPool = jedisPool;
  }

  @Override
  public void onMessage(Message message) {
    try {
      System.out.println("Received: " + ((TextMessage) message).getText());

      Gson gson = new Gson();
      LiftRide liftRide = gson.fromJson(((TextMessage) message).getText(), LiftRide.class);
      String id = String.format("skierId:%d", liftRide.getSkierId());
      List<LiftRide> liftRides = skierToLiftRides.getOrDefault(id, new ArrayList<>());
      liftRides.add(liftRide);
      skierToLiftRides.put(id, liftRides);
      try (Jedis jedis = jedisPool.getResource()) {
        jedis.set(id, gson.toJson(liftRides));
      }
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }
}
