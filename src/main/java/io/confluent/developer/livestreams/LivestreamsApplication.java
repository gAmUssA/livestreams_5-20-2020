package io.confluent.developer.livestreams;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.stream.LongStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@SpringBootApplication
public class LivestreamsApplication {
  
  @Value("${io.confluent.developer.topic.name}")
  private String topicName;

  @Value("${io.confluent.developer.topic.partitions}")
  private int numPartitions;

  @Value("${io.confluent.developer.topic.replication}")
  private short replicationFactor;

  @Bean
  NewTopic myTestTopic() {
    
    return new NewTopic(topicName, numPartitions, replicationFactor);
  }

  public static void main(String[] args) {
    SpringApplication.run(LivestreamsApplication.class, args);
  }

}


@Component
@RequiredArgsConstructor
@Log4j2
class Producer {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final NewTopic topic;

  @EventListener(ApplicationStartedEvent.class)
  public void produce() {

    LongStream.range(0, 10).forEach(i -> {
      String key = "kafka";
      String value = key.toUpperCase() + i;
      kafkaTemplate.send(topic.name(), key, value).addCallback(result -> {
        if (result != null) {
          final RecordMetadata recordMetadata = result.getRecordMetadata();
          log.info("produced to {}, {}, {}", recordMetadata.topic(), recordMetadata.partition(),
                   recordMetadata.offset());
        }
      }, ex -> {

      });

    });
    kafkaTemplate.flush();
  }

}