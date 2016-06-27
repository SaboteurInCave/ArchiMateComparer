package de.rostock;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static de.rostock.Model.archimate;
import static de.rostock.Model.xsi;

public class ModelDiff {
    private Model oldModel, newModel;
    private Document oldDoc, newDoc;
    private String path;
    private HashMap<String, String> idMapping;

    private final XPathFactory xFactory = XPathFactory.instance();
    private final List<Namespace> namespaces = Arrays.asList(xsi, archimate);

    public ModelDiff(Model oldModel, Model newModel, String path) {
        this.oldModel = oldModel;
        this.newModel = newModel;
        this.path = path;

        idMapping = new HashMap<>();
    }

    public void diff() {
        List<ArchimateView> newViews = newModel.getViews();
        List<ArchimateView> oldViews = oldModel.getViews();

        oldDoc = initDoc(oldModel.getRoot());
        newDoc = initDoc(newModel.getRoot());

        newViews.forEach(newView -> oldViews.stream().filter(view -> Objects.equals(newView.getName(), view.getName())).findFirst().map(oldView -> {
            Graph added = new Graph(newView.getGraph());
            Graph removed = new Graph(oldView.getGraph());

            normalize(added, oldView.getGraph());
            normalize(removed, newView.getGraph());

            getTextDiff(newView.getName(), added, removed);
            getFileDiff(newView.getName(), added, removed);

            return oldView;
        }).orElseGet(() -> {
            System.out.println(newView.getName() + " is new for old model!");
            return null;
        }));

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            File result = new File(path);
            outputter.output(oldDoc, new FileOutputStream(result));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Normalization of new view graph by removing identical content from old view
     */
    private void normalize(Graph newGraph, Graph oldGraph) {
        Set<GraphElement> oldStateVertexSet = oldGraph.getVertexSet();

        oldStateVertexSet.forEach(vertex -> {
            ArrayList<GraphConnection> connections = oldGraph.getConnections(vertex);

            if (newGraph.containVertex(vertex)) {
                ArrayList<GraphConnection> newConnections = newGraph.getConnections(vertex);
                newConnections.removeAll(connections);

                if (newConnections.isEmpty()) {
                    newGraph.removeVertex(vertex);
                }
            }
        });
    }

    private Document initDoc(Element root) {
        Element copyRoot = root.clone();
        return new Document(copyRoot);
    }

    private Element getChildById(final String viewName, final String id, final Document doc) {
        String queryString = "//folder[@type='diagrams']/element[@name='" + viewName + "']/descendant::child[@id='" + id + "']";

        XPathExpression<Element> query = xFactory.compile(queryString, Filters.element(), null, namespaces);
        List<Element> result = query.evaluate(doc);

        if (!result.isEmpty()) {
            return result.get(0); // we have only one element in view with id
        } else {
            return null;
        }
    }

    private Element getChildByArchimateElement(final String viewName, final String archimateElement, final Document doc) {
        String queryString = "//folder[@type='diagrams']/element[@name='" + viewName + "']/descendant::child[@archimateElement='" + archimateElement + "']";

        XPathExpression<Element> query = xFactory.compile(queryString, Filters.element(), null, namespaces);
        return query.evaluateFirst(doc);
    }

    private Element getSourceConnectionById(final String viewName, final String relationId, final Document doc) {
        String queryString = "//folder[@type='diagrams']/element[@name='" + viewName + "']/descendant::sourceConnection[@relationship='" + relationId + "']";

        XPathExpression<Element> query = xFactory.compile(queryString, Filters.element(), null, namespaces);
        List<Element> result = query.evaluate(doc);

        if (!result.isEmpty()) {
            return result.get(0); // we have only one sourceConnection in view with relationId
        } else {
            return null;
        }
    }

    private Element getElementById(final String elementId, final Document doc) {
        String queryString = "//element[@id='" + elementId + "']";
        XPathExpression<Element> query = xFactory.compile(queryString, Filters.element(), null, namespaces);

        return query.evaluateFirst(doc);
    }

    private Element searchElement(final String name, final String type, final Document doc) {
        String queryString = "//element[@name='" + name + "' and @xsi:type='" + type + "']";

        XPathExpression<Element> query = xFactory.compile(queryString, Filters.element(), null, namespaces);

        return query.evaluateFirst(doc);
    }

    private Element searchRelation(final String source, final String target, final Document doc) {
        String queryString = "//element[@source='" + source + "' and @target='" + target + "']";

        XPathExpression<Element> query = xFactory.compile(queryString, Filters.element(), null, namespaces);

        return query.evaluateFirst(doc);
    }

    private void getTextDiff(final String name, final Graph added, final Graph removed) {
        System.out.println("View: " + name);
        System.out.println("===ADDED===");
        System.out.println(added);

        System.out.println("===REMOVED===");
        System.out.println(removed);
    }

    private List<String> getOriginalRelationEndPoints(final List<GraphElement> endPoints) {
        ArrayList<String> id = new ArrayList<>();

        for (GraphElement endPoint : endPoints) {
            Element element = searchElement(endPoint.getName(), endPoint.getType(), oldDoc);

            if (element != null) {
                id.add(element.getAttributeValue("id"));
            } else {
                System.err.println("Element with name " + endPoint.getName() + ", type " + endPoint.getType() + " doesn't exist!");
            }
        }

        return id;
    }

    private Element addNewRelationship(ArrayList<String> idPoints, final String relationType) {
        // first - source, second - target
        String relationshipId = UUID.randomUUID().toString();

        System.out.println("addNewRelationship: " + idPoints);

        ArchimateXMLElement xmlElement = new ArchimateXMLElement(Arrays.asList(
                new Attribute("id", relationshipId),
                new Attribute("type", relationType, xsi),
                new Attribute("source", idPoints.get(0)),
                new Attribute("target", idPoints.get(1))
        ));

        Element element = xmlElement.createElement(ArchimateElementType.relation);

        createElement(element, "relations", oldDoc);

        return element;
    }

    private void createElement(final Element newElement, final String folderType, final Document doc) {
        doc.getRootElement()
                .getChildren("folder")
                .stream()
                .filter(folder -> folder.getAttributeValue("type").equals(folderType))
                .findFirst()
                .ifPresent(folder -> folder.addContent(newElement));
    }

    public void getFileDiff(final String viewName, final Graph added, final Graph removed) {
        removed.getGraph().forEach((element, connections) -> {
            if (!added.containVertex(element)) {
                // element was removed completely

                Element removedElement = getChildById(viewName, element.getElementId(), oldDoc);

                if (removedElement != null) {
                    removedElement.setAttribute("fillColor", "#ff0000");
                }
            }

            // colorize edges
            connections.forEach(connection -> colorizeRemovedEdge(viewName, element, connection));
        });

        // at first - just add vertex (elements) to element map and view
        added.getGraph().forEach((element, connections) -> {

            Element oldElement = searchElement(element.getName(), element.getType(), oldDoc);

            if (!removed.containVertex(element) && oldElement == null) {
                // new element found
                addElement(viewName, added, removed, element);
            } else {
                System.out.println("Just need to add " + element + " to view");

                Element newChild = getChildById(viewName, element.getElementId(), newDoc);

                if (newChild != null) {

                    String oldId = oldElement.getAttributeValue("id");
                    boolean isOldElementInView = getChildByArchimateElement(viewName, oldId, oldDoc) == null;

                    addNewElementToParent(viewName, newChild.clone(), oldId, newChild.getParentElement(), isOldElementInView);
                } else {
                    System.err.println("Can't find " + element.getElementId());
                }

            }
        });

        // secondly, add all new connections and color them

        added.getGraph().forEach((element, connections) -> {
            connections.forEach(connection -> {
                String relationId = connection.getEdgeId();

                Element relationshipElement;

                // save relation element
                ArchimateXMLElement relation = newModel.getElementById(relationId, "relations");

                if (relation != null) {

                    Element sourceElement = getElementById(relation.getSource(), newDoc);
                    Element targetElement = getElementById(relation.getTarget(), newDoc);

                    GraphElement source = new GraphElement( sourceElement.getAttributeValue("name"),
                                                            sourceElement.getAttributeValue("type", xsi),
                                                            sourceElement.getAttributeValue("id"));


                    GraphElement target = new GraphElement( targetElement.getAttributeValue("name"),
                            targetElement.getAttributeValue("type", xsi),
                            targetElement.getAttributeValue("id"));


                    ArrayList<GraphElement> endPoints = new ArrayList<>(Arrays.asList(source, target));
                    ArrayList<String> idPoints = new ArrayList<String>(getOriginalRelationEndPoints(endPoints));

                    Element availableRelationship = searchRelation(idPoints.get(0), idPoints.get(1), oldDoc);

                    relationshipElement = availableRelationship == null ? addNewRelationship(idPoints, relation.getType()) : availableRelationship;

                    // working with relation

                    // find child by archimateElement
                    Element sourceChild = getChildByArchimateElement(viewName, relationshipElement.getAttributeValue("source"), oldDoc);
                    Element targetChild = getChildByArchimateElement(viewName, relationshipElement.getAttributeValue("target"), oldDoc);

                    // add ConnectionSource
                    String connectionId = UUID.randomUUID().toString();

                    Element connectionElement = new Element("sourceConnection");

                    connectionElement.setAttribute("type", "archimate:Connection", xsi);
                    connectionElement.setAttribute("id", connectionId);
                    connectionElement.setAttribute("source", sourceChild.getAttributeValue("id"));
                    connectionElement.setAttribute("target", targetChild.getAttributeValue("id"));
                    connectionElement.setAttribute("relationship", relationshipElement.getAttributeValue("id"));

                    connectionElement.setAttribute("lineWidth", "2");
                    connectionElement.setAttribute("lineColor", "#00ff00");

                    sourceChild.addContent(connectionElement);

                    // update targetConnection of target
                    if (targetChild.getAttributeValue("targetConnections") != null) {
                        targetChild.setAttribute("targetConnections", targetChild.getAttributeValue("targetConnections") + " " + connectionId);
                    } else {
                        targetChild.setAttribute("targetConnections", connectionId);
                    }

                } else {
                    System.err.println("WARNING: relation with id " + relationId + " is not exists!");
                }
            });
        });
    }

    private void addElement(final String viewName, final Graph added, final Graph removed, final GraphElement element) {
        String addedElementId = added.getElementId(element);

        if (addedElementId != null) {
            Element addedElement = getChildById(viewName, addedElementId, newDoc);

            if (addedElement != null) {

                Element parent = addedElement.getParentElement();
                addedElement = addedElement.clone();

                String id = UUID.randomUUID().toString();
                String folderId = UUID.randomUUID().toString();

                // save xml element into file object
                String originalFolder = newModel.getFolderById(addedElement.getAttributeValue("archimateElement"));
                ArchimateXMLElement folderElement = newModel.getElementById(addedElement.getAttributeValue("archimateElement"), originalFolder);

                folderElement.setId(folderId);
                addedElement.setAttribute("archimateElement", folderId);

                oldModel.saveArchElement(folderElement, originalFolder);

                createElement(folderElement.createElement(ArchimateElementType.element), originalFolder, oldDoc);

                addNewElementToParent(viewName, addedElement, id, parent);
            }
        }
    }

    private void addNewElementToParent(final String viewName, Element addedElement, final String id, final Element parent) {
        addNewElementToParent(viewName, addedElement, id, parent, true);
    }

    private void addNewElementToParent(final String viewName, Element addedElement, final String id, final Element parent, final boolean isNew) {

        // remove all connections, because they will added in next iteration
        addedElement.removeChildren("child");
        addedElement.removeChildren("sourceConnection");
        addedElement.removeAttribute("targetConnections");

        if (isNew) {
            addedElement.setAttribute("id", id);
            addedElement.setAttribute("fillColor", "#00ff00");

            Element parentElement = null;

            String parentType = parent.getAttributeValue("type", xsi);
            String parentName = parent.getAttributeValue("name");

            switch (parentType) {
                case "archimate:ArchimateDiagramModel":
                    parentElement = xFactory.compile("//*[@name='" + parentName + "' and @xsi:type='" + parentType + "']", Filters.element(), null, namespaces)
                            .evaluateFirst(oldDoc);
                    break;
                case "archimate:Group":
                    String query = "//folder[@type='diagrams']/element[@name='" + viewName + "']/child[@name='" + parentName + "' and @xsi:type='" + parentType + "']";
                    parentElement = xFactory.compile(query, Filters.element(), null, namespaces).evaluateFirst(oldDoc);
                    break;
                case "archimate:DiagramObject":
                    Element parentObjectElementNew = getElementById(parent.getAttributeValue("archimateElement"), newDoc);

                    Element parentObjectElementOld = searchElement(parentObjectElementNew.getAttributeValue("name"), parentObjectElementNew.getAttributeValue("type", xsi), oldDoc);

                    if (parentObjectElementOld != null) {
                        parentElement = getChildByArchimateElement(viewName, parentObjectElementOld.getAttributeValue("id"), oldDoc);
                    } else {
                        System.err.println("WARNING: parentObjectElementOld is null!");
                    }
                    break;
            }

            if (parentElement != null) {
                parentElement.addContent(addedElement);

                if (parentElement.getChild("bounds") != null && parent.getChild("bounds") != null) {
                    parentElement.removeChild("bounds");
                    parentElement.addContent(parent.getChild("bounds").clone());
                }

            } else {
                System.err.println("WARNING: " + parentType + ", " + parentName + " are not exist!");
            }
        }
    }

    private void colorizeRemovedEdge(final String viewName, final GraphElement vertex, final GraphConnection edge) {
        String query = "//folder[@type='diagrams']/element[@name='" + viewName + "']/descendant::sourceConnection[@relationship='" + edge.getEdgeId() + "']";
        Element element = xFactory.compile(query, Filters.element(), null, namespaces).evaluateFirst(oldDoc); // only one relation is allowed in current view!

        if (element != null) {
            element.setAttribute("lineWidth", "2");
            element.setAttribute("lineColor", "#ff0000");
        }
    }
}
