package kafkatool;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kafkatool.controllers.Controller;
import kafkatool.services.KafkaConsumerService;
import kafkatool.services.KafkaProducerService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

public class Main extends Application {

    public static final Properties applicationProperties = new Properties();

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/kafkatool/layout/mainWindow.fxml"));
        Parent root = loader.load();
        Controller ctrl = loader.getController();
        ctrl.setApp(this);
        primaryStage.setTitle("Kafka tool");
        primaryStage.setScene(new Scene(root, 1000, 520));
        primaryStage.show();
    }

    @Override
    public void stop() {
        KafkaConsumerService.getInstance().stop();
        KafkaProducerService.getInstance().stop();
    }

    public void reinitializeApplication() {
        readApplicationProperties();
        KafkaConsumerService.getInstance().reinitialize();
        KafkaProducerService.getInstance().reinitialize();
    }

    public void saveNewProperties(Properties properties) {
        URL url = Main.class.getResource("/kafkatool/config/kafkaTool.properties");
        try (OutputStream output = new FileOutputStream(new File(url.toURI().getPath()))) {
            properties.store(output, null);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        readApplicationProperties();
        launch(args);
    }

    private static void readApplicationProperties() {
        try(InputStream input = Main.class.getResourceAsStream("/kafkatool/config/kafkaTool.properties")) {
            applicationProperties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read property file with settings! Can not proceed!");
        }
    }
}
