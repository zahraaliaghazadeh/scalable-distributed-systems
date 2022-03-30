import consumer.model.LiftRide;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.*;

public class Test {
  private static final String QUEUE_NAME = "SkierQueue";
  private static final ConcurrentHashMap<Integer, LiftRide> skierToLiftRide = new ConcurrentHashMap<>();

  public static void main(String[] args) throws Exception {
    JedisPool pool = new JedisPool("localhost", 6379);
    try (Jedis jedis = pool.getResource()) {
      jedis.set("foo", "brr");
    }
  }
}
