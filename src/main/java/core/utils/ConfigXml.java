package core.utils;

import app.AppContext;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/**
 * helper pro práci s config.xml – drží si odkazy na důležité sekce
 */
public class ConfigXml {

    private final File file;
    private final Document doc;

    private final Element root;
    private final Element generator;
    private final Element runDetails;
    private final Element model;

    public ConfigXml(File fileConf) throws Exception {
        this.file = fileConf;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        this.doc = builder.parse(file);

        this.root = doc.getDocumentElement();

        // tady si vytáhneš a uložíš podsekce
        this.generator = getFirstChildElementByTag(root, ConfigConstants.GENERATOR_TAG);
        this.runDetails = getFirstChildElementByTag(root, ConfigConstants.RUN_DETAILS_TAG);
        this.model = getFirstChildElementByTag(root, ConfigConstants.MODELS_TAG);
    }

    // ====== obecné pomocné metody ======

    private Element getFirstChildElementByTag(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        if (nl.getLength() == 0) return null;
        return (Element) nl.item(0);
    }

    private Element getDirectChild(Element parent, String tag) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n instanceof Element && ((Element) n).getTagName().equals(tag)) {
                return (Element) n;
            }
        }
        return null;
    }

    private String getSingleChildText(Element parent, String childName) {
        Element child = getDirectChild(parent, childName);
        return (child != null) ? child.getTextContent().trim() : null;
    }

    private void setSingleChildText(Element parent, String childName, String value) {
        Element child = getDirectChild(parent, childName);
        if (child == null) {
            child = doc.createElement(childName);
            parent.appendChild(child);
        }
        child.setTextContent(value);
    }

    public void save() throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        transformer.transform(new DOMSource(doc), new StreamResult(file));
    }

    // ====== konkrétní „API“ pro GUI ======
    // GENERATOR

    public double getFlowRate() {
        String txt = getSingleChildText(generator, ConfigConstants.FLOW_RATE_TAG);
        return (txt != null && !txt.isEmpty()) ? Double.parseDouble(txt) : 0.0;
    }

    public void setFlowRate(double value) {
        setSingleChildText(generator, ConfigConstants.FLOW_RATE_TAG, String.valueOf(value));
    }

    // queue/use
    public boolean isQueueUsed() {
        Element queue = getDirectChild(generator, ConfigConstants.QUEUE_TAG);
        if (queue == null) return false;
        String txt = getSingleChildText(queue, ConfigConstants.USE_TAG);
        return Boolean.parseBoolean(txt);
    }

    public void setQueueUsed(boolean use) {
        Element queue = getDirectChild(generator, ConfigConstants.QUEUE_TAG);
        if (queue == null) {
            queue = doc.createElement(ConfigConstants.QUEUE_TAG);
            generator.appendChild(queue);
        }
        setSingleChildText(queue, ConfigConstants.USE_TAG, String.valueOf(use));
    }

    public int getTimeStep() {
        String txt = getSingleChildText(runDetails, "timeStep");
        return (txt != null && !txt.isEmpty()) ? Integer.parseInt(txt) : 1;
    }

    public void setTimeStep(int ts) {
        setSingleChildText(runDetails, "timeStep", String.valueOf(ts));
    }

    public int getNumberOfRoads() {
        return 0;
    }

    public String getRoadFile() {
        return AppContext.RUN_DETAILS.outputDetails.outputFile;
    }

    public void setRoadFile(String filePath) {

    }

    public void setNumberOfRoads(int numRoads) {

    }

}
