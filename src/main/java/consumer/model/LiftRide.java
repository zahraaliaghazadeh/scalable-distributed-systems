package consumer.model;

public class LiftRide {
  private int skierId;
  private int time;
  private int liftID;
  private int waitTime;

  public int getSkierId() {
    return skierId;
  }

  public void setSkierId(int skierId) {
    this.skierId = skierId;
  }

  public int getTime() {
    return time;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public int getLiftID() {
    return liftID;
  }

  public void setLiftID(int liftID) {
    this.liftID = liftID;
  }

  public int getWaitTime() {
    return waitTime;
  }

  public void setWaitTime(int waitTime) {
    this.waitTime = waitTime;
  }
}
