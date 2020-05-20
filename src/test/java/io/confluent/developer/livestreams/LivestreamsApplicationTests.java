package io.confluent.developer.livestreams;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


class LivestreamsApplicationTests extends AbstractIntegrationTest {

  @Value("${io.confluent.developer.topic.name}")
  private String topicName;

  @Autowired
  KafkaProperties properties;


  @Test
  void contextLoads() {

    // kafka consumer 
    final Consumer<String, String> consumer = createConsumer(topicName);
    // consumer read all messages

    final ArrayList<String> actualValues = new ArrayList<>();
    while (true) {
      final ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, 10000);
      if (records.isEmpty()) {
        break;
      }
      records.forEach(stringStringConsumerRecord -> actualValues.add(stringStringConsumerRecord.value()));
    }

    // fixture
    final ArrayList<String> ours = new ArrayList<>();
    LongStream.range(0, 10).forEach(i -> ours.add("kafka".toUpperCase() + i)
    );

    assertEquals(ours, actualValues);
  }

  private Consumer<String, String> createConsumer(final String topicName) {

    final Consumer<String, String>
        consumer =
        new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties(), StringDeserializer::new,
                                          StringDeserializer::new).createConsumer();

    consumer.subscribe(Collections.singletonList(topicName));
    return consumer;
  }

}
