package de.rostock;

import org.jdom2.*;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

class Model {
    private String path;
    private Element root = null;

    private HashMap<String, ArrayList<ArchimateXMLElement>> folders;
    private HashMap<String, String> idFolder;

    // namespaces

    final private Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    final private Namespace archimate = Namespace.getNamespace("archimate", "http://www.archimatetool.com/archimate");

    final private String groupType = "archimate:Group";
    final private String objectType = "archimate:DiagramObject";
    final private String referenceType = "archimate:DiagramModelReference";

    Model(String path) {
        this.path = path;
        folders = new HashMap<>();
        idFolder = new HashMap<>();

        parseModel();
    }

    private String getFolderById(final String objectId) {
        if (idFolder.containsKey(objectId)) {
            return idFolder.get(objectId);
        } else {
            throw new NoSuchElementException(objectId + " isn't in id storage!");
        }
    }

    private void iterateElements(Element folder, final String originalFolder) {
        for (Element element : folder.getChildren()) {
            final String name = element.getName();

            if (name.equals("folder")) {
                iterateElements(element, originalFolder);
            } else if (name.equals("element")) {
                if (!folders.containsKey(originalFolder)) {
                    folders.put(originalFolder, new ArrayList<>());
                }

                ArchimateXMLElement newElement = new ArchimateXMLElement(element.getAttributes());
                folders.get(originalFolder).add(newElement);
                idFolder.put(newElement.getId(), originalFolder);
            }
        }
    }

    private void parseElements(List<Element> children, ArchimateView archimateView) {
        for (Element child : children) {
            String type = child.getAttributeValue("type", xsi);

            switch (type) {
                case groupType:
                    parseGroup(child, archimateView);
                    break;
                case objectType:
                    parseObject(child, archimateView);
                    break;
                case referenceType:
                    break;
                default:
                    throw new NoSuchElementException(type + " isn't allowed!");
            }
        }
    }

    private void parseGroup(Element group, ArchimateView view) {
        String id = group.getAttributeValue("id");
        String name = group.getAttributeValue("name");
        String fillColor = group.getAttributeValue("fillColor");

        Element bounds = group.getChild("bounds");
        ObjectBounds objectBounds = new ObjectBounds(
                bounds.getAttributeValue("x"),
                bounds.getAttributeValue("y"),
                bounds.getAttributeValue("width"),
                bounds.getAttributeValue("height")
        );

        ArchimateGroup viewGroup = new ArchimateGroup(id, objectBounds, null, name, fillColor);
        view.addGroup(viewGroup);

        List<Element> children = group.getChildren("child");

        if (children.size() > 0) {
            parseElements(children, view);
        }
    }

    private void parseConnection(Element connection, ArchimateObject item, ArchimateView view) {
        String id = connection.getAttributeValue("id");
        String source = connection.getAttributeValue("source");
        String target = connection.getAttributeValue("target");
        String relationship = connection.getAttributeValue("relationship");

        item.addSourceConnection(id);

        ArchimateConnection archimateConnection = new ArchimateConnection(id, source, target, relationship);

        view.addConnection(archimateConnection);
    }

    private void parseObject(Element object, ArchimateView view) {
        String id = object.getAttributeValue("id");
        String textAlignment = object.getAttributeValue("textAlignment");
        String archimateElement = object.getAttributeValue("archimateElement");

        ArrayList<String> targetConnections = null;

        if (object.getAttribute("targetConnections") != null) {
            targetConnections = new ArrayList<>(Arrays.asList(object.getAttributeValue("targetConnections").split(" ")));
        }

        String folder = getFolderById(archimateElement);

        /*
        // For name searching in future
        String objectName = folders.get(folder).stream()
                .filter((ArchimateXMLElement element) -> Objects.equals(element.getId(), archimateElement))
                .findFirst()
                .get()
                .getName();
        */

        Element bounds = object.getChild("bounds");
        ObjectBounds objectBounds = new ObjectBounds(
                bounds.getAttributeValue("x"),
                bounds.getAttributeValue("y"),
                bounds.getAttributeValue("width"),
                bounds.getAttributeValue("height")
        );

        Element parent = object.getParentElement();

        ArchimateObject archimateObject = new ArchimateObject(
                id,
                objectBounds,
                parent.getAttributeValue("id"),
                textAlignment,
                archimateElement
        );

        List<Element> children = object.getChildren("sourceConnection");

        for (Element child : children) {
            parseConnection(child, archimateObject, view);
        }

        view.addObject(archimateObject);
    }

    private void parseView(Element diagram) {
        ArchimateView archimateView = new ArchimateView(diagram.getAttributeValue("name"));
        List<Element> children = diagram.getChildren("child");

        parseElements(children, archimateView);

        System.out.println(archimateView);
    }

    void parseModel() {
        SAXBuilder builder = new SAXBuilder();
        try {
            Document document = builder.build(new File(path));


            root = document.getRootElement();

            List<Element> fileFolders = root.getChildren("folder");

            // try to get elements of model
            for (Element folder : fileFolders) {
                iterateElements(folder, folder.getAttributeValue("name"));
            }

            // model parsing

            XPathFactory xFactory = XPathFactory.instance();
            List<Namespace> namespaces = Arrays.asList(xsi, archimate);

            XPathExpression<Element> query = xFactory.compile(
                    "//element[@xsi:type='archimate:ArchimateDiagramModel']",
                    Filters.element(),
                    null,
                    namespaces);


            List<Element> views = query.evaluate(document);

            views.forEach(this::parseView);

        } catch (JDOMException | IOException  | NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    File createXmlDocument(Element rootElement, String outputPath) {
        Document xmlDoc = new Document();
        xmlDoc.setRootElement(rootElement);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            File result = new File(outputPath);
            outputter.output(xmlDoc, new FileOutputStream(result));
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {

        String result = "";

        for (String folder : folders.keySet()) {
            result += String.format("%s:", folder) + '\n';

            for (ArchimateXMLElement element : folders.get(folder)) {
                result += (element + ",");
            }
        }

        return  result;
    }

    public Element getRoot() {
        return root;
    }
}
