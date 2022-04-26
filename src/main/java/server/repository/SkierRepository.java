package server.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import server.model.LiftRide;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SkierRepository {
  private JedisPool jedisPool;
  private Gson gson;

  public SkierRepository() {
//    JedisPool jedisPool = new JedisPool("ec2-34-221-47-73.us-west-2.compute.amazonaws.com", 6379);
    jedisPool = new JedisPool("localhost", 6379);
    gson = new Gson();
  }

  private List<LiftRide> getLiftRidesBySkierId(int skierId) {
    System.out.println("Getting lift rides for skier ID: " + skierId);
    try (Jedis jedis = jedisPool.getResource()) {
      List<LiftRide> liftRides = gson.fromJson(
              jedis.get(getSkierIdKey(skierId)),
              new TypeToken<ArrayList<LiftRide>>(){}.getType()
      );
      System.out.println("Received lift rides: " + liftRides);
      return liftRides;
    } catch (Exception e){
      throw new RuntimeException(e);
    }
  }

  public int getDayVerticalBySkierId(
          int resortId,
          String seasonId,
          int skierId,
          String dayId
  ) {
    List<LiftRide> liftRides = getLiftRidesBySkierId(skierId);
    int total = liftRides.stream().filter(
            liftRide -> (
                    liftRide.getSeasonId().equals(seasonId)
                            && liftRide.getDayId().equals(dayId)
                            && liftRide.getResortId() == resortId
            )
    ).mapToInt(
            liftRide -> liftRide.getLiftId() * 10
    ).sum();
    System.out.println(String.format("Getting total [%d] vertical for day [%s]", total, dayId));
    return total;
  }

  public int getVerticalTotal(
          int resortId,
          int skierId,
          Optional<String> seasonId
  ) {
    List<LiftRide> liftRides = getLiftRidesBySkierId(skierId);
    int total = liftRides.stream().filter(
            liftRide -> (
                    liftRide.getResortId() == resortId
                    && seasonId.map(id -> liftRide.getSeasonId().equals(id)).orElse(true)
            )
    ).mapToInt(
            liftRide -> liftRide.getLiftId() * 10
    ).sum();
    System.out.println(
            String.format(
                    "Getting total [%d] vertical for resort [%d] and season [%s]",
                    total,
                    resortId,
                    seasonId.orElse("null")
            )
    );
    return total;
  }


  private String getSkierIdKey(int skierId) {
    return String.format("skierId:%d", skierId);
  }

}
