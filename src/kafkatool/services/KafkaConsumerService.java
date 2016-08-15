package kafkatool.services;

import javafx.scene.control.TextArea;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.sql.Timestamp;
import java.util.*;

/**
 * Service to watch given kafka topics
 * Created by Siarhei Runou on 15.8.16.
 */
public class KafkaConsumerService {

    private Map<String, TextArea> messagesMap = new HashMap<>();
    KafkaConsumer<String, String> consumer;
    private boolean isRunning = true;

    public KafkaConsumerService(Properties props) {
        consumer = new KafkaConsumer<>(props);
        Thread checker = new Thread(() -> {
            while (isRunning) {
                if (!messagesMap.keySet().isEmpty()) {
                    ConsumerRecords<String, String> records = consumer.poll(100);
                    for (ConsumerRecord<String, String> record : records) {
                        TextArea areaToUpdate = messagesMap.get(record.topic());
                        areaToUpdate.appendText( (new Timestamp(record.timestamp())).toLocalDateTime() + ": " + record.value() + "\n");
                    }
                }
                try {
                    Thread.sleep(Long.parseLong(props.getProperty("refreshInterval")));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        checker.start();
    }

    public void addTopicToWatch(String topicName, TextArea area) {
        messagesMap.putIfAbsent(topicName, area);
        Set<String> topics = new HashSet<>(consumer.subscription());
        topics.add(topicName);
        consumer.unsubscribe();
        consumer.subscribe(topics);
    }
}
