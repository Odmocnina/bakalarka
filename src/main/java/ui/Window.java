package ui;

import app.AppContext;
import core.engine.CoreEngine;
import core.model.Road;
import core.model.cellular.Cell;
import core.model.cellular.CellularRoad;
import core.utils.*;
import core.utils.constants.Constants;
import core.utils.loading.RoadLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.w3c.dom.events.Event;
import ui.render.IRoadRenderer;
import core.sim.Simulation;

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

import java.io.File;
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
            boolean canClose = true;

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

    private int getClickedRoadIndex(double absoluteY, Road[] roads, final double GAP, final double LANE_HEIGHT) {
        double currentY = GAP; // První silnice začíná s odsazením GAP

        for (int i = 0; i < roads.length; i++) {
            Road road = roads[i];

            // Výška silnice v pixelech = počet pruhů * výška jednoho pruhu
            double roadHeight = road.getNumberOfLanes() * LANE_HEIGHT;

            // Pokud absolutní Y kliknutí spadá do intervalu této silnice
            if (absoluteY >= currentY && absoluteY <= (currentY + roadHeight)) {
                return i; // Našli jsme silnici
            }

            // Posuneme se o výšku této silnice a mezeru pro další iteraci
            currentY += roadHeight + GAP;
        }

        return -1; // Kliknutí padlo mimo silnice (např. do mezery)
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

        Button newMapFileBtn = createIconButton("/icons/newMapFile.png", "New map file");
        Button editMapFileBtn = createIconButton("/icons/editMapFile.png", "Modify current map file");
        Button openMapFileBtn = createIconButton("/icons/openMapFile.png", "Open map file");
        Button saveMapFileBtn = createIconButton("/icons/saveMapFile.png", "Save map file");
        Button saveAsMapFileBtn = createIconButton("/icons/saveAsMapFile.png", "Save map file as...");
        ToggleButton changeLaneBtn = createIconToggleButton("/icons/ban.png", "Toggle lane change ban");
        Button startStopBtn = createIconButton("/icons/run.png", "Start/Stop simulation");
        Button exportResultsBtn = createIconButton("/icons/export.png", "Export results " +
                "(current state of simulation)");
        Button nextStepBtn = createIconButton("/icons/nextStep.png", "Next simulation step");
        ToggleButton collisionBanBtn = createIconToggleButton("/icons/collisionBan.png",
                "Ban collisions (toggle)");

        changeLaneBtn.setStyle(defaultStyle);
        editMapFileBtn.setStyle(defaultStyle);
        newMapFileBtn.setStyle(defaultStyle);
        openMapFileBtn.setStyle(defaultStyle);
        saveMapFileBtn.setStyle(defaultStyle);
        saveAsMapFileBtn.setStyle(defaultStyle);
        startStopBtn.setStyle(defaultStyle);
        exportResultsBtn.setStyle(defaultStyle);
        nextStepBtn.setStyle(defaultStyle);
        collisionBanBtn.setStyle(defaultStyle);

        newMapFileBtn.setOnAction(e -> {
            Actions.newMapAction(primaryStage, paintAll);
        });

        editMapFileBtn.setOnAction(e -> {
            Actions.editMapFile(primaryStage, paintAll);
        });

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

        exportResultsBtn.setOnAction(e -> {
            Actions.exportResultsAction(primaryStage);
        });

        nextStepBtn.setOnAction(e -> {
            simulation.step();
            paintAll.run();
        });

        changeLaneBtn.setOnAction(e -> {
            Actions.changeLaneChangingAction(paintAll);
        });

        saveMapFileBtn.setOnAction(e -> {
            Actions.saveMapAction();
        });

        saveAsMapFileBtn.setOnAction(e -> {
            Actions.saveMapAsAction(primaryStage);
        });

        openMapFileBtn.setOnAction(e -> {
            Actions.openMapAction(primaryStage, paintAll);
        });

        collisionBanBtn.setOnAction(e -> {
            Actions.collisionBanAction(paintAll);
        });

        return new ToolBar(
                startStopBtn,
                nextStepBtn,
                newMapFileBtn,
                editMapFileBtn,
                openMapFileBtn,
                saveMapFileBtn,
                saveAsMapFileBtn,
                changeLaneBtn,
                collisionBanBtn,
                exportResultsBtn
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
        MenuItem itemNewFile = new MenuItem("New map file", createMenuIcon("/icons/newMapFile.png"));
        MenuItem itemEditFile = new MenuItem("Modify current map file", createMenuIcon("/icons/editMapFile.png"));
        MenuItem itemOpenFile = new MenuItem("Open map file", createMenuIcon("/icons/openMapFile.png"));
        MenuItem itemSaveFile = new MenuItem("Save map file", createMenuIcon("/icons/saveMapFile.png"));
        MenuItem itemSaveAsFile = new MenuItem("Save map file as...", createMenuIcon("/icons/saveAsMapFile.png"));

        itemNewFile.setOnAction(e -> {
            Actions.newMapAction(primaryStage, paintAll);
        });

        itemEditFile.setOnAction(e -> {
            Actions.editMapFile(primaryStage, paintAll);
        });

        itemOpenFile.setOnAction(e -> {
            Actions.openMapAction(primaryStage, paintAll);
        });

        itemSaveFile.setOnAction(e -> {
            Actions.saveMapAction();
        });

        itemSaveAsFile.setOnAction(e -> {
            Actions.saveMapAsAction(primaryStage);
        });

        Menu fileMenu = new Menu("Map file");
        fileMenu.getItems().addAll(itemNewFile, itemEditFile, itemOpenFile, itemSaveFile, itemSaveAsFile);

        Menu loggingMenu = new Menu("Logging");
        CheckMenuItem toggleAllLogItem = new CheckMenuItem("Toggle all logging");
        toggleAllLogItem.setSelected(AppContext.RUN_DETAILS.log[0]);
        CheckMenuItem toggleInfoLogItem = new CheckMenuItem("Toggle info logging");
        toggleInfoLogItem.setSelected(AppContext.RUN_DETAILS.log[1]);
        CheckMenuItem toggleWarnLogItem = new CheckMenuItem("Toggle warning logging");
        toggleWarnLogItem.setSelected(AppContext.RUN_DETAILS.log[2]);
        CheckMenuItem toggleErrorLogItem = new CheckMenuItem("Toggle error logging");
        toggleErrorLogItem.setSelected(AppContext.RUN_DETAILS.log[3]);
        CheckMenuItem toggleFatalLogItem = new CheckMenuItem("Toggle fatal logging");
        toggleFatalLogItem.setSelected(AppContext.RUN_DETAILS.log[4]);
        CheckMenuItem toggleDebugLogItem = new CheckMenuItem("Toggle debug logging");
        toggleDebugLogItem.setSelected(AppContext.RUN_DETAILS.log[5]);
        loggingMenu.getItems().addAll(toggleAllLogItem, toggleInfoLogItem, toggleWarnLogItem,
                toggleDebugLogItem, toggleErrorLogItem, toggleFatalLogItem);

        toggleAllLogItem.setOnAction(e -> {
            ConfigModificator.changeLogging(0);
        });

        toggleInfoLogItem.setOnAction(e -> {
            ConfigModificator.changeLogging(1);
        });

        toggleWarnLogItem.setOnAction(e -> {
            ConfigModificator.changeLogging(2);
        });

        toggleErrorLogItem.setOnAction(e -> {
            ConfigModificator.changeLogging(3);
        });

        toggleFatalLogItem.setOnAction(e -> {
            ConfigModificator.changeLogging(4);
        });

        toggleDebugLogItem.setOnAction(e -> {
            ConfigModificator.changeLogging(5);
        });

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(loggingMenu);

        return menuBar;
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