package client;

import com.google.gson.Gson;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class SkierClient {
  private static final String postSkierUrl = "http://%s/distributed-systems-NU_war/skiers/12/seasons/2019/days/1/skiers/%s";
  private static int successfulRequests = 0;
  private static int failedRequests = 0;
  private static long wallTime = 0;
  private static List<RequestPerformance> requestPerformances = new ArrayList<>();
  private static final Object requestPerformanceLock = new Object();
  private static String ipPort;

  public static void main(String[] args) throws Exception {
    int numThreads = Integer.parseInt(args[0]);
    int numSkiers = Integer.parseInt(args[1]);
    int numLifts = Integer.parseInt(args[2]);
    int numRuns = Integer.parseInt(args[3]);
    ipPort = args[4];
    long startRuntime = System.currentTimeMillis();
    executePhase1(numThreads, numSkiers, numLifts, numRuns);
    executePhase2(numThreads, numSkiers, numLifts, numRuns);
    executePhase3(numThreads, numSkiers, numLifts, numRuns);
    long endRuntime = System.currentTimeMillis();
    wallTime = endRuntime - startRuntime;
    long totalRuntime = endRuntime - startRuntime;
    int totalRequests = successfulRequests + failedRequests;
    double throughput = totalRequests / (totalRuntime / 1000.0);
    System.out.println("Successful Requests: " + successfulRequests);
    System.out.println("Failed Requests: " + failedRequests);
    System.out.println("Total run time: " + totalRuntime + " ms");
    System.out.println("Total throughput: " + throughput + " requests/s");
    writeReqPerformancesToCsv("performance.csv");
    printPerformances();

//    long startRuntime = System.currentTimeMillis();
//    System.out.println("Running single threaded client");
//    for (int i = 0; i < 1000; i++) {
//      sendPost(1, 1, 1, 1);
//      System.out.println("i=" + i);
//    }
//    long endRuntime = System.currentTimeMillis();
//    System.out.println("Total time: " + (endRuntime - startRuntime) + " ms");

//    test();
  }

//  private static void test() {
//    int numThreads = 32;
//    int numRequests = 1000;
//    for (int i = 0; i < numThreads; i++) {
//      Runnable thread = () -> {
//        for (int j = 0; j < numRequests; j++) {
//          System.out.println("j=" + j);
////          sendPost(1, 2, 3, 4);
//          sendPostTest(j);
//        }
//      };
//      new Thread(thread).start();
//    }
//  }

  private static void executePhase1(
          final int numThreads,
          final int numSkiers,
          final int numLifts,
          final int numRuns
  ) throws InterruptedException {
    System.out.println("Executing phase 1");
    int numThreadsPhase1 = numThreads / 4;
    int skierIdRange = numSkiers / numThreadsPhase1;
    int skierIdStart = 1;
    final int timeStart = 1;
    final int timeEnd = 90;
    final int numRequests = (int) Math.round(0.2 * numRuns * numSkiers / numThreadsPhase1);

    CountDownLatch phase1Latch = new CountDownLatch((int) Math.ceil(numThreadsPhase1 * 0.2));
    System.out.println("Phase 1: numThreads = " + numThreadsPhase1);
    System.out.println("Phase 1: numRequests = " + numRequests);
    for (int i = 0; i < numThreadsPhase1; i++) {
      final int currSkierIdStart = skierIdStart;
      final int currSkierIdEnd = currSkierIdStart + skierIdRange - 1;
      Runnable runnable = () -> {
        runTask(numRequests, currSkierIdStart, currSkierIdEnd, numLifts, timeStart, timeEnd);
        phase1Latch.countDown();
      };
      Thread thread = new Thread(runnable);
      thread.start();
      skierIdStart += skierIdRange;
    }
    phase1Latch.await();
  }

  private static void executePhase2(
          final int numThreads,
          final int numSkiers,
          final int numLifts,
          final int numRuns
  ) throws InterruptedException {
    System.out.println("Executing phase 2");
    final int timeStart = 91;
    final int timeEnd = 360;
    final int skierIdRange = numSkiers / numThreads;
    int skierIdStart = 1;
    final int numRequests = (int) Math.round(0.6 * numRuns * numSkiers / numThreads);
    CountDownLatch phase2Latch = new CountDownLatch((int) Math.ceil(numThreads * 0.2));
    System.out.println("Phase 2: numThreads = " + numThreads);
    System.out.println("Phase 2: numRequests = " + numRequests);
    for (int i = 0; i < numThreads; i++) {
      final int currSkierIdStart = skierIdStart;
      final int currSkierIdEnd = currSkierIdStart + skierIdRange - 1;
      Runnable thread = () -> {
        runTask(numRequests, currSkierIdStart, currSkierIdEnd, numLifts, timeStart, timeEnd);
        phase2Latch.countDown();
      };
      new Thread(thread).start();
      skierIdStart += skierIdRange;
    }
    phase2Latch.await();
  }

  private static void executePhase3(
          final int numThreads,
          final int numSkiers,
          final int numLifts,
          final int numRuns
  ) {
    System.out.println("Executing phase 3");
    final int timeStart = 361;
    final int timeEnd = 420;
    final int numThreadsPhase3 = numThreads / 10;
    final int skierIdRange = numSkiers / numThreadsPhase3;
    int skierIdStart = 1;
    final int numRequests = (int) Math.round(0.1 * numRuns);
    System.out.println("Phase 3: numThreads = " + numThreadsPhase3);
    System.out.println("Phase 3: numRequests = " + numRequests);
    for (int i = 0; i < numThreadsPhase3; i++) {
      final int currSkierIdStart = skierIdStart;
      final int currSkierIdEnd = currSkierIdStart + skierIdRange - 1;
      Runnable thread = () -> {
        runTask(numRequests, currSkierIdStart, currSkierIdEnd, numLifts, timeStart, timeEnd);
      };
      new Thread(thread).start();
      skierIdStart += skierIdRange;
    }
  }

  private static void runTask(
          int numRequests,
          int skierIdStart,
          int skierIdEnd,
          int numLifts,
          int timeStart,
          int timeEnd
  ) {
    Random rand = new Random();
    HttpClient client = HttpClients.createDefault();
    for (int i = 0; i < numRequests; i++) {
      int skierId = rand.nextInt(skierIdEnd - skierIdStart) + skierIdStart;
      int liftId = rand.nextInt(numLifts - 1) + 1;
      int time = rand.nextInt(timeEnd - timeStart) + timeStart;
      int waitTime = rand.nextInt(11);
      int statusCode = sendPostDecorator(client, skierId, liftId, time, waitTime);
      if (statusCode < 300) {
        incrementSuccess();
      } else if (statusCode >= 400) {
        incrementFailures();
      } else {
        System.out.println("Received unhandled status code: " + statusCode);
      }
    }
  }

  private static synchronized void incrementSuccess() {
    successfulRequests++;
  }

  private static synchronized void incrementFailures() {
    failedRequests++;
  }

  private static int sendPostDecorator(
          HttpClient client,
          int skierId,
          int liftId,
          int time,
          int waitTime
  ) {
    long startTime = System.currentTimeMillis();
    int statusCode = sendPost(client, skierId, liftId, time, waitTime);
    long endTime = System.currentTimeMillis();
    long latency = endTime - startTime;
    RequestPerformance requestPerformance = new RequestPerformance();
    requestPerformance.startTime = startTime;
    requestPerformance.latency = latency;
    requestPerformance.requestType = "POST";
    requestPerformance.statusCode = statusCode;
    synchronized (requestPerformanceLock) {
      requestPerformances.add(requestPerformance);
    }
    return statusCode;
  }

  private static int sendPost(
          HttpClient client,
          int skierId,
          int liftId,
          int time,
          int waitTime
  ) {
    try {
      // TODO handle too many connections:
      // https://stackoverflow.com/questions/1978053/bindexception-address-already-in-use-on-a-client-socket
      // Create an instance of HttpClient.

      // Create a method instance.
      String url = String.format(postSkierUrl, ipPort, skierId);

      // Provide custom retry handler is necessary
      RequestBodySkier requestBodySkier = new RequestBodySkier();
      requestBodySkier.setLiftId(liftId);
      requestBodySkier.setTime(time);
      requestBodySkier.setWaitTime(waitTime);
      String body = new Gson().toJson(requestBodySkier);
      HttpPost post = new HttpPost(url);
      post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
      int statusCode = 0;
      try {
        // Execute the method.
        statusCode = client.execute(post).getStatusLine().getStatusCode();

        // TODO debugging only, remove
        if (statusCode >= 300) {
          System.out.println("ERROR: getting status code " + statusCode);
        }
      } catch (Exception e) {
        System.out.println("Error while sending post request");
        System.out.println(e);
      } finally {
        post.releaseConnection();
      }

      // Uncomment below for throttling
//       Thread.sleep(100);

      return statusCode;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void writeReqPerformancesToCsv(String filename) {
    PrintWriter pw;
    try {
      pw = new PrintWriter(new File(filename));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return;
    }
    StringBuilder builder = new StringBuilder();
    String columnNamesList = "RequestType,StartTime,Latency,StatusCode";
    builder.append(columnNamesList + "\n");
    synchronized (requestPerformanceLock) {
      for (RequestPerformance rp : requestPerformances) {
        builder.append(rp.startTime);
        builder.append(",");
        builder.append(rp.requestType);
        builder.append(",");
        builder.append(rp.latency);
        builder.append(",");
        builder.append(rp.statusCode);
        builder.append("\n");
      }
    }
    pw.write(builder.toString());
    pw.close();
  }

  private static void printPerformances() {
    synchronized (requestPerformanceLock) {
      List<Long> sortedLatencies = requestPerformances.stream().map((rp) -> (rp.latency))
              .sorted()
              .collect(Collectors.toList());
      double mean = sortedLatencies.stream().mapToLong(l -> l)
              .average()
              .orElse(0);
      int count = sortedLatencies.size();
      double median;
      int mid = count / 2;
      if (count % 2 == 0) {
        median = sortedLatencies.get(mid-1) + sortedLatencies.get(mid) / 2.0;
      } else {
        median = sortedLatencies.get(mid);
      }
      long min = sortedLatencies.get(0);
      long max = sortedLatencies.get(sortedLatencies.size() - 1);
      double throughput = 1000.0 * count / wallTime; // convert to seconds
      long p99Latency = sortedLatencies.get((int) Math.ceil(0.99 * count) - 1);
      System.out.println("Mean response time = " + mean + " ms");
      System.out.println("Median response time = " + median + " ms");
      System.out.println("Throughput = " + throughput + " requests/sec");
      System.out.println("Min response time = " + min + " ms");
      System.out.println("Max response time = " + max + " ms");
      System.out.println("p99 response time = " + p99Latency + " ms");
    }
  }



  public static class RequestBodySkier {
    private int liftId;
    private int time;
    private int waitTime;

    public int getLiftId() {
      return liftId;
    }

    public void setLiftId(int liftId) {
      this.liftId = liftId;
    }

    public int getTime() {
      return time;
    }

    public void setTime(int time) {
      this.time = time;
    }

    public int getWaitTime() {
      return waitTime;
    }

    public void setWaitTime(int waitTime) {
      this.waitTime = waitTime;
    }
  }


  private static class RequestPerformance {
    long startTime;
    String requestType;
    long latency;
    int statusCode;
  }

//  private static void callServlet() {
//    // Create an instance of HttpClient.
//    HttpClient client = new HttpClient();
//
//    // Create a method instance.
//    GetMethod method = new GetMethod(url);
//
//    // Provide custom retry handler is necessary
//    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
//            new DefaultHttpMethodRetryHandler(3, false));
//
//    try {
//      // Execute the method.
//      int statusCode = client.executeMethod(method);
//
//      if (statusCode != HttpStatus.SC_OK) {
//        System.err.println("Method failed: " + method.getStatusLine());
//      }
//
//      // Read the response body.
//      byte[] responseBody = method.getResponseBody();
//
//      // Deal with the response.
//      // Use caution: ensure correct character encoding and is not binary data
//      System.out.println(new String(responseBody));
//
//    } catch (HttpException e) {
//      System.err.println("Fatal protocol violation: " + e.getMessage());
//      e.printStackTrace();
//    } catch (IOException e) {
//      System.err.println("Fatal transport error: " + e.getMessage());
//      e.printStackTrace();
//    } finally {
//      // Release the connection.
//      method.releaseConnection();
//    }
//  }

//  private static void sendPostTest(
//      int skierId
//  ) {
//    try {
////      HttpClient client = new HttpClient();
//      org.apache.http.client.HttpClient httpClient = HttpClients.createDefault();
//
//      // Create a method instance.
//      String url = String.format(postSkierUrl, ipPort, skierId);
////      PostMethod method = new PostMethod(url);
//
//      // Provide custom retry handler is necessary
////      StringRequestEntity requestEntity = new StringRequestEntity(
////              body,
////              "application/json",
////              "UTF-8");
////      method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
////              new DefaultHttpMethodRetryHandler(3, false));
////      method.setRequestEntity(requestEntity);
//
////      int statusCode = 0;
//
//      HttpPost post = new HttpPost(url);
//      try {
//        // Execute the method.
////        client.executeMethod(method);
//        httpClient.execute(post);
//      } catch (Exception e) {
//        System.out.println(e);
//      } finally {
//        // Release the connection.
////        method.releaseConnection();
//      }
//    } catch (Exception e) {
//      throw new RuntimeException(e);
//    }
//  }
}
