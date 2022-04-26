package server;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import server.model.ResortRequestUrl;
import server.model.ResortResponse;
import server.model.SkierRequestUrl;
import server.repository.ResortRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ResortServlet extends HttpServlet {
  private final Gson gson;
  private final SqsPublisher sqsPublisher;
  private final ResortRepository resortRepository;

  public ResortServlet() {
    gson = new Gson();
    sqsPublisher = new SqsPublisher();
    resortRepository = new ResortRepository();
  }

  private ResortRequestUrl parseStringUrl(String urlPath) {
    ResortRequestUrl resortRequestUrl = new ResortRequestUrl();
    String[] urlParts = urlPath.split("/");
    System.out.println(Arrays.asList(urlParts));
    if (urlParts.length == 7 &&
            StringUtils.isNumeric(urlParts[1]) &&
            urlParts[2].equals("seasons") &&
            StringUtils.isNotEmpty(urlParts[3]) &&
            urlParts[4].equals("day") &&
            StringUtils.isNumeric(urlParts[5]) &&
            urlParts[6].equals("skiers")
    ) {
      resortRequestUrl.setResortId(Integer.parseInt(urlParts[1]));
      resortRequestUrl.setSeasonId(urlParts[3]);
      resortRequestUrl.setDayId(urlParts[5]);
      return resortRequestUrl;
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

    ResortRequestUrl resortRequestUrl;
    try {
       resortRequestUrl = parseStringUrl(urlPath);
    } catch (Exception e) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    long uniqueSkiersNum = resortRepository.getNumUniqueSkiers(
            resortRequestUrl.getResortId(),
            resortRequestUrl.getSeasonId(),
            resortRequestUrl.getDayId()
    );
    ResortResponse resortResponse = new ResortResponse();
    resortResponse.setNumSkiers(uniqueSkiersNum);
    res.getWriter().write(gson.toJson(resortResponse));
  }
}
