package core.utils;

import core.utils.constants.ConfigConstants;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Helper for working with config.xml using standard w3c.dom.
 * Allows reading/writing values using path strings (e.g., "logging/debug").
 */
public class ConfigXml {

    private final File file;
    private final Document doc;
    private final Element root;

    public ConfigXml(File fileConf) throws Exception {
        this.file = fileConf;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        this.doc = builder.parse(file);

        this.root = doc.getDocumentElement();
    }

    // ==========================================
    //       UNIVERSAL PATH METHODS
    // ==========================================

    /**
     * Gets a String value from XML based on path "section/subsection/tag".
     * Returns null if path doesn't exist.
     */
    public String getValue(String path) {
        Element current = getElementByPath(path, false); // false = don't create
        return (current != null) ? current.getTextContent().trim() : null;
    }

    /**
     * Helper for boolean values. Returns false if tag missing or not "true".
     */
    public boolean getBool(String path) {
        String val = getValue(path);
        return "true".equalsIgnoreCase(val);
    }

    /**
     * Sets a value at specific path. Creates tags if they don't exist.
     */
    public void setValue(String path, String value) {
        Element target = getElementByPath(path, true); // true = create if missing
        if (target != null) {
            target.setTextContent(value);
        }
    }

    /**
     * Helper for setting boolean values.
     */
    public void setBool(String path, boolean value) {
        this.setValue(path, String.valueOf(value));
    }

    private void removeEmptyTextNodes(Document doc) throws Exception {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        // find all text nodes that are empty or contain only whitespace
        XPathExpression expr = xpath.compile("//text()[normalize-space(.) = '']");
        NodeList emptyNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < emptyNodes.getLength(); i++) {
            Node node = emptyNodes.item(i);
            node.getParentNode().removeChild(node);
        }
    }

    /**
     * Saves the current state of Document back to the XML file.
     */
    public void save() throws Exception {
        removeEmptyTextNodes(doc);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // Pretty print settings
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        // Omit XML declaration if you want (optional)
        // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new FileOutputStream(file));
        transformer.transform(source, result);
    }

    // ==========================================
    //           INTERNAL HELPERS
    // ==========================================

    /**
     * Traverses the XML tree based on path.
     * @param path String like "logging/debug"
     * @param createIfMissing If true, creates missing elements along the path.
     */
    private Element getElementByPath(String path, boolean createIfMissing) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        String[] tags = path.split(ConfigConstants.CONFIG_REQUEST_SEPARATOR);
        Element current = root;

        for (String tagName : tags) {
            Element child = getDirectChild(current, tagName);

            if (child == null) {
                if (createIfMissing) {
                    child = doc.createElement(tagName);
                    current.appendChild(child);
                } else {
                    return null; // Path doesn't exist and we are not creating it
                }
            }
            current = child;
        }
        return current;
    }

    /**
     * Finds a direct child element by tag name (standard DOM is clumsy at this).
     */
    private Element getDirectChild(Element parent, String tagName) {
        NodeList list = parent.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(tagName)) {
                return (Element) node;
            }
        }
        return null;
    }
}