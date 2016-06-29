package de.rostock.model;

import de.rostock.archimate.*;
import de.rostock.graph.GraphElement;
import org.jdom2.*;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Model {
    private String path;
    private Element root = null;

    private HashMap<String, ArrayList<ArchimateXMLElement>> folders;
    private HashMap<String, String> idFolder;
    private List<ArchimateView> views;

    // namespaces

    final static public Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    final static public Namespace archimate = Namespace.getNamespace("archimate", "http://www.archimatetool.com/archimate");
    private final String defaultRelation = "rostock:Relation";

    final private String groupType = "archimate:Group";
    final private String objectType = "archimate:DiagramObject";
    final private String referenceType = "archimate:DiagramModelReference";

    public Model(String path) {
        this.path = path;
        folders = new HashMap<>();
        idFolder = new HashMap<>();
        views = new ArrayList<>();

        parseModel();
    }

    String getFolderById(final String objectId) {
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

    private void addArchElement(final ArchimateXMLElement element, final String folder) {
        if (!folders.containsKey(folder)) {
            folders.put(folder, new ArrayList<>());
        }

        folders.get(folder).add(element);
        idFolder.put(element.getId(), folder);
    }

    void saveArchElement(final ArchimateXMLElement addedElement, final String folder) {
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
                    System.err.println(type + " isn't supported!");
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

        String relationValue = connection.getAttributeValue("relationship");

        String relationship = relationValue == null ? defaultRelation : relationValue;

        item.addSourceConnection(id);

        ArchimateConnection archimateConnection = new ArchimateConnection(id, source, target, relationship);

        view.addConnection(archimateConnection);
    }

    private void parseObject(Element object, ArchimateView view) {
        String id = object.getAttributeValue("id");
        String textAlignment = object.getAttributeValue("textAlignment");
        String archimateElement = object.getAttributeValue("archimateElement");

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

        if (children.size() > 0) {
            for (Element child : children) {
                parseConnection(child, archimateObject, view);
            }
        } else {
            // for case, when model doesn't have source connection, we can emulate relationship parent - child

            if (!parent.getAttributeValue("type", xsi).equals("archimate:ArchimateDiagramModel")) {

                Element fakeConnection = new Element("relationConnection");

                fakeConnection.setAttribute("id", UUID.randomUUID().toString());
                fakeConnection.setAttribute("source", parent.getAttributeValue("id"));
                fakeConnection.setAttribute("target", object.getAttributeValue("id"));

                parseConnection(fakeConnection, archimateObject, view);
            }
        }

        children = object.getChildren("child");

        // get children
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

    }

    ArchimateXMLElement getElementById(final String elementId, final String folder) {
        return folders.get(folder)
                    .stream()
                    .filter((ArchimateXMLElement element) -> Objects.equals(element.getId(), elementId))
                    .findFirst().get();
    }

    @org.jetbrains.annotations.Nullable
    private GraphElement getGraphElement(String elementId, String objectId) {
        String folder = getFolderById(elementId);

        ArchimateXMLElement value = getElementById(elementId, folder);

        if (value != null) {
            return new GraphElement(value.getName(), value.getType(), objectId);
        } else {
            return null;
        }
    }

    private void makeGraph(ArchimateView view) {
        HashMap<String, ArchimateConnection> connections = view.getConnections();

        connections.forEach((id, connection) -> {

            ArchimateObject fromObject = view.getObject(connection.getSource());
            ArchimateObject toObject = view.getObject(connection.getTarget());

            GraphElement from = getGraphElement(fromObject.getArchimateElement(), fromObject.getId());
            GraphElement to = getGraphElement(toObject.getArchimateElement(), toObject.getId());

            if (!connection.getRelationship().equals(defaultRelation)) {

                ArchimateXMLElement value =
                        folders.get("relations")
                                .stream()
                                .filter((ArchimateXMLElement element) -> Objects.equals(element.getId(), connection.getRelationship()))
                                .findFirst().get();

                view.getGraph().addEdge(from, to, value.getType(), value.getId());
            } else {
                view.getGraph().addEdge(from ,to, defaultRelation, "-1");
            }
        });
    }

    private void parseModel() {
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

    Element getRoot() {
        return root;
    }

    List<ArchimateView> getViews() {
        return views;
    }
}
