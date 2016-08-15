package kafkatool.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import kafkatool.Main;
import kafkatool.services.KafkaConsumerService;
import kafkatool.services.KafkaProducerService;
import kafkatool.util.JSONMinify;

import java.io.IOException;

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
        Tab tab = FXMLLoader.load(Main.class.getResource("../layout/consumerTab.fxml"));
        TextArea consTextarea = (TextArea) ((AnchorPane) tab.getContent()).getChildren().get(0);
        tab.setText("Topic1 consumer");
        tabPane.getTabs().add(tab);
        consumerService.addTopicToWatch("Topic1", consTextarea);
    }

    @FXML
    private void initialize() {
        topicSelector.getItems().addAll(Main.applicationProperties.getProperty("topics").trim().split(","));
        this.producerService = new KafkaProducerService(Main.applicationProperties);
        this.consumerService = new KafkaConsumerService(Main.applicationProperties);
    }
}
