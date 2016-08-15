package kafkatool.services;

import javafx.concurrent.Task;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service to send messages to Kafka queue
 * Created by Siarhei Runou on 15.8.16.
 */
public class KafkaProducerService {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final KafkaProducer<String, String> producer;

    public KafkaProducerService(Properties props) {
        producer = new KafkaProducer<>(props);
    }

    public void sendMessageToKafka(String message, String topic, Runnable callback) {
        executorService.submit(new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                producer.send(new ProducerRecord<>(topic, message));
                callback.run();
                return true;
            }
        });
    }

}
