package kafkatool.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import kafkatool.Main;
import kafkatool.services.KafkaConsumerService;
import kafkatool.services.KafkaProducerService;
import kafkatool.util.JSONMinify;

import java.io.IOException;
import java.util.Optional;

/**
 * Main application controller
 * Created by Siarhei Runou on 15.8.16.
 */
public class Controller {

    @FXML
    private ComboBox<String> topicSelector;
    @FXML
    private TabPane tabPane;
    @FXML
    private TextArea textArea;
    @FXML
    private Button sendButton;
    @FXML
    private Button addConsumerButton;
    private KafkaProducerService producerService;
    private KafkaConsumerService consumerService;

    @FXML
    public void selectTopic() {
        System.out.println(topicSelector.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void send() {
        final String topic = topicSelector.getSelectionModel().getSelectedItem();
        final String message = JSONMinify.minify(textArea.getText());
        producerService.sendMessageToKafka(message, topic, () -> {});
    }

    @FXML
    public void addConsumer() throws IOException {
        Optional<String> result = getTopicName();
        if (!result.isPresent() || "".equals(result.get().trim())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("You should specify topic name!");
            alert.show();
            return;
        }
        final String topicName = result.get();
        Tab tab = FXMLLoader.load(Main.class.getResource("../layout/consumerTab.fxml"));
        TextArea consTextarea = (TextArea) ((AnchorPane) tab.getContent()).getChildren().get(0);
        tab.setText(topicName);
        tab.setClosable(true);
        tab.setOnClosed(event -> consumerService.unregisterTopic(topicName));
        tabPane.getTabs().add(tab);
        consumerService.addTopicToWatch(result.get(), consTextarea);
    }

    private Optional<String> getTopicName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Please specify topic");
        dialog.setContentText("Topic name:");
        return dialog.showAndWait();
    }

    @FXML
    private void initialize() {
        topicSelector.getItems().addAll(Main.applicationProperties.getProperty("topics").trim().split(","));
        this.producerService = new KafkaProducerService(Main.applicationProperties);
        this.consumerService = new KafkaConsumerService(Main.applicationProperties);
    }
}
