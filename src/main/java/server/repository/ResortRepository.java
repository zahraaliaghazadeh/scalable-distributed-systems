package server.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import server.model.LiftRide;

import java.util.ArrayList;
import java.util.List;

public class ResortRepository {
  private JedisPool jedisPool;
  private Gson gson;

  public ResortRepository() {
//    JedisPool jedisPool = new JedisPool("ec2-34-221-47-73.us-west-2.compute.amazonaws.com", 6379);
    jedisPool = new JedisPool("localhost", 6379);
    gson = new Gson();
  }

  private List<LiftRide> getLiftRidesByResortAndDay(int resortId, String dayId) {
    System.out.println("Getting lift rides for resort ID: " + resortId + " and dayId: " + dayId);
    try (Jedis jedis = jedisPool.getResource()) {
      List<LiftRide> liftRides = gson.fromJson(
              jedis.get(getResortDayId(resortId, dayId)),
              new TypeToken<ArrayList<LiftRide>>(){}.getType()
      );
      System.out.println("Received lift rides: " + liftRides);
      return liftRides;
    } catch (Exception e){
      throw new RuntimeException(e);
    }
  }

  public long getNumUniqueSkiers(int resortId, String seasonId, String dayId) {
    final List<LiftRide> liftRides = getLiftRidesByResortAndDay(resortId, dayId);
    return liftRides.stream()
            .filter(liftRide -> (liftRide.getSeasonId().equals(seasonId)))
            .map(LiftRide::getSkierId)
            .distinct()
            .count();
  }

  private String getResortDayId(int resortId, String dayId) {
    return String.format("resortId:%d-dayId:%s", resortId, dayId);
  }

}
