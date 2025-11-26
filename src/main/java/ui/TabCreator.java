package ui;

import app.AppContext;
import core.utils.ConfigXml;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TabCreator {

    protected Tab createGeneratorTab(ConfigXml configXml) {
        Tab tab = new Tab("Generator");

        double currentFlowRate = AppContext.SIMULATION.getFlowRate();
        boolean queueUse = false;

        if (configXml != null) {
            currentFlowRate = configXml.getFlowRate();
            queueUse = configXml.isQueueUsed();
        }

        Label flowLabel = new Label("Flow rate:");
        Label flowValueLabel = new Label(String.format("%.2f", currentFlowRate));

        Slider flowSlider = new Slider(0.0, 1.0, currentFlowRate);
        flowSlider.setShowTickMarks(true);
        flowSlider.setShowTickLabels(true);
        flowSlider.setMajorTickUnit(0.25);
        flowSlider.setBlockIncrement(0.01);

        // when value of slider changes, update label
        flowSlider.valueProperty().addListener((obs, oldV, newV) ->
                flowValueLabel.setText(String.format("%.2f", newV.doubleValue()))
        );

        // všechno pro flowRate v jedné horizontální skupině
        HBox flowBox = new HBox(5, flowLabel, flowSlider, flowValueLabel);
        flowBox.setAlignment(Pos.CENTER_LEFT);

        CheckBox queueUseCheck = new CheckBox("Use queue");
        queueUseCheck.setSelected(queueUse);

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> {
            if (configXml != null) {
                configXml.setFlowRate(flowSlider.getValue());
                configXml.setQueueUsed(queueUseCheck.isSelected());
                try {
                    configXml.save();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // finální řada pro celý tab – pořád jen jeden „proužek“
        HBox row = new HBox(20, flowBox, queueUseCheck, saveBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 10, 10, 10));

        tab.setContent(row);
        return tab;
    }

    private Tab createRunDetailsTab() {
        Tab tab = new Tab("Run details");

        TextField timeStepField = new TextField("1");
        timeStepField.setPrefWidth(70);

        TextField durationField = new TextField("1000");
        durationField.setPrefWidth(100);

        CheckBox showGuiCheck = new CheckBox("Show GUI");

        Button saveBtn = new Button("Uložit");

        VBox timeBox = new VBox(new Label("Time step:"), timeStepField);
        VBox durBox = new VBox(new Label("Duration:"), durationField);

        timeBox.setSpacing(3);
        durBox.setSpacing(3);

        HBox row = new HBox(20,
                timeBox,
                durBox,
                showGuiCheck,
                saveBtn
        );

        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 10, 10, 10));

        tab.setContent(row);
        return tab;
    }


    private Tab createModelsTab() {
        Tab tab = new Tab("Models");

        ComboBox<String> cfModel = new ComboBox<>();
        cfModel.getItems().addAll("gipps", "ovm", "fvdm", "idm");
        cfModel.setValue("gipps");
        cfModel.setPrefWidth(120);

        ComboBox<String> lcModel = new ComboBox<>();
        lcModel.getItems().addAll("mobil", "mobil-simple");
        lcModel.setValue("mobil");
        lcModel.setPrefWidth(120);

        Button saveBtn = new Button("Uložit");

        VBox cfBox = new VBox(new Label("Car-following:"), cfModel);
        VBox lcBox = new VBox(new Label("Lane-changing:"), lcModel);

        cfBox.setSpacing(3);
        lcBox.setSpacing(3);

        HBox row = new HBox(20,
                cfBox,
                lcBox,
                saveBtn
        );

        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 10, 10, 10));

        tab.setContent(row);
        return tab;
    }

    private Tab createRoadsTab() {
        Tab tab = new Tab("Roads");

        int numberOfRoads = AppContext.SIMULATION.getRoads().length;
        Spinner<Integer> roadsSpinner = new Spinner<>(1, 1000000, numberOfRoads);
        roadsSpinner.setPrefWidth(80);

        TextField roadFileField = new TextField("config/roads/road1.xml");
        roadFileField.setPrefWidth(200);

        Button saveBtn = new Button("Uložit");

        VBox roadsBox = new VBox(new Label("Number of roads:"), roadsSpinner);
        VBox fileBox = new VBox(new Label("Road file:"), roadFileField);

        roadsBox.setSpacing(3);
        fileBox.setSpacing(3);

        HBox row = new HBox(20,
                roadsBox,
                fileBox,
                saveBtn
        );

        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 10, 10, 10));

        tab.setContent(row);
        return tab;
    }
}
