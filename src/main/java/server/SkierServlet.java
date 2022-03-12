package server;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;
import server.model.LiftRide;
import server.model.SkierRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SkierServlet extends HttpServlet {
  private final Gson gson = new Gson();
  private final SqsPublisher sqsPublisher = new SqsPublisher();

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameterers");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`
      res.getWriter().write("It works!!");
    }
  }

//  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
//    res.setStatus(HttpServletResponse.SC_CREATED);
//  }

//  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
////    res.setContentType("text/plain");
//    String urlPath = req.getPathInfo();
//
//    // check we have a URL!
//    if (urlPath == null || urlPath.isEmpty()) {
//      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
////      res.getWriter().write("missing parameterers");
//      return;
//    }
//
//    String[] urlParts = urlPath.split("/");
//    // and now validate url path and return the response status code
//    // (and maybe also some value if input is valid)
//
//    if (!isUrlValid(urlParts)) {
//      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
//    } else {
//      res.setStatus(HttpServletResponse.SC_CREATED);
//      // do any sophisticated processing with urlParts which contains all the url params
//      // TODO: process url params in `urlParts`
////      res.getWriter().write("It works!");
//    }
//  }
//
//  private boolean isUrlValid(String[] urlParts) {
//    // urlPath  = "/1/seasons/2019/day/1/skier/123"
//    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
//    // System.out.println(Arrays.asList(urlParts));
//    if (urlParts.length == 3 && StringUtils.isNumeric(urlParts[1]) && urlParts[2].equals("vertical")) {
//      return true;
//    }
//    if (urlParts.length == 8 &&
//            StringUtils.isNumeric(urlParts[1]) &&
//            urlParts[2].equals("seasons") &&
//            StringUtils.isNumeric(urlParts[3]) &&
//            urlParts[4].equals("days") &&
//            StringUtils.isNumeric(urlParts[5]) &&
//            urlParts[6].equals("skiers") &&
//            StringUtils.isNumeric(urlParts[7])
//    ) {
//      return true;
//    }
//    return false;
//  }


//  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
//    try {
//      Thread.sleep(1000);
//    } catch (Exception e) {
//      throw new RuntimeException(e);
//    }
//    res.setContentType("text/plain");
//    String urlPath = req.getPathInfo();
//
//    // check we have a URL!
//    if (urlPath == null || urlPath.isEmpty()) {
//      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
//      res.getWriter().write("missing parameterers");
//      return;
//    }
//
//    String[] urlParts = urlPath.split("/");
//    // and now validate url path and return the response status code
//    // (and maybe also some value if input is valid)
//
//    if (!isUrlValid(urlParts)) {
//      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
//    } else {
//      res.setStatus(HttpServletResponse.SC_OK);
//      // do any sophisticated processing with urlParts which contains all the url params
//      // TODO: process url params in `urlParts`
//      res.getWriter().write("It works!");
//    }
//  }
//
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
//      res.getWriter().write("missing parameterers");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      String reqPayloadStr = CharStreams.toString(req.getReader());
      int skierId = Integer.parseInt(urlParts[7]);
      SkierRequest skierRequest;
      try {
        skierRequest = gson.fromJson(reqPayloadStr, SkierRequest.class);
      } catch (JsonSyntaxException e) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//        res.getWriter().write("Invalid request payload");
        return;
      }
      LiftRide liftRide = new LiftRide();
      liftRide.setSkierId(skierId);
      liftRide.setLiftID(skierRequest.getLiftID());
      liftRide.setTime(skierRequest.getTime());
      liftRide.setWaitTime(skierRequest.getWaitTime());
      sqsPublisher.publish(gson.toJson(liftRide));

      res.setStatus(HttpServletResponse.SC_CREATED);
//      res.getWriter().write("It works!");
    }
  }

  private boolean isUrlValid(String[] urlParts) {
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
//    System.out.println(Arrays.asList(urlParts));
    if (urlParts.length == 3 && StringUtils.isNumeric(urlParts[1]) && urlParts[2].equals("vertical")) {
      return true;
    }
    if (urlParts.length == 8 &&
            StringUtils.isNumeric(urlParts[1]) &&
            urlParts[2].equals("seasons") &&
            StringUtils.isNumeric(urlParts[3]) &&
            urlParts[4].equals("days") &&
            StringUtils.isNumeric(urlParts[5]) &&
            urlParts[6].equals("skiers") &&
            StringUtils.isNumeric(urlParts[7])
    ) {
      return true;
    }
    return false;
  }
}
