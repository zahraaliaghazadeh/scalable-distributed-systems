package server;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import server.model.LiftRide;
import server.model.SkierRequest;
import server.model.SkierRequestUrl;
import server.repository.SkierRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

public class SkierServlet extends HttpServlet {
  private final Gson gson;
  private final SqsPublisher sqsPublisher;
  private final SkierRepository skierRepository;

  public SkierServlet() {
    gson = new Gson();
    sqsPublisher = new SqsPublisher();
    skierRepository = new SkierRepository();
  }

  private SkierRequestUrl parseStringUrl(String urlPath) {
    SkierRequestUrl skierRequestUrl = new SkierRequestUrl();
    String[] urlParts = urlPath.split("/");
    if (urlParts.length == 3 && StringUtils.isNumeric(urlParts[1]) && urlParts[2].equals("vertical")) {
      skierRequestUrl.setRequestType(SkierRequestUrl.RequestType.VERTICAL_BY_RESORT);
      skierRequestUrl.setSkierId(Integer.parseInt(urlParts[1]));
      return skierRequestUrl;
    } else if (urlParts.length == 8 &&
            StringUtils.isNumeric(urlParts[1]) &&
            urlParts[2].equals("seasons") &&
            StringUtils.isNotEmpty(urlParts[3]) &&
            urlParts[4].equals("days") &&
            StringUtils.isNumeric(urlParts[5]) &&
            urlParts[6].equals("skiers") &&
            StringUtils.isNumeric(urlParts[7])
    ) {
      skierRequestUrl.setRequestType(SkierRequestUrl.RequestType.VERTICAL_BY_DAY);
      skierRequestUrl.setResortId(Integer.parseInt(urlParts[1]));
      skierRequestUrl.setSeasonId(urlParts[3]);
      skierRequestUrl.setDayId(urlParts[5]);
      skierRequestUrl.setSkierId(Integer.parseInt(urlParts[7]));
      return skierRequestUrl;
    }
    throw new RuntimeException("Invalid URL: " + urlPath);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
    Map<String, String> paramMap = req.getParameterMap().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]));
    System.out.println("urlPath = " + urlPath);
    System.out.println("paramMap = " + paramMap);

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return;
    }

    SkierRequestUrl skierRequestUrl;
    try {
      skierRequestUrl = parseStringUrl(urlPath);
    } catch (Exception e) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    if (skierRequestUrl.getRequestType() == SkierRequestUrl.RequestType.VERTICAL_BY_DAY) {
      int vertical = skierRepository.getDayVerticalBySkierId(
              skierRequestUrl.getResortId(),
              skierRequestUrl.getSeasonId(),
              skierRequestUrl.getSkierId(),
              skierRequestUrl.getDayId()
      );
      res.setStatus(HttpServletResponse.SC_OK);
      res.getWriter().write(String.valueOf(vertical));
    } else if (skierRequestUrl.getRequestType() == SkierRequestUrl.RequestType.VERTICAL_BY_RESORT) {
      if (!paramMap.containsKey("resort")) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("ERROR: Non-null parameter 'resort' is empty");
        return;
      }
      int resortId = Integer.parseInt(paramMap.get("resort"));
      Optional<String> seasonId = Optional.empty();
      if (paramMap.containsKey("season")) {
        seasonId = Optional.of(paramMap.get("season"));
      }
      int vertical = skierRepository.getVerticalTotal(
              resortId,
              skierRequestUrl.getSkierId(),
              seasonId
      );
      res.setStatus(HttpServletResponse.SC_OK);
      res.getWriter().write(String.valueOf(vertical));
    }
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    try {
      SkierRequestUrl skierRequestUrl = parseStringUrl(urlPath);
      String reqPayloadStr = CharStreams.toString(req.getReader());
      int skierId = skierRequestUrl.getSkierId();
      int resortId = skierRequestUrl.getResortId();
      String seasonId = skierRequestUrl.getSeasonId();
      String dayId = skierRequestUrl.getDayId();
      SkierRequest skierRequest;
      try {
        skierRequest = gson.fromJson(reqPayloadStr, SkierRequest.class);
      } catch (JsonSyntaxException e) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      LiftRide liftRide = new LiftRide();
      liftRide.setSkierId(skierId);
      liftRide.setLiftId(skierRequest.getLiftId());
      liftRide.setTime(skierRequest.getTime());
      liftRide.setWaitTime(skierRequest.getWaitTime());
      liftRide.setResortId(resortId);
      liftRide.setSeasonId(seasonId);
      liftRide.setDayId(dayId);
      sqsPublisher.publish(gson.toJson(liftRide));

      res.setStatus(HttpServletResponse.SC_CREATED);
    } catch (Exception e) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }
}
