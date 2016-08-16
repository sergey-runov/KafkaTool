package kafkatool.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import kafkatool.Main;
import kafkatool.services.KafkaConsumerService;
import kafkatool.services.KafkaProducerService;
import kafkatool.util.JSONMinify;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanProperty;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.*;

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
    private Main app;

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
        final String topicName = getTopicName();
        if (null == topicName) return;
        Tab tab = FXMLLoader.load(Main.class.getResource("../layout/consumerTab.fxml"));
        TextArea consTextarea = (TextArea) ((AnchorPane) tab.getContent()).getChildren().get(0);
        tab.setText(topicName);
        tab.setClosable(true);
        tab.setOnClosed(event -> consumerService.unregisterTopic(topicName));
        tabPane.getTabs().add(tab);
        consumerService.addTopicToWatch(topicName, consTextarea);
    }

    @FXML
    public void showConfigurationDialog() {
        ObservableList<PropertySheet.Item> items = FXCollections.observableArrayList();
        Properties mapToModify = new Properties();
        for (Map.Entry entry : Main.applicationProperties.entrySet()) {
            mapToModify.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }
        for (Map.Entry entry : mapToModify.entrySet()) {
                items.add(new PropertySheet.Item() {
                    @Override
                    public Class<?> getType() {
                        return String.class;
                    }

                    @Override
                    public String getCategory() {
                        return "Main";
                    }

                    @Override
                    public String getName() {
                        return entry.getKey().toString();
                    }

                    @Override
                    public String getDescription() {
                        return "Dummy";
                    }

                    @Override
                    public Object getValue() {
                        return entry.getValue();
                    }

                    @Override
                    public void setValue(Object o) {
                        entry.setValue(o);
                    }

                    @Override
                    public Optional<ObservableValue<? extends Object>> getObservableValue() {
                        return Optional.of(new SimpleStringProperty(entry.getValue().toString()));
                    }
                });
        }
        PropertySheet propSheet = new PropertySheet(items);
        propSheet.setPrefSize(400L, 600L);
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setResizable(false);
        alert.getDialogPane().setContent(propSheet);
        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().addAll(ButtonType.CANCEL, saveButton);
        Optional response = alert.showAndWait();
        if(response.isPresent() && saveButton.equals(response.get())) {
            app.saveNewProperties(mapToModify);
            app.reinitializeApplication();
        }
    }

    private String getTopicName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Please specify topic");
        dialog.setContentText("Topic name:");
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent() || "".equals(result.get().trim())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("You should specify topic name!");
            alert.show();
            return null;
        }
        return result.get();
    }

    @FXML
    private void initialize() {
        topicSelector.getItems().addAll(Main.applicationProperties.getProperty("topics").trim().split(","));
        this.producerService = KafkaProducerService.getInstance();
        this.consumerService = KafkaConsumerService.getInstance();
    }

    public void setApp(Main app) {
        this.app = app;
    }
}
