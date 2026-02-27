package core.utils.loading;

import app.AppContext;
import core.model.Road;
import core.utils.RunDetails;
import core.utils.constants.Constants;
import core.utils.constants.RoadLoadingConstants;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*************************
 * Unit tests for RoadLoader class, focusing on map loading and XML parsing logic
 *
 * @author Michael Hladky
 * @version 1.0
 ************************/
public class RoadLoaderTest {

    /** mock for car following model to satisfy AppContext requirements during loading **/
    @Mock
    private ICarFollowingModel mockCarFollowingModel;

    /** mock for lane changing model to satisfy AppContext requirements during loading **/
    @Mock
    private ILaneChangingModel mockLaneChangingModel;

    /**
     * setup method to initialize required global state and mocks before each test
     **/
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Prepare AppContext with mocks
        AppContext.CAR_FOLLOWING_MODEL = mockCarFollowingModel;
        AppContext.LANE_CHANGING_MODEL = mockLaneChangingModel;
        AppContext.RUN_DETAILS = new RunDetails();

        // Return empty arrays instead of null to prevent NPE in String.split()
        String[] emptyParams = new String[0];
        lenient().when(mockCarFollowingModel.getType()).thenReturn(Constants.CONTINUOUS);
        lenient().when(mockCarFollowingModel.getID()).thenReturn("test-model");
        lenient().when(mockCarFollowingModel.getCellSize()).thenReturn(1.0);
        lenient().when(mockCarFollowingModel.getParametersForGeneration()).thenReturn(Arrays.toString(emptyParams));
        lenient().when(mockLaneChangingModel.getParametersForGeneration()).thenReturn(Arrays.toString(emptyParams));
    }

    /**
     * test to verify that loadRoad returns null when an unknown car following model type is set in AppContext
     **/
    @Test
    void loadRoad_ShouldReturnNullForUnknownModelType() throws Exception {
        // Arrange: Build a dummy XML element for the road
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element roadElement = doc.createElement(RoadLoadingConstants.ROAD_TAG);

        Element length = doc.createElement(RoadLoadingConstants.ROAD_LENGTH_TAG);
        length.setTextContent("1000.0");
        roadElement.appendChild(length);

        Element maxSpeed = doc.createElement(RoadLoadingConstants.ROAD_MAX_SPEED_TAG);
        maxSpeed.setTextContent("50.0");
        roadElement.appendChild(maxSpeed);

        Element numberOfLanes = doc.createElement(RoadLoadingConstants.NUMBER_OF_LANES_TAG);
        numberOfLanes.setTextContent("1");
        roadElement.appendChild(numberOfLanes);

        // Setup mock to return an invalid type specifically for this test
        when(mockCarFollowingModel.getType()).thenReturn("UNKNOWN_MODEL");

        // Act
        Road result = RoadLoader.loadRoad(roadElement, 0);

        // Assert
        assertNull(result, "Should return null for unknown car following model type");
    }

    /**
     * test to verify that loadMapStart returns null when the provided file path does not exist
     **/
    @Test
    void loadMapStart_ShouldReturnNullWhenFileNotFound() {
        String invalidFilePath = "non_existent.xml";
        Road[] result = RoadLoader.loadMapStart(invalidFilePath);
        assertNull(result, "Should return null for non-existent file path");
    }


}