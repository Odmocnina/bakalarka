package ui;

import app.AppContext;
import core.engine.CoreEngine;
import core.model.Road;
import core.model.cellular.Cell;
import core.model.cellular.CellularRoad;
import core.utils.*;
import core.utils.constants.Constants;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import core.sim.Simulation;

import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;

/********************************************
 * Main window for traffic simulator GUI, javaFX is used
 *
 * @author Michael Hladky
 * @version 1.1
 ********************************************/
public class Window extends Application {

    /** simulation for stepping **/
    private Simulation simulation;

    /** core engine for running simulation **/
    private CoreEngine engine;

    /** horizontal scrollbar **/
    private ScrollBar hScroll;

    /** vertical scrollbar **/
    private ScrollBar vScroll;

    /** info label at the bottom **/
    private Label infoLabel;

    /** button for start/stop in toolbar **/
    private Button toolbarStartStopBtn;

    /** menu item for start/stop in menu **/
    private MenuItem menuStartStopItem;

    /** boolean property for lane change ban toggle button and menu item, used for keeping toggle button and menu item
     * in sync **/
    private BooleanProperty laneChangeProp;

    /** boolean property for collision ban toggle button and menu item, used for keeping toggle button and menu item in
     *  sync **/
    private BooleanProperty collisionBanProp;

    /** boolean properties for output settings toggle buttons and menu items, used for keeping toggle buttons and menu
     * items in sync **/
    private BooleanProperty[] whatToExportProps;

    /** boolean properties for logging settings toggle buttons and menu items, used for keeping toggle buttons and menu
     * items in sync **/
    private BooleanProperty[] logSettingsProps;

    /**
     * start method for JavaFX application
     *
     * @param primaryStage primary stage for JavaFX application
     **/
    @Override
    public void start(Stage primaryStage) {
        // get renderer and simulation from AppContext
        this.simulation = AppContext.SIMULATION;

        //set up stuff inf the window

        // canvas for drawing
        Canvas canvas = new Canvas();
        Pane canvasPane = new Pane(canvas);
        canvas.widthProperty().bind(canvasPane.widthProperty()); // this shit binds that graphics needs to only allocate
        canvas.heightProperty().bind(canvasPane.heightProperty()); // stuff we see

        // Scrollbars for navigation
        hScroll = new ScrollBar();
        hScroll.setOrientation(Orientation.HORIZONTAL);
        hScroll.prefWidthProperty().bind(canvasPane.widthProperty());
        vScroll = new ScrollBar();
        vScroll.setOrientation(Orientation.VERTICAL);

        // Info label at the top (moved from canvas drawing)
        infoLabel = new Label("Initializing...");
        infoLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        infoLabel.setPadding(new Insets(5, 10, 5, 10));

        final String WINDOW_TEXT = "Traffic simulator";

        // VBox to hold scrollbar and controls at the bottom
        VBox bottomLayout = new VBox(hScroll, infoLabel);

        // part that repaints all roads
        Runnable paintAll = () -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            Road[] roads = simulation.getRoads();

            // Update info string in the top label instead of drawing on canvas
            String infoString = "Forward model used: " + AppContext.CAR_FOLLOWING_MODEL.getName() +
                    " | lane changing model used: " + AppContext.LANE_CHANGING_MODEL.getName() + " | time steps: " +
                    simulation.getStepCount() + " | Opened map file: " + AppContext.RUN_DETAILS.mapFile;
            Platform.runLater(() -> infoLabel.setText(infoString));

            if (roads == null || roads.length == 0) {
                MyLogger.log("Error while getting roads to render", Constants.ERROR_FOR_LOGGING);
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                return;
            }

            final double GAP = 20.0;           // gap between roads when drawing
            Object content = roads[0].getContent();

            // Determine what to draw
            if (content instanceof Cell[][]) {
                this.handleCellular(roads, canvas, gc, GAP);
            } else if (content instanceof LinkedList[]) {
                this.handleContinuous(roads, canvas, gc, GAP);
            }
        };

        setSyncedProperties(paintAll); // set up the BooleanProperties for syncing toggle buttons and menu items

        MenuBar menuBar = createMenu(primaryStage, paintAll);
        ToolBar toolBar = createToolBar(primaryStage, paintAll);

        // top: info menu + toolbar
        VBox topPane = new VBox(menuBar, toolBar);
        topPane.setSpacing(5);
        topPane.setPadding(new Insets(5, 10, 5, 10));

        // main layout of gui
        BorderPane root = new BorderPane();
        root.setTop(topPane);
       // root.setTop(infoLabel);      // Added info label to top
        root.setCenter(canvasPane);  // Changed to Pane with Canvas
        root.setRight(vScroll);      // Added vertical scrollbar
        root.setBottom(bottomLayout);// Controls + Horizontal Scrollbar

        Scene scene = new Scene(root, 1200, 700, Color.LIGHTGRAY);
        primaryStage.setScene(scene);
        primaryStage.setTitle(WINDOW_TEXT);
        primaryStage.show();

        // Listeners for scrollbars to repaint when scrolling
        hScroll.valueProperty().addListener(o -> paintAll.run());
        vScroll.valueProperty().addListener(o -> paintAll.run());

        canvasPane.widthProperty().addListener(obs -> paintAll.run());
        canvasPane.heightProperty().addListener(obs -> paintAll.run());

        // tick of engine - one simulation step and repaint
        Runnable tick = () -> {
            simulation.step();
            Platform.runLater(paintAll);
        };

        primaryStage.setOnCloseRequest(event -> {
            boolean canClose;

            if (AppContext.RUN_DETAILS.mapChanged) { // confirm if unsaved changes
                canClose = DialogMaker.onCloseUnsavedChangesDialog(primaryStage);
            } else { // confirm if nothing is changed
                canClose = DialogMaker.showExitConfirmation(primaryStage);
            }

            // decide based on user input to close application or not
            if (!canClose) {
                event.consume();
            } else {
                // close confirmed, stop engine and exit
                if (engine != null && engine.getRunning()) {
                    engine.stop();
                }
                Platform.exit();
                System.exit(0);
            }
        });

        canvasPane.setOnMouseClicked(event -> {
            // get roads
            Road[] roads = simulation.getRoads();
            if (roads == null || roads.length == 0) return;

            // get y position with scroll offset
            double absoluteY = event.getY() + vScroll.getValue();

            int clickedRoadIndex = getClickedRoadIndex(absoluteY, roads, 20.0, 8.0);

            if (clickedRoadIndex != -1) {
                Road clickedRoad = roads[clickedRoadIndex];
                MyLogger.log("Clicked on road index: " + clickedRoadIndex + ", Road details: " +
                        "Lanes: " + clickedRoad.getNumberOfLanes() + ", Length: " + clickedRoad.getLength(),
                        Constants.INFO_FOR_LOGGING);
                ArrayList<RoadParameters> roadParams = RoadParameters.existingRoadsToRoadParameters(roads);
                DialogMaker.changeRoadsDialog(primaryStage, roadParams, 1, false,
                        clickedRoadIndex);
                if (AppContext.RUN_DETAILS.mapChanged) {
                    Road[] newRoads = RoadParameters.roadParametersToRoads(roadParams);
                    for (Road newRoad : newRoads) { // check if some generator needs to set up queues
                        newRoad.setUpQueuesIfNeeded();
                    }
                    AppContext.SIMULATION.resetSimulationWithNewRoads(newRoads);
                    paintAll.run();
                }
            } else {
                MyLogger.log("Clicked outside of any road.", Constants.INFO_FOR_LOGGING);
            }


        });

        // engine initialization
        engine = new CoreEngine(tick, AppContext.RUN_DETAILS.timeBetweenSteps);

        // first paint
        paintAll.run();
    }

    /**
     * creates menu with menu items and their actions which are toggleable, this is so that toggle options are synced
     * between menu and toolbar, for example lane change ban toggle button in toolbar and lane change ban menu
     * item in menu are synced using laneChangeProp BooleanProperty, when one of them is toggled, the laneChangeProp is
     * updated, which triggers the listener that calls the action to update the simulation and repaint, and since both
     * the toggle button and menu item are bound to the same laneChangeProp, they will both update to reflect the
     * change, this way we ensure that the toggle options are always in sync between menu and toolbar
     *
     * @param paintAll runnable to repaint all roads after actions that change the simulation
     **/
    private void setSyncedProperties(Runnable paintAll) {
        this.laneChangeProp = new SimpleBooleanProperty(!AppContext.RUN_DETAILS.laneChange);
        this.laneChangeProp.addListener((obs, oldVal, newVal) -> {
            Actions.changeLaneChangingAction(this.simulation, paintAll);  // call the action to update the simulation and repaint
        });

        this.collisionBanProp = new SimpleBooleanProperty(AppContext.RUN_DETAILS.preventCollisions);
        this.collisionBanProp.addListener((obs, oldVal, newVal) -> Actions.collisionBanAction(this.simulation, paintAll));

        boolean[] whatToExport = AppContext.RUN_DETAILS.outputDetails.output;
        this.whatToExportProps = new BooleanProperty[whatToExport.length];
        this.whatToExportProps[Constants.SIMULATION_DETAILS_OUTPUT_INDEX] =
                new SimpleBooleanProperty(whatToExport[Constants.SIMULATION_DETAILS_OUTPUT_INDEX]);
        this.whatToExportProps[Constants.SIMULATION_DETAILS_OUTPUT_INDEX].addListener((obs, oldVal, newVal) ->
                Actions.setOutputAction(Constants.SIMULATION_DETAILS_OUTPUT_INDEX));
        this.whatToExportProps[Constants.SIMULATION_TIME_OUTPUT_INDEX] =
                new SimpleBooleanProperty(whatToExport[Constants.SIMULATION_TIME_OUTPUT_INDEX]);
        this.whatToExportProps[Constants.SIMULATION_TIME_OUTPUT_INDEX].addListener((obs, oldVal, newVal) ->
                Actions.setOutputAction(Constants.SIMULATION_TIME_OUTPUT_INDEX));
        this.whatToExportProps[Constants.CARS_PASSED_OUTPUT_INDEX] =
                new SimpleBooleanProperty(whatToExport[Constants.CARS_PASSED_OUTPUT_INDEX]);
        this.whatToExportProps[Constants.CARS_PASSED_OUTPUT_INDEX].addListener((obs, oldVal, newVal) ->
                Actions.setOutputAction(Constants.CARS_PASSED_OUTPUT_INDEX));
        this.whatToExportProps[Constants.CARS_ON_ROAD_OUTPUT_INDEX] =
                new SimpleBooleanProperty(whatToExport[Constants.CARS_ON_ROAD_OUTPUT_INDEX]);
        this.whatToExportProps[Constants.CARS_ON_ROAD_OUTPUT_INDEX].addListener((obs, oldVal, newVal) ->
                Actions.setOutputAction(Constants.CARS_ON_ROAD_OUTPUT_INDEX));
        this.whatToExportProps[Constants.WHEN_WAS_ROAD_EMPTY_OUTPUT_INDEX] =
                new SimpleBooleanProperty(whatToExport[Constants.WHEN_WAS_ROAD_EMPTY_OUTPUT_INDEX]);
        this.whatToExportProps[Constants.WHEN_WAS_ROAD_EMPTY_OUTPUT_INDEX].addListener((obs, oldVal, newVal) ->
                Actions.setOutputAction(Constants.WHEN_WAS_ROAD_EMPTY_OUTPUT_INDEX));
        this.whatToExportProps[Constants.COLLISION_COUNT_OUTPUT_INDEX] =
                new SimpleBooleanProperty(whatToExport[Constants.COLLISION_COUNT_OUTPUT_INDEX]);
        this.whatToExportProps[Constants.COLLISION_COUNT_OUTPUT_INDEX].addListener((obs, oldVal, newVal) ->
                Actions.setOutputAction(Constants.COLLISION_COUNT_OUTPUT_INDEX));
        this.whatToExportProps[Constants.ROAD_DETAILS_OUTPUT_INDEX] =
                new SimpleBooleanProperty(whatToExport[Constants.ROAD_DETAILS_OUTPUT_INDEX]);
        this.whatToExportProps[Constants.ROAD_DETAILS_OUTPUT_INDEX].addListener((obs, oldVal, newVal) ->
                Actions.setOutputAction(Constants.ROAD_DETAILS_OUTPUT_INDEX));

        boolean[] logSettings = AppContext.RUN_DETAILS.log;
        this.logSettingsProps = new BooleanProperty[logSettings.length];
        this.logSettingsProps[Constants.GENERAL_LOGGING_INDEX] =
                new SimpleBooleanProperty(logSettings[Constants.GENERAL_LOGGING_INDEX]);
        this.logSettingsProps[Constants.GENERAL_LOGGING_INDEX].addListener((obs, oldVal, newVal) ->
                Actions.setLoggingAction(Constants.GENERAL_LOGGING_INDEX));
        this.logSettingsProps[Constants.INFO_LOGGING_INDEX] =
                new SimpleBooleanProperty(logSettings[Constants.INFO_LOGGING_INDEX]);
        this.logSettingsProps[Constants.INFO_LOGGING_INDEX].addListener((obs, oldVal, newVal) ->
                Actions.setLoggingAction(Constants.INFO_LOGGING_INDEX));
        this.logSettingsProps[Constants.WARN_LOGGING_INDEX] =
                new SimpleBooleanProperty(logSettings[Constants.WARN_LOGGING_INDEX]);
        this.logSettingsProps[Constants.WARN_LOGGING_INDEX].addListener((obs, oldVal, newVal) ->
                Actions.setLoggingAction(Constants.WARN_LOGGING_INDEX));
        this.logSettingsProps[Constants.ERROR_LOGGING_INDEX] =
                new SimpleBooleanProperty(logSettings[Constants.ERROR_LOGGING_INDEX]);
        this.logSettingsProps[Constants.ERROR_LOGGING_INDEX].addListener((obs, oldVal, newVal) ->
                Actions.setLoggingAction(Constants.ERROR_LOGGING_INDEX));
        this.logSettingsProps[Constants.FATAL_LOGGING_INDEX] =
                new SimpleBooleanProperty(logSettings[Constants.FATAL_LOGGING_INDEX]);
        this.logSettingsProps[Constants.FATAL_LOGGING_INDEX].addListener((obs, oldVal, newVal) ->
                Actions.setLoggingAction(Constants.FATAL_LOGGING_INDEX));
        this.logSettingsProps[Constants.DEBUG_LOGGING_INDEX] =
                new SimpleBooleanProperty(logSettings[Constants.DEBUG_LOGGING_INDEX]);
        this.logSettingsProps[Constants.DEBUG_LOGGING_INDEX].addListener((obs, oldVal, newVal) ->
                Actions.setLoggingAction(Constants.DEBUG_LOGGING_INDEX));
    }

    /**
     * helper method to get index of clicked road-based on y coordinate of click, roads and drawing parameters, used for
     * modification of roads by clicking on them in the GUI
     *
     * @param absoluteY y coordinate of click with scroll offset
     * @param roads array of roads to check against
     * @param GAP space between roads when drawing
     * @param LANE_HEIGHT height of one lane when drawing
     * @return index of clicked road, or -1 if click was outside any road
     **/
    private int getClickedRoadIndex(double absoluteY, Road[] roads, final double GAP, final double LANE_HEIGHT) {
        double currentY = GAP; // first road starts after initial gap

        for (int i = 0; i < roads.length; i++) {
            Road road = roads[i];

            // height of the road in pixels = number of lanes * height of one lane
            double roadHeight = road.getNumberOfLanes() * LANE_HEIGHT;

            // if y coordinate is within the current road
            if (absoluteY >= currentY && absoluteY <= (currentY + roadHeight)) {
                return i; // we have found the clicked road index
            }

            // shift currentY to the next road position
            currentY += roadHeight + GAP;
        }

        return -1; // no road was clicked, click was outside any road
    }

    /**
     * handles drawing of cellular roads
     *
     * @param roads array of roads to draw
     * @param canvas place to draw on
     * @param gc graphics context to draw on
     * @param GAP space between roads when drawing
     **/
    private void handleCellular(Road[] roads, Canvas canvas, GraphicsContext gc, final double GAP) {
        final double CELL_PIXEL_SIZE = 8.0; // size of cell for ONLY DRAWING, CELL SIZE IN MODEL MAY, AND LIKELY IS
        // DIFFERENT

        double neededHeight = GAP;
        double neededWidth  = 0;

        for (Road r : roads) {
            CellularRoad cr = (CellularRoad) r;
            int lanes = 1;
            int cols = 1;

            if (cr.getLength() > 0 && cr.getNumberOfLanes() > 0) {
                lanes = cr.getNumberOfLanes();
                cols = (int) (cr.getLengthInCells());
            }

            neededHeight += lanes * CELL_PIXEL_SIZE + GAP;
            neededWidth = Math.max(neededWidth, (cols + 1) * CELL_PIXEL_SIZE);
        }

        this.drawRoads(canvas, gc, roads, GAP, CELL_PIXEL_SIZE, neededWidth, neededHeight);
    }

    /**
     * handles drawing of continuous roads
     *
     * @param roads array of roads to draw
     * @param canvas place to draw on
     * @param gc graphics context to draw on
     * @param GAP space between roads when drawing
     **/
    private void handleContinuous(Road[] roads, Canvas canvas, GraphicsContext gc, final double GAP) {
        final double LANE_WIDTH = 8.0; // size of lane

        double neededHeight = GAP;
        double neededWidth  = 0;

        for (Road r : roads) {
            int lanes = 1;
            int length = 1;

            if (r.getLength() > 0 && r.getNumberOfLanes() > 0) {
                lanes = r.getNumberOfLanes();
                length = (int) (r.getLength() * Constants.CONTINOUS_ROAD_DRAWING_SCALE_FACTOR);
            }

            neededHeight += lanes * LANE_WIDTH + GAP;
            neededWidth = Math.max(neededWidth, length + LANE_WIDTH);
        }

        this.drawRoads(canvas, gc, roads, GAP, LANE_WIDTH, neededWidth, neededHeight);
    }

    /**
     * draws all roads on the canvas
     *
     * @param canvas place to draw on
     * @param gc graphics context to draw on
     * @param roads array of roads to draw
     * @param GAP space between roads when drawing
     * @param CELL_PIXEL_SIZE size of cell or lane for drawing
     * @param neededWidth needed width of canvas
     * @param neededHeight needed height of canvas
     **/
    private void drawRoads(Canvas canvas, GraphicsContext gc, Road[] roads, final double GAP,
                           final double CELL_PIXEL_SIZE, double neededWidth, double neededHeight) {

        // Setup Scrollbars based on content size
        double viewportW = canvas.getWidth();
        double viewportH = canvas.getHeight();

        double maxScrollH = Math.max(0, neededWidth - viewportW);
        double maxScrollV = Math.max(0, neededHeight - viewportH);

        hScroll.setMax(maxScrollH);
        vScroll.setMax(maxScrollV);

        // check so that the scroll bars aren't fucked up
        if (neededWidth > viewportW) {
            double visibleH = (viewportW / neededWidth) * maxScrollH;
            hScroll.setVisibleAmount(visibleH);
        } else {
            hScroll.setVisibleAmount(0); // nothing is overflowing
        }

        if (neededHeight > viewportH) {
            double visibleV = (viewportH / neededHeight) * maxScrollV;
            vScroll.setVisibleAmount(visibleV);
        } else {
            vScroll.setVisibleAmount(0);
        }

        // Get camera position
        double camX = hScroll.getValue();
        double camY = vScroll.getValue();

        // Clear Screen
        gc.clearRect(0, 0, viewportW, viewportH);
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, viewportW, viewportH);

        // Translate camera
        gc.save();
        gc.translate(-camX, -camY);

        double y = GAP;
        int i = 0;
        for (Road road : roads) {
            int lanes = road.getNumberOfLanes();
            double roadHeight = lanes * CELL_PIXEL_SIZE;

            // Optimization: Don't draw if road is outside vertical view
            if (y + roadHeight < camY || y > camY + viewportH) {
                y += roadHeight + GAP;
                i++;
                continue;
            }

            // info string (kept per road)
            String info = "Road: " + (i + 1) + " | Lanes: " + lanes + " | Length: " + road.getLength() + " | Cars on road: " +
                    road.getNumberOfCarsOnRoad() + " | Cars passed: " +
                    ResultsRecorder.getResultsRecorder().getCarsPassedOnRoad(i);
            gc.setFill(Color.BLACK);
            gc.fillText(info, 0, y - 5);
            gc.save();
            gc.translate(0, y);

            // Use max of neededWidth to ensure renderer doesn't cut off drawing loop
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (road) {
                AppContext.RENDERER.draw(gc, road, Math.max(neededWidth, viewportW), roadHeight, CELL_PIXEL_SIZE);
            }
            gc.restore();

            y += roadHeight + GAP;
            i++;
        }
        gc.restore(); // Restore translation
    }

    /**
     * helper method to create button with icon
     *
     * @param resourcePath path to icon in resources (e.g. "/icons/run.png")
     * @param tooltipText tooltip text for button
     * @return Button with icon
     */
    private Button createIconButton(String resourcePath, String tooltipText) {
        Button button = new Button();
        ImageView imageView = createMenuIcon(resourcePath);
        if (imageView == null) {
            MyLogger.log("Failed to create button, imageView is null for resource: " + resourcePath,
                    Constants.ERROR_FOR_LOGGING);
            button.setText("ERR");
            return button;
        }

        button.setGraphic(imageView);
        // set tooltip and focus traversable
        button.setTooltip(new Tooltip(tooltipText));
        button.setFocusTraversable(false);

        return button;
    }

    /**
     * helper method to create toggle (on/off) button with icon
     *
     * @param resourcePath path to icon in resources (e.g. "/icons/ban.png")
     * @param tooltipText tooltip text for button
     * @return ToggleButton with icon
     */
    private ToggleButton createIconToggleButton(String resourcePath, String tooltipText) {
        ToggleButton button = new ToggleButton();
        ImageView imageView = createMenuIcon(resourcePath);

        if (imageView == null) {
            MyLogger.log("Failed to create toggle button, imageView is null for resource: " + resourcePath,
                    Constants.ERROR_FOR_LOGGING);
            button.setText("ERR");
            return button;
        }

        button.setGraphic(imageView);
        button.setTooltip(new Tooltip(tooltipText));
        button.setFocusTraversable(false);

        String defaultStyle = "-fx-background-color: transparent; -fx-border-color: transparent;";
        String selectedStyle = "-fx-background-color: #b3d9ff; -fx-border-color: #66a3ff; -fx-border-radius: 3;";

        button.setStyle(defaultStyle);

        button.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                button.setStyle(selectedStyle);
            } else {
                button.setStyle(defaultStyle);
            }
        });

        return button;
    }

    /**
     * helper method to set button image
     *
     * @param resourcePath path to icon in resources (e.g. "/icons/run.png")
     * @param button button to set image on
     */
    private void setButtonImage(String resourcePath, Button button) {
        ImageView imageView = createMenuIcon(resourcePath);
        if (imageView == null) {
            MyLogger.log("Failed to set button image, imageView is null for resource: " + resourcePath,
                    Constants.ERROR_FOR_LOGGING);
            button.setText("ERR");
            return;
        }
        button.setText("");
        button.setGraphic(imageView);
    }

    /**
     * helper method to set menu item image
     *
     * @param resourcePath path to icon in resources (e.g. "/icons/run.png")
     * @param menuItem menu item to set image on
     */
    private void setButtonImage(String resourcePath, MenuItem menuItem) {
        ImageView imageView = createMenuIcon(resourcePath);
        if (imageView == null) {
            MyLogger.log("Failed to set menu item image, imageView is null for resource: " + resourcePath,
                    Constants.ERROR_FOR_LOGGING);
            menuItem.setText(menuItem.getText() + " ERR");
            return;
        }
        menuItem.setGraphic(imageView);
    }

    /**
     * helper method to create menu icon (image at buttons/menu items and similar ui stuff)
     *
     * @param resourcePath path to icon in resources (e.g. "/icons/run.png")
     * @return ImageView with icon
     **/
    private ImageView createMenuIcon(String resourcePath) {
        InputStream stream = getClass().getResourceAsStream(resourcePath);

        // check if resource was found
        if (stream == null) {
            MyLogger.log("Icon resource not found: " + resourcePath, Constants.ERROR_FOR_LOGGING);
            return null;
        }

        // create image and image view
        Image image = new Image(stream);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);
        imageView.setPreserveRatio(true);

        return imageView;
    }

    /**
     * Creates a ToggleButton that is bound to a BooleanProperty. This allows the button to reflect the state of the
     * property, used for toolbar
     *
     * @param iconPath path to icon in resources (e.g. "/icons/ban.png")
     * @param tooltip tooltip text for button
     * @param property BooleanProperty to bind the button's selected state to. When the property changes, the button
     *                 will update, and when the button is toggled, the property will update.
     * @return ToggleButton that is bound to the given BooleanProperty
     **/
    private ToggleButton createBoundToggleButton(String iconPath, String tooltip, BooleanProperty property) {
        ToggleButton btn = createIconToggleButton(iconPath, tooltip);

        btn.selectedProperty().bindBidirectional(property);

        return btn;
    }

    /**
     * Creates a CheckMenuItem that is bound to a BooleanProperty. This allows the menu item to reflect the state of the
     * property, used for menu
     *
     * @param text text for menu item
     * @param iconPath path to icon in resources (e.g. "/icons/ban.png")
     * @param property BooleanProperty to bind the menu item's selected state to. When the property changes, the menu item
     *                 will update, and when the menu item is toggled, the property will update.
     * @return CheckMenuItem that is bound to the given BooleanProperty
     */
    private CheckMenuItem createBoundMenuItem(String text, String iconPath, BooleanProperty property) {
        CheckMenuItem item;
        if (iconPath == null || iconPath.isEmpty()) {
            item = new CheckMenuItem(text);
        } else {
            item = new CheckMenuItem(text, createMenuIcon(iconPath));
        }

        item.selectedProperty().bindBidirectional(property);

        return item;
    }

    /**
     * creates toolbar with buttons
     *
     * @param primaryStage primary stage for file choosers
     * @param paintAll runnable to repaint all roads
     * @return ToolBar with buttons
     **/
    private ToolBar createToolBar(Stage primaryStage, Runnable paintAll) {
        String defaultStyle = "-fx-background-color: transparent; -fx-border-color: transparent;";

        // map file buttons
        Button newMapFileBtn = createIconButton("/icons/newMapFile.png", "New map file");
        Button editMapFileBtn = createIconButton("/icons/editMapFile.png", "Modify current map file");
        Button openMapFileBtn = createIconButton("/icons/openMapFile.png", "Open map file");
        Button saveMapFileBtn = createIconButton("/icons/saveMapFile.png", "Save map file");
        Button saveAsMapFileBtn = createIconButton("/icons/saveAsMapFile.png", "Save map file as...");
        ToggleButton changeLaneBtn = createBoundToggleButton("/icons/ban.png", "Toggle lane change ban",
                laneChangeProp);
        editMapFileBtn.setStyle(defaultStyle);
        newMapFileBtn.setStyle(defaultStyle);
        openMapFileBtn.setStyle(defaultStyle);
        saveMapFileBtn.setStyle(defaultStyle);
        saveAsMapFileBtn.setStyle(defaultStyle);

        // simulation control buttons
        toolbarStartStopBtn = createIconButton("/icons/run.png", "Start/Stop simulation");
        Button nextStepBtn = createIconButton("/icons/nextStep.png", "Next simulation step");
        Button resetBtn = createIconButton("/icons/reset.png", "Reset simulation");
        ToggleButton collisionBanBtn = createBoundToggleButton("/icons/collisionBan.png",
                "Ban collisions (toggle)", collisionBanProp);
        Button setTimeBetweenStepsBtn = createIconButton("/icons/time.png",
                "Set time between simulation steps (ms)");
        toolbarStartStopBtn.setStyle(defaultStyle);
        nextStepBtn.setStyle(defaultStyle);
        resetBtn.setStyle(defaultStyle);
        setTimeBetweenStepsBtn.setStyle(defaultStyle);

        // out file buttons
        Button exportResultsToTxtBtn = createIconButton("/icons/export.png", "Export results to TXT");
        Button exportToCsvBtn = createIconButton("/icons/csvSeparator.png", "Export results to CSV");
        Button setOutputFileNameBtn = createIconButton("/icons/exportFileName.png", "Set output file name");
        Button setCsvSeparatorBtn = createIconButton("/icons/csvSeparator.png", "Set CSV separator");
        Button whatToExportBtn = createIconButton("/icons/whatToExport.png", "What to export");
        whatToExportBtn.setOnAction(e -> {
            ContextMenu contextMenu = new ContextMenu();

            makeToggleWhatToExportMenu(contextMenu.getItems());

            contextMenu.show(whatToExportBtn, Side.BOTTOM, 0, 0);
        });
        exportResultsToTxtBtn.setStyle(defaultStyle);
        setOutputFileNameBtn.setStyle(defaultStyle);
        exportToCsvBtn.setStyle(defaultStyle);
        setCsvSeparatorBtn.setStyle(defaultStyle);
        whatToExportBtn.setStyle(defaultStyle);

        ToggleButton toggleAllLoggingBtn = //createIconToggleButton("/icons/log.png", "Toggle all logging");
                createBoundToggleButton("/icons/log.png", "Toggle all logging",
                        logSettingsProps[Constants.GENERAL_LOGGING_INDEX]);
        Button whatToLogBtn = createIconButton("/icons/whatToExport.png", "What to log");
        whatToLogBtn.setOnAction(e -> {
            ContextMenu contextMenu = new ContextMenu();
            makeToggleLoggingMenu(contextMenu.getItems());
            contextMenu.show(whatToLogBtn, Side.BOTTOM, 0, 0);
        });
        whatToLogBtn.setStyle(defaultStyle);


        newMapFileBtn.setOnAction(e -> Actions.newMapAction(primaryStage, paintAll));

        editMapFileBtn.setOnAction(e -> Actions.editMapFile(this.simulation, primaryStage, paintAll));

        toolbarStartStopBtn.setOnAction(e -> handleStartStopAction(primaryStage));

        resetBtn.setOnAction(e -> handleReset(primaryStage, paintAll));

        exportResultsToTxtBtn.setOnAction(e -> Actions.exportResultsToTxtAction(this.simulation));

        exportToCsvBtn.setOnAction(e -> Actions.exportResultsToCsvAction(this.simulation));

        nextStepBtn.setOnAction(e -> Actions.nextStepAction(simulation, paintAll));

        saveMapFileBtn.setOnAction(e -> Actions.saveMapAction(this.simulation));

        saveAsMapFileBtn.setOnAction(e -> Actions.saveMapAsAction(this.simulation, primaryStage));

        openMapFileBtn.setOnAction(e -> Actions.openMapAction(primaryStage, paintAll));

        setTimeBetweenStepsBtn.setOnAction(e -> Actions.setTimeBetweenStepsAction(primaryStage, engine));

        setOutputFileNameBtn.setOnAction(e -> Actions.setOutputFileAction());

        setCsvSeparatorBtn.setOnAction(e -> Actions.setCsvSeparatorAction(primaryStage));

        return new ToolBar(
                newMapFileBtn,
                editMapFileBtn,
                openMapFileBtn,
                saveMapFileBtn,
                saveAsMapFileBtn,
                toolbarStartStopBtn,
                nextStepBtn,
                resetBtn,
                changeLaneBtn,
                collisionBanBtn,
                setTimeBetweenStepsBtn,
                exportResultsToTxtBtn,
                exportToCsvBtn,
                setOutputFileNameBtn,
                setCsvSeparatorBtn,
                whatToExportBtn,
                toggleAllLoggingBtn,
                whatToLogBtn
        );
    }

    /**
     * helper method to create menu items for "what to export" menu, these menu items are toggleable and bound to
     * BooleanProperties
     *
     * @param items list of menu items to add the created menu items to
     **/
    private void makeToggleWhatToExportMenu(ObservableList<MenuItem> items) {
        CheckMenuItem simulationDetailsItem = createBoundMenuItem("Simulation details",
                null, whatToExportProps[Constants.SIMULATION_DETAILS_OUTPUT_INDEX]);
        CheckMenuItem simulationTimeItem = createBoundMenuItem("Simulation time",
                null, whatToExportProps[Constants.SIMULATION_TIME_OUTPUT_INDEX]);
        CheckMenuItem carsPassedItem = createBoundMenuItem("Cars passed",
                null, whatToExportProps[Constants.CARS_PASSED_OUTPUT_INDEX]);
        CheckMenuItem carsOnRoadItem = createBoundMenuItem("Cars on road",
                null, whatToExportProps[Constants.CARS_ON_ROAD_OUTPUT_INDEX]);
        CheckMenuItem whenWasRoadEmptyItem = createBoundMenuItem("When was road empty",
                null, whatToExportProps[Constants.WHEN_WAS_ROAD_EMPTY_OUTPUT_INDEX]);
        CheckMenuItem collisionsItem = createBoundMenuItem("Collisions",
                null, whatToExportProps[Constants.COLLISION_COUNT_OUTPUT_INDEX]);
        CheckMenuItem roadDetailsItem = createBoundMenuItem("Road details",
                null, whatToExportProps[Constants.ROAD_DETAILS_OUTPUT_INDEX]);

        items.addAll(
                simulationDetailsItem,
                simulationTimeItem,
                carsPassedItem,
                carsOnRoadItem,
                whenWasRoadEmptyItem,
                collisionsItem,
                roadDetailsItem
        );
    }

    /**
     * helper method to create menu items for "what to log" menu, these menu items are toggleable and bound to
     * BooleanProperties
     *
     * @param items list of menu items to add the created menu items to
     **/
    private void makeToggleLoggingMenu(ObservableList<MenuItem> items) {
        CheckMenuItem infoItem = createBoundMenuItem("Log info", null,
                logSettingsProps[Constants.INFO_LOGGING_INDEX]);
        CheckMenuItem warnItem = createBoundMenuItem("Log warnings", null,
                logSettingsProps[Constants.WARN_LOGGING_INDEX]);
        CheckMenuItem errorItem = createBoundMenuItem("Log errors", null,
                logSettingsProps[Constants.ERROR_LOGGING_INDEX]);
        CheckMenuItem fatalItem = createBoundMenuItem("Log fatal problems", null,
                logSettingsProps[Constants.FATAL_LOGGING_INDEX]);
        CheckMenuItem debugItem = createBoundMenuItem("Log debug info", null,
                logSettingsProps[Constants.DEBUG_LOGGING_INDEX]);


        items.addAll(
                infoItem,
                warnItem,
                errorItem,
                fatalItem,
                debugItem
        );
    }

    /**
     * creates menu bar with menu items
     *
     * @param primaryStage primary stage for file choosers
     * @param paintAll runnable to repaint all roads
     * @return MenuBar with menu items
     **/
    private MenuBar createMenu(Stage primaryStage, Runnable paintAll) {
        Menu fileMenu = createMapFileMenu(primaryStage, paintAll);
        Menu simulationMenu = createSimulationMenu(primaryStage, paintAll);
        Menu loggingMenu = createLoggingMenu();
        Menu outputMenu = createOutputMenu(primaryStage);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(simulationMenu);
        menuBar.getMenus().add(outputMenu);
        menuBar.getMenus().add(loggingMenu);

        return menuBar;
    }

    private Menu createMapFileMenu(Stage primaryStage, Runnable paintAll) {
        MenuItem itemNewFile = new MenuItem("New map file", createMenuIcon("/icons/newMapFile.png"));
        MenuItem itemEditFile = new MenuItem("Modify current map file", createMenuIcon("/icons/editMapFile.png"));
        MenuItem itemOpenFile = new MenuItem("Open map file", createMenuIcon("/icons/openMapFile.png"));
        MenuItem itemSaveFile = new MenuItem("Save map file", createMenuIcon("/icons/saveMapFile.png"));
        MenuItem itemSaveAsFile = new MenuItem("Save map file as...", createMenuIcon("/icons/saveAsMapFile.png"));

        itemNewFile.setOnAction(e -> Actions.newMapAction(primaryStage, paintAll));
        itemEditFile.setOnAction(e -> Actions.editMapFile(this.simulation, primaryStage, paintAll));
        itemOpenFile.setOnAction(e -> Actions.openMapAction(primaryStage, paintAll));
        itemSaveFile.setOnAction(e -> Actions.saveMapAction(this.simulation));
        itemSaveAsFile.setOnAction(e -> Actions.saveMapAsAction(this.simulation, primaryStage));

        Menu fileMenu = new Menu("Map file");
        fileMenu.getItems().addAll(
                itemNewFile,
                itemEditFile,
                itemOpenFile,
                itemSaveFile,
                itemSaveAsFile
        );

        return fileMenu;
    }

    /**
     * creates simulation menu with items for controlling the simulation
     *
     * @param primaryStage primary stage for file choosers
     * @param paintAll runnable to repaint all roads
     * @return Menu with items for controlling the simulation
     **/
    private Menu createSimulationMenu(Stage primaryStage, Runnable paintAll) {
        Menu simulationMenu = new Menu("Simulation");

        menuStartStopItem = new MenuItem("Start/Stop simulation", createMenuIcon("/icons/run.png"));
        MenuItem nextStepItem = new MenuItem("Next simulation step", createMenuIcon("/icons/nextStep.png"));
        MenuItem resetSimulationItem = new MenuItem("Reset simulation", createMenuIcon("/icons/reset.png"));
        CheckMenuItem changeLaneToggleItem = createBoundMenuItem("Toggle lane change ban",
                "/icons/ban.png", laneChangeProp);
        CheckMenuItem collisionBanToggleItem = createBoundMenuItem("Ban collisions (toggle)",
                "/icons/collisionBan.png", collisionBanProp);
        MenuItem setTimeBetweenStepsItem = new MenuItem("Set time between simulation steps (ms)",
                createMenuIcon("/icons/time.png"));

        menuStartStopItem.setOnAction(e -> handleStartStopAction(primaryStage));
        nextStepItem.setOnAction(e -> Actions.nextStepAction(simulation, paintAll));
        resetSimulationItem.setOnAction(e -> handleReset(primaryStage, paintAll));
        setTimeBetweenStepsItem.setOnAction(e -> Actions.setTimeBetweenStepsAction(primaryStage, engine));

        simulationMenu.getItems().addAll(
                menuStartStopItem,
                nextStepItem,
                resetSimulationItem,
                changeLaneToggleItem,
                collisionBanToggleItem,
                setTimeBetweenStepsItem
        );

        return simulationMenu;
    }

    /**
     * creates output menu with items for exporting results and setting output options
     *
     * @param primaryStage primary stage for file choosers
     * @return Menu with items for exporting results and setting output options
     **/
    private Menu createOutputMenu(Stage primaryStage) {
        Menu outputMenu = new Menu("Output");
        MenuItem exportResultsItem = new MenuItem("Export results to TXT",
                createMenuIcon("/icons/export.png"));
        MenuItem setOutputFileNameItem = new MenuItem("Set output file name",
                createMenuIcon("/icons/exportFileName.png"));
        MenuItem exportToCSVItem = new CheckMenuItem("Export to CSV");
        MenuItem setCsvSeparator = new MenuItem("Set CSV separator",
                createMenuIcon("/icons/csvSeparator.png"));
        Menu whatToExportSubMenu = new Menu("What to export",
                createMenuIcon("/icons/whatToExport.png"));

        makeToggleWhatToExportMenu(whatToExportSubMenu.getItems());

        setOutputFileNameItem.setOnAction(e -> Actions.setOutputFileAction());
        setCsvSeparator.setOnAction(e -> Actions.setCsvSeparatorAction(primaryStage));
        exportResultsItem.setOnAction(e -> Actions.exportResultsToTxtAction(this.simulation));
        exportToCSVItem.setOnAction(e -> Actions.exportResultsToCsvAction(this.simulation));

        outputMenu.getItems().addAll(
                exportResultsItem,
                exportToCSVItem,
                setOutputFileNameItem,
                setCsvSeparator,
                whatToExportSubMenu
        );

        return outputMenu;
    }

    /**
     * creates logging menu with items for toggling logging options
     *
     * @return Menu with items for toggling logging options
     **/
    private Menu createLoggingMenu() {
        Menu loggingMenu = new Menu("Logging");
        MenuItem toggleAllLoggingItem = createBoundMenuItem("Toggle all logging", "/icons/log.png",
                logSettingsProps[Constants.GENERAL_LOGGING_INDEX]);
        loggingMenu.getItems().add(toggleAllLoggingItem);
        makeToggleLoggingMenu(loggingMenu.getItems());
        return loggingMenu;
    }

    /**
     * stop method for JavaFX application
     **/
    @Override
    public void stop() {
        if (engine != null && engine.getRunning()) {
            engine.stop();
        }
        Platform.exit();
    }

    /**
     * main method to launch JavaFX application
     *
     * @param args command line arguments
     **/
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * helper method to handle start/stop action for simulation, used by both toolbar button and menu item to keep them
     * in sync
     **/
    private void handleStartStopAction(Stage stage) {
        if (this.simulation.getRoads() == null || this.simulation.getRoads().length == 0) {
            MyLogger.log("No roads in the simulation, cannot start/stop.", Constants.WARN_FOR_LOGGING);
            return;
        }

        boolean isRunning = engine.getRunning();

        if (!isRunning && simulation.getStepCount() <= 0 && AppContext.RUN_DETAILS.mapChanged) {
            boolean start = DialogMaker.askIfUserWantToSaveMap(stage, "Unsaved Changes",
                    "Unsaved changes in map.", "You have unsaved changes in current map. Any exported data will not" +
                            " reflect current map file. Do you wish to start simulation anyway?", "Start simulation");
            if (!start) {
                MyLogger.log("Start simulation cancelled by user.", Constants.INFO_FOR_LOGGING);
                return;
            }
        }

        if (isRunning) {
            engine.stop();
            MyLogger.log("Simulation stopped.", Constants.INFO_FOR_LOGGING);

            setButtonImage("/icons/run.png", toolbarStartStopBtn);
            setButtonImage("/icons/run.png", menuStartStopItem);
        } else {
            engine.start();
            MyLogger.log("Simulation started.", Constants.INFO_FOR_LOGGING);

            setButtonImage("/icons/stop.png", toolbarStartStopBtn);
            setButtonImage("/icons/stop.png", menuStartStopItem);
        }
    }

    /**
     * helper method to handle reset action for simulation, used by both toolbar button and menu item
     *
     * @param stage primary stage for confirmation dialog
     * @param paintAll runnable to repaint all roads after reset
     **/
    private void handleReset(Stage stage, Runnable paintAll) {
        if (simulation.getStepCount() > 0) {
            boolean reset = DialogMaker.confirmDialog(stage, "Reset Simulation Confirmation",
                    "Changing road properties will reset the simulation state.");
            if (!reset) {
                MyLogger.log("Reset simulation cancelled by user.", Constants.INFO_FOR_LOGGING);
                return;
            }
        }
        Actions.resetSimulationAction(simulation, paintAll);
    }
}