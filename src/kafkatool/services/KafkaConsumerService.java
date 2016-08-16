package kafkatool.services;

import javafx.scene.control.TextArea;
import kafkatool.Main;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to watch given kafka topics
 * Created by Siarhei Runou on 15.8.16.
 */
public class KafkaConsumerService {

    private Map<String, TextArea> messagesMap = new ConcurrentHashMap<>();
    KafkaConsumer<String, String> consumer;
    private boolean isRunning = true;
    private static KafkaConsumerService instance;

    public static synchronized KafkaConsumerService getInstance() {
        if (instance == null) {
            instance = new KafkaConsumerService();
        }
        return instance;
    }

    private KafkaConsumerService() {
        consumer = new KafkaConsumer<>(Main.applicationProperties);
        Thread checker = new Thread(() -> {
            while (isRunning) {
                if (!messagesMap.keySet().isEmpty()) {
                    ConsumerRecords<String, String> records = consumer.poll(100);
                    for (ConsumerRecord<String, String> record : records) {
                        TextArea areaToUpdate = messagesMap.get(record.topic());
                        if (null != areaToUpdate) {
                            areaToUpdate.appendText(LocalDateTime.now() + ":\n" + record.value() + "\n");
                        }
                    }
                }
                try {
                    Thread.sleep(Long.parseLong(Main.applicationProperties.getProperty("refreshInterval")));
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
        consumer.subscribe(new ArrayList<>(topics));
    }

    public void unregisterTopic(String topicName) {
        messagesMap.remove(topicName);
        Set<String> topics = new HashSet<>(consumer.subscription());
        topics.remove(topicName);
        consumer.unsubscribe();
        consumer.subscribe(new ArrayList<>(topics));
    }
}
