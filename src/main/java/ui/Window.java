package ui;

import app.AppContext;
import core.engine.CoreEngine;
import core.model.Road;
import core.model.cellular.Cell;
import core.model.cellular.CellularRoad;
import core.utils.*;
import core.utils.constants.Constants;
import ui.render.IRoadRenderer;
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

    /** renderer for drawing roads **/
    private IRoadRenderer renderer;

    /** horizontal scrollbar **/
    private ScrollBar hScroll;

    /** vertical scrollbar **/
    private ScrollBar vScroll;

    /** info label at the bottom **/
    private Label infoLabel;

    /**
     * start method for JavaFX application
     *
     * @param primaryStage primary stage for JavaFX application
     **/
    @Override
    public void start(Stage primaryStage) {
        // get renderer and simulation from AppContext
        this.simulation = AppContext.SIMULATION;
        this.renderer = AppContext.RENDERER;

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
                Road[] newRoads = RoadParameters.roadParametersToRoads(roadParams);
                for (Road newRoad : newRoads) { // check if some generator needs to set up queues
                    newRoad.setUpQueuesIfNeeded();
                }
                AppContext.SIMULATION.resetSimulationWithNewRoads(newRoads);
                paintAll.run();
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
                renderer.draw(gc, road, Math.max(neededWidth, viewportW), roadHeight, CELL_PIXEL_SIZE);
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
     */
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
        ToggleButton changeLaneBtn = createIconToggleButton("/icons/ban.png", "Toggle lane change ban");
        editMapFileBtn.setStyle(defaultStyle);
        newMapFileBtn.setStyle(defaultStyle);
        openMapFileBtn.setStyle(defaultStyle);
        saveMapFileBtn.setStyle(defaultStyle);
        saveAsMapFileBtn.setStyle(defaultStyle);

        // simulation control buttons
        Button startStopBtn = createIconButton("/icons/run.png", "Start/Stop simulation");
        Button nextStepBtn = createIconButton("/icons/nextStep.png", "Next simulation step");
        ToggleButton collisionBanBtn = createIconToggleButton("/icons/collisionBan.png",
                "Ban collisions (toggle)");
        Button setTimeBetweenStepsBtn = createIconButton("/icons/time.png",
                "Set time between simulation steps (ms)");
        startStopBtn.setStyle(defaultStyle);
        nextStepBtn.setStyle(defaultStyle);
        setTimeBetweenStepsBtn.setStyle(defaultStyle);
        changeLaneBtn.setSelected(!AppContext.RUN_DETAILS.laneChange);
        collisionBanBtn.setSelected(AppContext.RUN_DETAILS.preventCollisions);

        // out file buttons
        Button exportResultsToTxtBtn = createIconButton("/icons/export.png", "Export results to TXT");
        Button exportToCsvBtn = createIconButton("/icons/csvSeparator.png", "Export results to CSV");
        Button setOutputFileNameBtn = createIconButton("/icons/exportFileName.png", "Set output file name");
        Button setCsvSeparatorBtn = createIconButton("/icons/csvSeparator.png", "Set CSV separator");
        Button whatToExportBtn = createIconButton("/icons/whatToExport.png", "What to export");
        whatToExportBtn.setOnAction(e -> {
            ContextMenu contextMenu = new ContextMenu();

            CheckMenuItem simulationDetailsItem = new CheckMenuItem("Simulation details");
            CheckMenuItem simulationTimeItem = new CheckMenuItem("Simulation time");
            CheckMenuItem carsPassedItem = new CheckMenuItem("Cars passed");
            CheckMenuItem carsOnRoadItem = new CheckMenuItem("Cars on road");
            CheckMenuItem collisionsItem = new CheckMenuItem("Collisions");
            CheckMenuItem roadDetailsItem = new CheckMenuItem("Road details");


            contextMenu.getItems().addAll(
                    simulationDetailsItem,
                    simulationTimeItem,
                    carsPassedItem,
                    carsOnRoadItem,
                    collisionsItem,
                    roadDetailsItem
            );

            contextMenu.show(whatToExportBtn, Side.BOTTOM, 0, 0);
        });
        exportResultsToTxtBtn.setStyle(defaultStyle);
        setOutputFileNameBtn.setStyle(defaultStyle);
        exportToCsvBtn.setStyle(defaultStyle);
        setCsvSeparatorBtn.setStyle(defaultStyle);
        whatToExportBtn.setStyle(defaultStyle);

        ToggleButton toggleAllLoggingBtn = createIconToggleButton("/icons/log.png", "Toggle all logging");
        Button whatToLogBtn = createIconButton("/icons/whatToExport.png", "What to log");
        whatToLogBtn.setOnAction(e -> {
            ContextMenu contextMenu = new ContextMenu();

            CheckMenuItem infoItem = new CheckMenuItem("Log info");
            CheckMenuItem warnItem = new CheckMenuItem("Log warnings");
            CheckMenuItem errorItem = new CheckMenuItem("Log errors");
            CheckMenuItem fatalItem = new CheckMenuItem("Log fatal problems");
            CheckMenuItem debugItem = new CheckMenuItem("Log debug info");

            infoItem.setSelected(AppContext.RUN_DETAILS.log[Constants.INFO_LOGGING_INDEX]);
            warnItem.setSelected(AppContext.RUN_DETAILS.log[Constants.WARN_LOGGING_INDEX]);
            errorItem.setSelected(AppContext.RUN_DETAILS.log[Constants.ERROR_LOGGING_INDEX]);
            fatalItem.setSelected(AppContext.RUN_DETAILS.log[Constants.FATAL_LOGGING_INDEX]);
            debugItem.setSelected(AppContext.RUN_DETAILS.log[Constants.DEBUG_LOGGING_INDEX]);

            infoItem.setOnAction(ev -> Actions.setLoggingAction(Constants.INFO_LOGGING_INDEX));
            warnItem.setOnAction(ev -> Actions.setLoggingAction(Constants.WARN_LOGGING_INDEX));
            errorItem.setOnAction(ev -> Actions.setLoggingAction(Constants.ERROR_LOGGING_INDEX));
            fatalItem.setOnAction(ev -> Actions.setLoggingAction(Constants.FATAL_LOGGING_INDEX));
            debugItem.setOnAction(ev -> Actions.setLoggingAction(Constants.DEBUG_LOGGING_INDEX));

            contextMenu.getItems().addAll(
                    infoItem,
                    warnItem,
                    errorItem,
                    fatalItem,
                    debugItem
            );

            contextMenu.show(whatToLogBtn, Side.BOTTOM, 0, 0);
        });
        toggleAllLoggingBtn.setSelected(AppContext.RUN_DETAILS.log[0]);
        //toggleAllLoggingBtn.setStyle(defaultStyle);
        whatToLogBtn.setStyle(defaultStyle);


        newMapFileBtn.setOnAction(e -> Actions.newMapAction(primaryStage, paintAll));

        editMapFileBtn.setOnAction(e -> Actions.editMapFile(primaryStage, paintAll));

        startStopBtn.setOnAction(e -> {
            if (engine.getRunning()) {
                setButtonImage("/icons/run.png", startStopBtn);
                engine.stop();
                MyLogger.log("Simulation stopped via toolbar button", Constants.INFO_FOR_LOGGING);
            } else {
                setButtonImage("/icons/stop.png", startStopBtn);
                engine.start();
                MyLogger.log("Simulation started via toolbar button", Constants.INFO_FOR_LOGGING);
            }
        });

        exportResultsToTxtBtn.setOnAction(e -> Actions.exportResultsToTxtAction());

        exportToCsvBtn.setOnAction(e -> Actions.exportResultsToCsvAction());

        nextStepBtn.setOnAction(e -> Actions.nextStepAction(simulation, paintAll));

        changeLaneBtn.setOnAction(e -> Actions.changeLaneChangingAction(paintAll));

        saveMapFileBtn.setOnAction(e -> Actions.saveMapAction());

        saveAsMapFileBtn.setOnAction(e -> Actions.saveMapAsAction(primaryStage));

        openMapFileBtn.setOnAction(e -> Actions.openMapAction(primaryStage, paintAll));

        collisionBanBtn.setOnAction(e -> Actions.collisionBanAction(paintAll));

        setTimeBetweenStepsBtn.setOnAction(e -> Actions.setTimeBetweenStepsAction(primaryStage, engine));

        setOutputFileNameBtn.setOnAction(e -> Actions.setOutputFileAction());

        setCsvSeparatorBtn.setOnAction(e -> Actions.setCsvSeparatorAction(primaryStage));

        toggleAllLoggingBtn.setOnAction(e -> Actions.setLoggingAction(Constants.GENERAL_LOGGING_INDEX));

        return new ToolBar(
                newMapFileBtn,
                editMapFileBtn,
                openMapFileBtn,
                saveMapFileBtn,
                saveAsMapFileBtn,
                startStopBtn,
                nextStepBtn,
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

        itemEditFile.setOnAction(e -> Actions.editMapFile(primaryStage, paintAll));

        itemOpenFile.setOnAction(e -> Actions.openMapAction(primaryStage, paintAll));

        itemSaveFile.setOnAction(e -> Actions.saveMapAction());

        itemSaveAsFile.setOnAction(e -> Actions.saveMapAsAction(primaryStage));

        Menu fileMenu = new Menu("Map file");
        fileMenu.getItems().addAll(itemNewFile, itemEditFile, itemOpenFile, itemSaveFile, itemSaveAsFile);

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
        MenuItem startStopItem = new MenuItem("Start/Stop simulation", createMenuIcon("/icons/run.png"));
        MenuItem nextStepItem = new MenuItem("Next simulation step", createMenuIcon("/icons/nextStep.png"));
        CheckMenuItem changeLaneToggleItem = new CheckMenuItem("Toggle lane change ban", createMenuIcon("/icons/ban.png"));
        CheckMenuItem collisionBanToggleItem = new CheckMenuItem("Ban collisions (toggle)",
                createMenuIcon("/icons/collisionBan.png"));
        MenuItem setTimeBetweenStepsItem = new MenuItem("Set time between simulation steps (ms)",
                createMenuIcon("/icons/time.png"));

        changeLaneToggleItem.setSelected(!AppContext.RUN_DETAILS.laneChange);
        collisionBanToggleItem.setSelected(AppContext.RUN_DETAILS.preventCollisions);

        startStopItem.setOnAction(e -> {
            if (engine.getRunning()) {
                setButtonImage("/icons/run.png", startStopItem);
                engine.stop();
                MyLogger.log("Simulation stopped via toolbar button", Constants.INFO_FOR_LOGGING);
            } else {
                setButtonImage("/icons/stop.png", startStopItem);
                engine.start();
                MyLogger.log("Simulation started via toolbar button", Constants.INFO_FOR_LOGGING);
            }
        });

        nextStepItem.setOnAction(e -> Actions.nextStepAction(simulation, paintAll));

        changeLaneToggleItem.setOnAction(e -> Actions.changeLaneChangingAction(paintAll));

        collisionBanToggleItem.setOnAction(e -> Actions.collisionBanAction(paintAll));

        setTimeBetweenStepsItem.setOnAction(e -> Actions.setTimeBetweenStepsAction(primaryStage, engine));

        simulationMenu.getItems().addAll(startStopItem, nextStepItem, changeLaneToggleItem, collisionBanToggleItem,
                setTimeBetweenStepsItem);

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
        CheckMenuItem simulationDetailsItem = new CheckMenuItem("Simulation details");
        CheckMenuItem simulationTimeItem = new CheckMenuItem("Simulation time");
        CheckMenuItem carsPassedItem = new CheckMenuItem("Cars passed");
        CheckMenuItem carsOnRoadItem = new CheckMenuItem("Cars on road");
        CheckMenuItem collisionsItem = new CheckMenuItem("Collisions");
        CheckMenuItem roadDetailsItem = new CheckMenuItem("Road details");
        whatToExportSubMenu.getItems().addAll(simulationDetailsItem, simulationTimeItem, carsPassedItem, carsOnRoadItem,
                collisionsItem, roadDetailsItem);

        setOutputFileNameItem.setOnAction(e -> Actions.setOutputFileAction());

        setCsvSeparator.setOnAction(e -> Actions.setCsvSeparatorAction(primaryStage));

        exportResultsItem.setOnAction(e -> Actions.exportResultsToTxtAction());

        exportToCSVItem.setOnAction(e -> Actions.exportResultsToCsvAction());

        outputMenu.getItems().addAll(exportResultsItem, exportToCSVItem, setOutputFileNameItem, setCsvSeparator,
                whatToExportSubMenu);

        return outputMenu;
    }

    /**
     * creates logging menu with items for toggling logging options
     *
     * @return Menu with items for toggling logging options
     **/
    private Menu createLoggingMenu() {
        Menu loggingMenu = new Menu("Logging");
        CheckMenuItem toggleAllLogItem = new CheckMenuItem("Toggle all logging");
        toggleAllLogItem.setSelected(AppContext.RUN_DETAILS.log[Constants.GENERAL_LOGGING_INDEX]);
        CheckMenuItem toggleInfoLogItem = new CheckMenuItem("Toggle info logging");
        toggleInfoLogItem.setSelected(AppContext.RUN_DETAILS.log[Constants.INFO_LOGGING_INDEX]);
        CheckMenuItem toggleWarnLogItem = new CheckMenuItem("Toggle warning logging");
        toggleWarnLogItem.setSelected(AppContext.RUN_DETAILS.log[Constants.WARN_LOGGING_INDEX]);
        CheckMenuItem toggleErrorLogItem = new CheckMenuItem("Toggle error logging");
        toggleErrorLogItem.setSelected(AppContext.RUN_DETAILS.log[Constants.ERROR_LOGGING_INDEX]);
        CheckMenuItem toggleFatalLogItem = new CheckMenuItem("Toggle fatal logging");
        toggleFatalLogItem.setSelected(AppContext.RUN_DETAILS.log[Constants.FATAL_LOGGING_INDEX]);
        CheckMenuItem toggleDebugLogItem = new CheckMenuItem("Toggle debug logging");
        toggleDebugLogItem.setSelected(AppContext.RUN_DETAILS.log[Constants.DEBUG_LOGGING_INDEX]);
        loggingMenu.getItems().addAll(toggleAllLogItem, toggleInfoLogItem, toggleWarnLogItem,
                toggleDebugLogItem, toggleErrorLogItem, toggleFatalLogItem);

        toggleAllLogItem.setOnAction(e -> Actions.setLoggingAction(Constants.GENERAL_LOGGING_INDEX));

        toggleInfoLogItem.setOnAction(e -> Actions.setLoggingAction(Constants.INFO_LOGGING_INDEX));

        toggleWarnLogItem.setOnAction(e -> Actions.setLoggingAction(Constants.WARN_LOGGING_INDEX));

        toggleErrorLogItem.setOnAction(e -> Actions.setLoggingAction(Constants.ERROR_LOGGING_INDEX));

        toggleFatalLogItem.setOnAction(e -> Actions.setLoggingAction(Constants.FATAL_LOGGING_INDEX));

        toggleDebugLogItem.setOnAction(e -> Actions.setLoggingAction(Constants.DEBUG_LOGGING_INDEX));

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
}