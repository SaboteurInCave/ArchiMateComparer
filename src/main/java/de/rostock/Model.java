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
    private List<ArchimateView> views;

    // namespaces

    final static public Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    final static public Namespace archimate = Namespace.getNamespace("archimate", "http://www.archimatetool.com/archimate");

    final private String groupType = "archimate:Group";
    final private String objectType = "archimate:DiagramObject";
    final private String referenceType = "archimate:DiagramModelReference";

    Model(String path) {
        this.path = path;
        folders = new HashMap<>();
        idFolder = new HashMap<>();
        views = new ArrayList<>();

        parseModel();
    }

    public String getFolderById(final String objectId) {
        if (idFolder.containsKey(objectId)) {
            return idFolder.get(objectId);
        } else {
            throw new NoSuchElementException(objectId + " isn't in id storage!");
        }
    }

    private void iterateElements(Element folder, final String originalFolder) {
        folder.getChildren().forEach(element -> {
            final String name = element.getName();

            if (name.equals("folder")) {
                iterateElements(element, originalFolder);
            } else if (name.equals("element")) {
                addArchElement(new ArchimateXMLElement(element.getAttributes()), originalFolder);
            }
        });
    }

    public void addArchElement(final ArchimateXMLElement element, final String folder) {
        if (!folders.containsKey(folder)) {
            folders.put(folder, new ArrayList<>());
        }

        folders.get(folder).add(element);
        idFolder.put(element.getId(), folder);
    }

    public void saveArchElement(final ArchimateXMLElement addedElement, final String folder) {
        root.getChildren("folder")
            .stream()
            .filter(element -> element.getAttributeValue("type").equals(folder))
            .findFirst()
            .ifPresent(folderElement -> {
                Element newElement = new Element("element");
                addedElement.getAttributesKeys().forEach(key -> newElement.setAttribute(key, addedElement.getValue(key)));
                folderElement.setContent(newElement);
            });
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
                    System.err.println(type + " isn't allowed!");
                    //throw new NoSuchElementException(type + " isn't allowed!");
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

        children = object.getChildren("child");

        // get childrens
        if (children.size() > 0) {
            parseElements(children, view);
        }

        view.addObject(archimateObject);
    }

    private void parseView(Element diagram) {

        String name = diagram.getAttributeValue("name");
        String id = diagram.getAttributeValue("id");
        String viewpoint = diagram.getAttribute("viewpoint") != null ? diagram.getAttributeValue("viewpoint") : null;

        ArchimateView archimateView = new ArchimateView(name, id, viewpoint);
        List<Element> children = diagram.getChildren("child");

        parseElements(children, archimateView);

        views.add(archimateView);

        makeGraph(archimateView);

        /*
        System.out.println("View: " + archimateView.getName());

        System.out.println("===");
        */
    }

    public ArchimateXMLElement getElementById(final String elementId, final String folder) {
        return folders.get(folder)
                    .stream()
                    .filter((ArchimateXMLElement element) -> Objects.equals(element.getId(), elementId))
                    .findFirst().get();
    }



    private GraphElement getGraphElement(String elementId, String objectId) {
        String folder = getFolderById(elementId);

        ArchimateXMLElement value = getElementById(elementId, folder);

        if (value != null) {
            return new GraphElement(value.getName(), value.getType(), objectId);
        } else {
            return null;
        }
    }

    void makeGraph(ArchimateView view) {
        HashMap<String, ArchimateConnection> connections = view.getConnections();

        connections.forEach((id, connection) -> {

            ArchimateObject fromObject = view.getObject(connection.getSource());
            ArchimateObject toObject = view.getObject(connection.getTarget());

            GraphElement from = getGraphElement(fromObject.getArchimateElement(), fromObject.getId());
            GraphElement to = getGraphElement(toObject.getArchimateElement(), toObject.getId());

            ArchimateXMLElement value =
                    folders.get("relations")
                            .stream()
                            .filter((ArchimateXMLElement element) -> Objects.equals(element.getId(), connection.getRelationship()))
                            .findFirst().get();

            view.getGraph().addEdge(from, to, value.getType(), value.getId());
        });
    }

    void parseModel() {
        SAXBuilder builder = new SAXBuilder();
        try {
            Document document = builder.build(new File(path));


            root = document.getRootElement();

            List<Element> fileFolders = root.getChildren("folder");

            // try to get elements of model
            for (Element folder : fileFolders) {
                iterateElements(folder, folder.getAttributeValue("type"));
            }

            // model parsing

            XPathFactory xFactory = XPathFactory.instance();
            List<Namespace> namespaces = Arrays.asList(xsi, archimate);

            XPathExpression<Element> query = xFactory.compile(
                    "//element[@xsi:type='archimate:ArchimateDiagramModel']",
                    Filters.element(),
                    null,
                    namespaces);


            List<Element> rawViews = query.evaluate(document);

            rawViews.forEach(this::parseView);
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

    public List<ArchimateView> getViews() {
        return views;
    }
}
