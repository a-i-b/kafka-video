package org.aib.producer;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Sender {

  @Value("${kafka.topic.name}")
  private String topic;

  private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

  @Autowired
  private KafkaTemplate<String, ByteBuffer> kafkaTemplateBinary;


  public void send(String key, ByteBuffer payload) {
	LOGGER.info("sending payload length='{}' to topic='{}'", payload.remaining(), topic);
	kafkaTemplateBinary.send(topic, key, payload);
  }
}