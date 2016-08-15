package kafkatool.model;

import java.time.LocalDateTime;

/**
 * DTO to represent message from Kafka
 * Created by Siarhei Runou on 15.8.16.
 */
public class KafkaMessage {
    private LocalDateTime timestamp;
    private String message;

    public KafkaMessage(String message, LocalDateTime timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return timestamp + ":" + message;
    }
}
