package server;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SqsPublisher {
  private static final String url = "https://sqs.us-west-2.amazonaws.com/468427783636/SkierQueue";
  private final SqsClient client;

  public SqsPublisher() {
    this.client = SqsClient.builder()
            .region(Region.US_WEST_2)
            .build();
  }

  public void publish(String payload) {
    SendMessageRequest request = SendMessageRequest.builder()
            .queueUrl(url)
            .messageBody(payload)
            .delaySeconds(5)
            .build();
    client.sendMessage(request);
  }
}
