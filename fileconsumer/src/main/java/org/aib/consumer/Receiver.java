package org.aib.consumer;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

public class Receiver {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(Receiver.class);

  private Consumer<ByteBuffer> callback;

  public void SetCallback(Consumer<ByteBuffer> callback) {
	  this.callback = callback;
  }
  
  @KafkaListener(topics = "${kafka.topic.name}")
  public void receive(ByteBuffer payload) {
    LOGGER.info("received payload lenght='{}' with key '{}'", payload.remaining(), "x");
    callback.accept(payload);
  }
}