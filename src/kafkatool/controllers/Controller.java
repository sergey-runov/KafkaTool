package kafkatool.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import kafkatool.Main;
import kafkatool.services.KafkaConsumerService;
import kafkatool.services.KafkaProducerService;
import kafkatool.util.JSONMinify;
import org.controlsfx.control.PropertySheet;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Main application controller
 * Created by Siarhei Runou on 15.8.16.
 */
public class Controller {

    @FXML
    private ComboBox<String> topicSelector;
    @FXML
    private ComboBox<String> historySelector;
    @FXML
    private TabPane tabPane;
    @FXML
    private TextArea textArea;
    @FXML
    private Button sendButton;
    @FXML
    private Button addConsumerButton;
    private HashMap<String, LinkedList<Pair<String, String>>> history;
    private KafkaProducerService producerService;
    private KafkaConsumerService consumerService;
    private Main app;

    @FXML
    public void selectTopic() {
        String selectedTopic = topicSelector.getSelectionModel().getSelectedItem();
        if (!history.containsKey(selectedTopic)) {
            history.put(selectedTopic, new LinkedList<>());
            textArea.setText("");
        } else {
            textArea.setText(
                    history.get(selectedTopic).isEmpty() ? "" : history.get(selectedTopic).getFirst().getValue());
            historySelector
                    .setItems(FXCollections
                            .observableArrayList(history.get(selectedTopic).stream()
                                    .map(Pair::getKey)
                                    .collect(Collectors.toList())));
        }
        System.out.println(topicSelector.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void selectHistoryEntry() {
        String selectedItem = historySelector.getSelectionModel().getSelectedItem();
        String selectedTopic = topicSelector.getSelectionModel().getSelectedItem();
        textArea.setText(history.get(selectedTopic).stream()
                .filter(val -> selectedItem.equals(val.getKey()))
                .findAny()
                .map(Pair::getValue)
                .orElse(""));
    }

    @FXML
    public void send() {
        final String topic = topicSelector.getSelectionModel().getSelectedItem();
        final String message = JSONMinify.minify(textArea.getText());
        history.get(topic).addFirst(
                new Pair<>(LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME), message));
        producerService.sendMessageToKafka(message, topic, () -> {
        });
    }

    @FXML
    public void addConsumer() throws IOException {
        final String topicName = getTopicName();
        if (null == topicName)
            return;
        Tab tab = FXMLLoader.load(Main.class.getResource("/kafkatool/layout/consumerTab.fxml"));
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
        if (response.isPresent() && saveButton.equals(response.get())) {
            topicSelector.setItems(FXCollections.observableArrayList());
            app.saveNewProperties(mapToModify);
            app.reinitializeApplication();
            topicSelector.getItems().addAll(Main.applicationProperties.getProperty("topics").trim().split(","));
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
        this.history = new HashMap<>();
    }

    public void setApp(Main app) {
        this.app = app;
    }
}
