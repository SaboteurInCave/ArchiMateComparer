package de.rostock.model;

import de.rostock.archimate.ArchimateElementType;
import de.rostock.archimate.ArchimateView;
import de.rostock.archimate.ArchimateXMLElement;
import de.rostock.graph.Graph;
import de.rostock.graph.GraphConnection;
import de.rostock.graph.GraphElement;
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

import static de.rostock.model.Model.archimate;
import static de.rostock.model.Model.xsi;

public class ModelDiff {
    private Model oldModel, newModel;
    private Document oldDoc, newDoc;
    private String path;

    private final XPathFactory xFactory = XPathFactory.instance();
    private final List<Namespace> namespaces = Arrays.asList(xsi, archimate);

    private HashMap<String, String> viewMapping;


    public ModelDiff(Model oldModel, Model newModel, String path, final HashMap<String, String> viewMapping) {
        this.oldModel = oldModel;
        this.newModel = newModel;
        this.path = path;
        this.viewMapping = viewMapping;
    }

    /**
     * Get difference between models in text and graphical modes
     */
    public void diff() {
        List<ArchimateView> newViews = newModel.getViews();
        List<ArchimateView> oldViews = oldModel.getViews();

        oldDoc = initDoc(oldModel.getRoot());
        newDoc = initDoc(newModel.getRoot());


        // check views by names
        newViews.forEach(newView -> oldViews.stream()
                .filter(view -> Objects.equals(newView.getName(), view.getName())).findFirst()
                .map(oldView -> {
                    diffViews(newView, oldView);
                    return oldView;
                }).orElseGet(() -> {

                    // check if newView in viewMapping from options
                    if (viewMapping.containsKey(newView.getName())) {
                        System.out.println(newView.getName() + " is in viewMapping!");

                        String oldName = viewMapping.get(newView.getName());

                        oldViews.stream()
                                .filter(archimateView -> archimateView.getName().equals(oldName))
                                .findFirst()
                                .ifPresent(oldView -> diffViews(newView, oldView));


                        // need to rename view as newView.getName()

                        Element diagram = searchElement(oldName, "archimate:ArchimateDiagramModel", oldDoc);
                        diagram.setAttribute("name", String.format("%s(%s)", newView.getName(), diagram.getAttributeValue("name")));

                        return null;
                    } else {

                        // completely new model!
                        System.out.println(newView.getName() + " is new for old model!");

                        // create home element

                        Element element = searchElement(newView.getName(), "archimate:ArchimateDiagramModel", newDoc);
                        Element newDiagramElement = element.clone();

                        newDiagramElement.removeChildren("child");
                        newDiagramElement.removeChildren("bounds");
                        newDiagramElement.removeChildren("sourceConnection");

                        newDiagramElement.setAttribute("id", UUID.randomUUID().toString());

                        oldDoc.getRootElement().getChildren("folder").stream()
                                .filter(folder -> folder.getAttributeValue("type").equals("diagrams"))
                                .findFirst().ifPresent(diagramFolder -> diagramFolder.addContent(newDiagramElement));

                        diffViews(newView, null);

                        return null;
                    }
                }
            )
        );

        // save diff to xml file
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            File result = new File(path);
            outputter.output(oldDoc, new FileOutputStream(result));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** get difference in text and graphical file mode from given views
     * @param newView view from new file
     * @param oldView iew from old file. Can be <i>nullable</i>
     */
    private void diffViews(ArchimateView newView, ArchimateView oldView) {
        if (newView != null) {
            Graph added = new Graph(newView.getGraph());
            Graph removed = oldView != null ? new Graph(oldView.getGraph()) : new Graph();

            added.setElementOrder(getElementOrder(newView.getName(), newDoc));

            if (oldView != null) {
                normalize(added, oldView.getGraph());
            }

            normalize(removed, newView.getGraph());

            getTextDiff(newView.getName(), added, removed);
            getFileDiff(newView.getName(), added, removed);
        } else {
            System.err.println("New view is null!");
        }
    }

    /**
     * Normalization of new view graph by removing identical content from old view
     * @param newGraph graph of elements from new view
     * @param oldGraph graph of elements from old view
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

    /**
     * Copy root of Document
     * @param root root element of document
     * @return copy of Document
     */
    private Document initDoc(Element root) {
        Element copyRoot = root.clone();
        return new Document(copyRoot);
    }

    /** Search child element in doc's view by <i>id</i> attribute
     * @param viewName name of view
     * @param id <i>id</i> of element
     * @param doc given doc
     * @return element or <b>null</b>
     */
    private Element getChildById(final String viewName, final String id, final Document doc) {
        String queryString = "//folder[@type='diagrams']/element[@name='" + viewName + "']/descendant::child[@id='" + id + "']";

        XPathExpression<Element> query = xFactory.compile(queryString, Filters.element(), null, namespaces);
        return  query.evaluateFirst(doc);
    }

    /** Search child element in doc's view by <i>archimateElement</i> attribute
     * @param viewName <i>name</i> of view
     * @param archimateElement <i>archimateElement</i> value of element
     * @param doc given doc
     * @return element or <b>null</b>
     */
    private Element getChildByArchimateElement(final String viewName, final String archimateElement, final Document doc) {
        String queryString = "//folder[@type='diagrams']/element[@name='" + viewName + "']/descendant::child[@archimateElement='" + archimateElement + "']";

        XPathExpression<Element> query = xFactory.compile(queryString, Filters.element(), null, namespaces);
        return query.evaluateFirst(doc);
    }

    /** Search element in doc by <i>id</i> attribute
     * @param elementId <i>id</i> attribute of element
     * @param doc given doc
     * @return element or <b>null</b>
     */
    private Element getElementById(final String elementId, final Document doc) {
        String queryString = "//element[@id='" + elementId + "']";
        XPathExpression<Element> query = xFactory.compile(queryString, Filters.element(), null, namespaces);

        return query.evaluateFirst(doc);
    }

    /** Search element by <i>name</i> and <i>type</i> attributes
     * @param name <i>name</i> attribute of element
     * @param type <i>type</i> attribute of element (need to use without xsi namespace, because it has already included)
     * @param doc given doc
     * @return element or <b>null</b>
     */
    private Element searchElement(final String name, final String type, final Document doc) {
        String queryString = "//element[@name='" + name + "' and @xsi:type='" + type + "']";

        XPathExpression<Element> query = xFactory.compile(queryString, Filters.element(), null, namespaces);

        return query.evaluateFirst(doc);
    }

    /** Search relation by <i>source</i> and <i>target</i> attributes
     * @param source <i>source</i> attribute
     * @param target <i>target</i> attribute
     * @param doc given doc
     * @return element or <b>null</b>
     */
    private Element searchRelation(final String source, final String target, final Document doc) {
        String queryString = "//element[@source='" + source + "' and @target='" + target + "']";

        XPathExpression<Element> query = xFactory.compile(queryString, Filters.element(), null, namespaces);

        return query.evaluateFirst(doc);
    }

    /** Get text difference between graphs
     * @param name view name
     * @param added graph from new view
     * @param removed graph from old view
     */
    private void getTextDiff(final String name, final Graph added, final Graph removed) {
        System.out.println("View: " + name);
        System.out.println("===ADDED===");
        System.out.println(added);

        System.out.println("===REMOVED===");
        System.out.println(removed);
    }

    /** Get source and target ids of old document (translation of ids from new doc to ids of ids doc)
     * @param endPoints arraylist of source and target ids of new doc
     * @return array list, which contains source and target ids of old doc
     */
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

    /** Create relationship element and return it
     * @param idPoints arraylist of source and target connections
     * @param relationType type of relationship
     * @return relationship element
     */
    private Element addNewRelationship(ArrayList<String> idPoints, final String relationType) {
        // first - source, second - target
        String relationshipId = UUID.randomUUID().toString();

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

    /** Save element to doc
     * @param newElement created element
     * @param folderType type of folder in document
     * @param doc document
     */
    private void createElement(final Element newElement, final String folderType, final Document doc) {
        doc.getRootElement()
                .getChildren("folder")
                .stream()
                .filter(folder -> folder.getAttributeValue("type").equals(folderType))
                .findFirst()
                .ifPresent(folder -> folder.addContent(newElement));
    }

    /** Get order of elements for iterating (DFS)
     * @param viewName name of view
     * @param doc document
     * @return arraylist of elements in order
     */
    private ArrayList<GraphElement> getElementOrder(final String viewName, final Document doc) {
        Element rootElement = searchElement(viewName, "archimate:ArchimateDiagramModel", doc);
        ArrayList<GraphElement> order = new ArrayList<>();

        if (rootElement != null) {

            rootElement.getChildren("child").forEach(element -> {

                Stack<Element> stack = new Stack<>();
                Set<Element> used = new HashSet<>();

                stack.add(element);

                while (stack.size() > 0) {
                    Element poppedElement = stack.pop();

                    if (!used.contains(poppedElement)) {
                        used.add(poppedElement);

                        Element archimateElement = getElementById(poppedElement.getAttributeValue("archimateElement"), doc);

                        if (archimateElement != null) {
                            order.add(new GraphElement(archimateElement.getAttributeValue("name"), archimateElement.getAttributeValue("type", xsi), poppedElement.getAttributeValue("id")));
                        } else {
                            System.err.println("Cant find element for " + poppedElement.getAttributes());
                        }
                        poppedElement.getChildren("child").forEach(stack::push);
                    }
                }
            });
        }

        return order;
    }

    /** Alter xml documents due to difference between graphs
     * @param viewName name of view
     * @param added graph of new view
     * @param removed graph of old view
     */
    private void getFileDiff(final String viewName, Graph added, Graph removed) {

        removed.getGraph().forEach((element, connections) -> {
            if (!added.containVertex(element)) {

                // element was removed completely

                Element removedElement = getChildById(getOldDocViewName(viewName), element.getViewChildId(), oldDoc);

                if (removedElement != null) {
                    removedElement.setAttribute("fillColor", "#ff0000");
                }
            }

            // colorize edges
            connections.forEach(connection -> colorizeRemovedEdge(getOldDocViewName(viewName), connection));
        });

        // at first - just add all elements to view
        if (added.getElementOrder().size() != 0) {
            // we have particular order how to iterate vertexes
            added.getElementOrder().forEach(element -> {
                if (added.getGraph().containsKey(element)) {
                    iterateAddedElements(viewName, element, added, removed);
                }
            });
        } else {
            // in case, that is something wrong with element order
            added.getGraph().forEach((element, graphConnections) -> iterateAddedElements(viewName, element, added, removed));
        }

        // secondly, add all new connections and color them
        added.getGraph().forEach((element, connections) -> connections.forEach(connection -> colorizeAddedEdge(viewName, connection)));
    }

    /** routine for adding graph element in model (creating element with unique id if needed and put it in view)
     * @param viewName name of view
     * @param element graph element
     * @param added graph of new view
     * @param removed graph of old view
     */
    private void iterateAddedElements(final String viewName, GraphElement element, Graph added, Graph removed) {
        Element oldElement = searchElement(element.getName(), element.getType(), oldDoc);

        if (!removed.containVertex(element) && oldElement == null) {
            // new element found
            addElement(viewName, added, element);
        } else {

            Element newChild = getChildById(viewName, element.getViewChildId(), newDoc);

            if (newChild != null) {

                String oldId = oldElement.getAttributeValue("id");
                boolean isOldElementInView = getChildByArchimateElement(getOldDocViewName(viewName), oldId, oldDoc) == null;

                addNewElementToParent(viewName, newChild.clone(), oldId, newChild.getParentElement(), isOldElementInView);
            } else {
                System.err.printf("Can't find child element with id %s%n", element.getViewChildId());
            }

        }
    }

    /** Helper function for iterateAddedElements --- actually create element from scratch
     * @param viewName name of view
     * @param added graph of new view
     * @param element graph element
     */
    private void addElement(final String viewName, final Graph added, final GraphElement element) {
        String addedElementId = added.getElementId(element);

        if (addedElementId != null) {
            Element addedElement = getChildById(viewName, addedElementId, newDoc);

            if (addedElement != null) {

                Element parent = addedElement.getParentElement();
                addedElement = addedElement.clone();

                String folderId = UUID.randomUUID().toString();

                // save xml element into file object
                String originalFolder = newModel.getFolderById(addedElement.getAttributeValue("archimateElement"));
                ArchimateXMLElement folderElement = newModel.getElementById(addedElement.getAttributeValue("archimateElement"), originalFolder);

                folderElement.setId(folderId);

                oldModel.saveArchElement(folderElement, originalFolder);
                createElement(folderElement.createElement(ArchimateElementType.element), originalFolder, oldDoc);
                addNewElementToParent(viewName, addedElement, folderId, parent);
            }
        }
    }

    /** Prepare element for adding childs and bounds + put element into view in correct order
     * @param viewName name of view
     * @param addedElement added element
     * @param id <i>ArchimateElement</i> attribute
     * @param parent parent element of addedElement
     */
    private void addNewElementToParent(final String viewName, Element addedElement, final String id, final Element parent) {
        addNewElementToParent(viewName, addedElement, id, parent, true);
    }

    /** get view name from option or default for old document
     * @param viewName name of view
     * @return altered view name
     */
    private String getOldDocViewName(final String viewName) {
        return viewMapping.containsKey(viewName) ? viewMapping.get(viewName) : viewName;
    }

    /** Add element to parent in view
     * @param viewName name of view
     * @param addedElement added Element
     * @param id <i>ArchimateElement</i> attribute
     * @param parent parent element of addedElement in view
     * @param isNew need to put in view?
     */
    private void addNewElementToParent(final String viewName, Element addedElement, final String id, final Element parent, final boolean isNew) {

        // remove all connections, because they will added in next iteration
        addedElement.removeChildren("child");
        addedElement.removeChildren("sourceConnection");
        addedElement.removeAttribute("targetConnections");

        if (isNew) {
            addedElement.setAttribute("id", UUID.randomUUID().toString());
            addedElement.setAttribute("archimateElement", id);
            addedElement.setAttribute("fillColor", "#00ff00");

            Element parentElement = null;

            String parentType = parent.getAttributeValue("type", xsi);
            String parentName = parent.getAttributeValue("name");

            switch (parentType) {
                case "archimate:ArchimateDiagramModel":
                    if (!viewMapping.containsKey(viewName)) {
                        parentElement = xFactory.compile("//*[@name='" + parentName + "' and @xsi:type='" + parentType + "']", Filters.element(), null, namespaces)
                                .evaluateFirst(oldDoc);
                    } else {
                        parentElement = xFactory.compile("//*[@name='" + viewMapping.get(viewName) + "' and @xsi:type='" + parentType + "']", Filters.element(), null, namespaces)
                                .evaluateFirst(oldDoc);
                    }
                    break;
                case "archimate:Group":
                    String query = "//folder[@type='diagrams']/element[@name='" + getOldDocViewName(viewName) + "']/child[@name='" + parentName + "' and @xsi:type='" + parentType + "']";
                    parentElement = xFactory.compile(query, Filters.element(), null, namespaces).evaluateFirst(oldDoc);
                    break;
                case "archimate:DiagramObject":
                    Element parentObjectElementNew = getElementById(parent.getAttributeValue("archimateElement"), newDoc);

                    Element parentObjectElementOld = searchElement(parentObjectElementNew.getAttributeValue("name"), parentObjectElementNew.getAttributeValue("type", xsi), oldDoc);

                    if (parentObjectElementOld != null) {
                        parentElement = getChildByArchimateElement(getOldDocViewName(viewName), parentObjectElementOld.getAttributeValue("id"), oldDoc);
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
                System.out.println(addedElement.getAttributes());
                System.out.println(parent.getAttributes());
                System.out.println("WARNING: " + parentType + ", " + parentName + " are not exist!");
            }
        }
    }

    /** Colorize removed connections in old document
     * @param viewName name of view
     * @param edge graph connection
     */
    private void colorizeRemovedEdge(final String viewName, final GraphConnection edge) {
        String query = "//folder[@type='diagrams']/element[@name='" + viewName + "']/descendant::sourceConnection[@relationship='" + edge.getEdgeId() + "']";
        Element element = xFactory.compile(query, Filters.element(), null, namespaces).evaluateFirst(oldDoc); // only one relation is allowed in current view!

        if (element != null) {
            element.setAttribute("lineWidth", "2");
            element.setAttribute("lineColor", "#ff0000");
        }
    }

    /** Colorize connection edge and put it in correct place
     * @param viewName name of view
     * @param connection graph connection
     */
    private void colorizeAddedEdge(final String viewName, final GraphConnection connection) {
        String relationId = connection.getEdgeId();

        Element relationshipElement;

        // save relation element, if it is not fake relationship

        if (!Objects.equals(relationId, "-1")) {

            ArchimateXMLElement relation = newModel.getElementById(relationId, "relations");

            if (relation != null) {

                Element sourceElement = getElementById(relation.getSource(), newDoc);
                Element targetElement = getElementById(relation.getTarget(), newDoc);

                GraphElement source = new GraphElement(sourceElement.getAttributeValue("name"),
                        sourceElement.getAttributeValue("type", xsi),
                        sourceElement.getAttributeValue("id"));


                GraphElement target = new GraphElement(targetElement.getAttributeValue("name"),
                        targetElement.getAttributeValue("type", xsi),
                        targetElement.getAttributeValue("id"));


                ArrayList<GraphElement> endPoints = new ArrayList<>(Arrays.asList(source, target));
                ArrayList<String> idPoints = new ArrayList<>(getOriginalRelationEndPoints(endPoints));

                Element availableRelationship = searchRelation(idPoints.get(0), idPoints.get(1), oldDoc);

                relationshipElement = availableRelationship == null ? addNewRelationship(idPoints, relation.getType()) : availableRelationship;

                // working with relation

                // find child by archimateElement
                Element sourceChild = getChildByArchimateElement(getOldDocViewName(viewName), relationshipElement.getAttributeValue("source"), oldDoc);
                Element targetChild = getChildByArchimateElement(getOldDocViewName(viewName), relationshipElement.getAttributeValue("target"), oldDoc);

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
        }
    }
}
