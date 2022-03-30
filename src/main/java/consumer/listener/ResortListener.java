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
import java.util.concurrent.locks.ReentrantLock;

public class ResortListener implements MessageListener {
  private Map<String, List<LiftRide>> resortDayToLiftRides;
  private JedisPool jedisPool;
  private ReentrantLock lock;

  public ResortListener(Map<String, List<LiftRide>> resortDayToLiftRides, JedisPool jedisPool, ReentrantLock lock) {
    this.resortDayToLiftRides = resortDayToLiftRides;
    this.jedisPool = jedisPool;
    this.lock = lock;
  }

  @Override
  public void onMessage(Message message) {
    try {
      System.out.println("Received: " + ((TextMessage) message).getText());

      Gson gson = new Gson();
      LiftRide liftRide = gson.fromJson(((TextMessage) message).getText(), LiftRide.class);
      String id = String.format("resortId:%d-dayId:%s", liftRide.getResortId(), liftRide.getDayId());

      lock.lock();
      List<LiftRide> liftRides = resortDayToLiftRides.getOrDefault(id, new ArrayList<>());
      liftRides.add(liftRide);
      resortDayToLiftRides.put(id, liftRides);

      try (Jedis jedis = jedisPool.getResource()) {
        jedis.set(id, gson.toJson(liftRides));
      }
      lock.unlock();

    } catch (JMSException e) {
      e.printStackTrace();
    }
  }
}
