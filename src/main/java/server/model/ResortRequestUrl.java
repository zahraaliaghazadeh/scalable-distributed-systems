package server.model;

public class ResortRequestUrl {
  private int resortId;
  private String seasonId;
  private String dayId;

  public int getResortId() {
    return resortId;
  }

  public String getSeasonId() {
    return seasonId;
  }

  public void setSeasonId(String seasonId) {
    this.seasonId = seasonId;
  }

  public String getDayId() {
    return dayId;
  }

  public void setDayId(String dayId) {
    this.dayId = dayId;
  }

  public void setResortId(int resortId) {
    this.resortId = resortId;
  }
}
