package client;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class SkierClient {
  private static String url = "http://localhost:8080/distributed_systems_NU_war_exploded/";

  public static void main(String[] args) throws Exception {
    long initial = System.currentTimeMillis();
    int numThreads = 100;
    CountDownLatch completed = new CountDownLatch(numThreads);
    for (int i = 0; i < numThreads; i++) {
      Runnable thread = () -> {
        callServlet();
        completed.countDown();
      };
      new Thread(thread).start();
    }
    completed.await();
    System.out.println("Total time: " + (System.currentTimeMillis() - initial));
  }

  private static void callServlet() {
    // Create an instance of HttpClient.
    HttpClient client = new HttpClient();

    // Create a method instance.
    GetMethod method = new GetMethod(url);

    // Provide custom retry handler is necessary
    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
            new DefaultHttpMethodRetryHandler(3, false));

    try {
      // Execute the method.
      int statusCode = client.executeMethod(method);

      if (statusCode != HttpStatus.SC_OK) {
        System.err.println("Method failed: " + method.getStatusLine());
      }

      // Read the response body.
      byte[] responseBody = method.getResponseBody();

      // Deal with the response.
      // Use caution: ensure correct character encoding and is not binary data
      System.out.println(new String(responseBody));

    } catch (HttpException e) {
      System.err.println("Fatal protocol violation: " + e.getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Fatal transport error: " + e.getMessage());
      e.printStackTrace();
    } finally {
      // Release the connection.
      method.releaseConnection();
    }
  }
}
