package kafkatool.services;

import javafx.concurrent.Task;
import kafkatool.Main;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service to send messages to Kafka queue
 * Created by Siarhei Runou on 15.8.16.
 */
public class KafkaProducerService {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private KafkaProducer<String, String> producer;
    private static KafkaProducerService instance;

    public static synchronized KafkaProducerService getInstance() {
        if (null == instance) {
            instance = new KafkaProducerService();
        }
        return instance;
    }

    private KafkaProducerService() {
        producer = new KafkaProducer<>(Main.applicationProperties);
    }

    public void sendMessageToKafka(String message, String topic, Runnable callback) {
        executorService.submit(new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                producer.send(new ProducerRecord<>(topic, message));
                producer.flush();
                callback.run();
                return true;
            }
        });
    }

    public void stop() {
        executorService.shutdownNow();
        producer.close();
    }

    public void reinitialize() {
        producer.close();
        producer = new KafkaProducer<>(Main.applicationProperties);
    }

}
