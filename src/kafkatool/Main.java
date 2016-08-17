package kafkatool;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kafkatool.controllers.Controller;
import kafkatool.services.KafkaConsumerService;
import kafkatool.services.KafkaProducerService;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Main extends Application {

    public static final Properties applicationProperties = new Properties();
    private static final String EXTERNAL_PROPERTY_FILE_PATH = "./kafkaTool.properties";
    private static final String INTERNAL_PROPERTY_FILE_PATH = "config/kafkaTool.properties";

    @Override
    public void start(Stage primaryStage) throws Exception {

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
        Path path = Paths.get(EXTERNAL_PROPERTY_FILE_PATH);
        OutputStream output;
        try {
            if (Files.notExists(path))
                Files.createFile(path);
            output = new FileOutputStream(EXTERNAL_PROPERTY_FILE_PATH);
            properties.store(output, null);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        readApplicationProperties();
        launch(args);
    }

    private static void readApplicationProperties() {
        try (InputStream input = checkIfFileExists() ?
                new FileInputStream(EXTERNAL_PROPERTY_FILE_PATH) :
                Main.class.getResourceAsStream("/kafkatool/config/kafkaTool.properties")) {
            applicationProperties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read property file with settings! Can not proceed!");
        }
    }

    private static boolean checkIfFileExists() {
        Path path = Paths.get(EXTERNAL_PROPERTY_FILE_PATH);
        return Files.exists(path);
    }
}
