package kafkatool;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kafkatool.services.KafkaConsumerService;
import kafkatool.services.KafkaProducerService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main extends Application {

    public static final Properties applicationProperties = new Properties();

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../layout/mainWindow.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Kafka tool");
        primaryStage.setScene(new Scene(root, 1000, 520));
        primaryStage.show();
    }

    @Override
    public void stop() {
        KafkaConsumerService.getInstance().stop();
        KafkaProducerService.getInstance().stop();
    }


    public static void main(String[] args) {
        try(InputStream input = Main.class.getResourceAsStream("../config/kafkaTool.properties")) {
            applicationProperties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read property file with settings! Can not proceed!");
        }
        launch(args);
    }
}
