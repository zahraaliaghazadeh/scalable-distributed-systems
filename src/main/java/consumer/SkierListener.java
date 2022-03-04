package consumer;

import com.google.gson.Gson;
import consumer.model.LiftRide;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Map;

public class SkierListener implements MessageListener {
  private Map<Integer, LiftRide> skierToLiftRide;

  public SkierListener(Map<Integer, LiftRide> skierToLiftRide) {
    this.skierToLiftRide = skierToLiftRide;
  }

  @Override
  public void onMessage(Message message) {
    try {
      System.out.println("Received: " + ((TextMessage) message).getText());

      Gson gson = new Gson();
      LiftRide liftRide = gson.fromJson(((TextMessage) message).getText(), LiftRide.class);
      skierToLiftRide.put(liftRide.getSkierId(), liftRide);
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }
}
