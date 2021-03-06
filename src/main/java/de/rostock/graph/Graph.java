package de.rostock.graph;

import java.util.*;

public class Graph {
    private HashMap<GraphElement, ArrayList<GraphConnection>> graph; // graph representation - doesn't changes at all if diff views
    private HashMap<GraphElement, String> elementIdMap; // elementMapping - if we will add new vertex, we need to add id to elementIdMap

    private ArrayList<GraphElement> elementOrder; // order of graph traveling

    public Graph() {
        graph = new HashMap<>();
        elementIdMap = new HashMap<>();
        elementOrder = new ArrayList<>();
    }

    public Graph(Graph newGraph) {
        graph = new HashMap<>();
        elementIdMap = new HashMap<>();
        elementOrder = new ArrayList<>();


        HashMap<GraphElement, ArrayList<GraphConnection>> graphMap = newGraph.getGraph();

        graphMap.forEach((vertex, connections) -> {

            ArrayList<GraphConnection> newConnection = new ArrayList<>();

            connections.forEach(graphConnection -> newConnection.add(new GraphConnection(graphConnection)));

            addEdge(vertex, newConnection);
        });
    }

    public HashMap<GraphElement, ArrayList<GraphConnection>> getGraph() {
        return graph;
    }

    public ArrayList<GraphElement> getElementOrder() {
        return elementOrder;
    }

    private void addElementToMap(final GraphElement element) {
        if (!elementIdMap.containsKey(element)) {
            elementIdMap.put(element, element.getViewChildId());
        }
    }

    public String getElementId(GraphElement element) {
        if (elementIdMap.containsKey(element)) {
            return elementIdMap.get(element);
        } else {
            return null;
        }
    }

    public void addEdge(GraphElement from, GraphElement to, String relationType, String relationId) {
        GraphConnection newConnection = new GraphConnection(to, relationType, relationId);

        addElementToMap(from);
        addElementToMap(to);

        if (!graph.containsKey(from)) {
            graph.put(from, new ArrayList<>());
        }

        if (!graph.containsKey(to)) {
            graph.put(to, new ArrayList<>());
        }

        graph.get(from).add(newConnection);
    }

    private void addEdge(GraphElement vertex, ArrayList<GraphConnection> connections) {

        addElementToMap(vertex);

        if (!graph.containsKey(vertex)) {
            graph.put(vertex, connections);
        }
    }

    public Set<GraphElement> getVertexSet() {
        return graph.keySet();
    }

    public ArrayList<GraphConnection> getConnections(GraphElement vertex) {
        if (graph.containsKey(vertex)) {
            return graph.get(vertex);
        } else {
            throw new NoSuchElementException();
        }
    }

    public boolean containVertex(GraphElement element) {
        return graph.containsKey(element);
    }

    public void removeVertex(GraphElement element) {
        if (graph.containsKey(element)) {
            graph.remove(element);
        } else {
            throw new NoSuchElementException();
        }
    }

    public void setElementOrder(ArrayList<GraphElement> elementOrder) {
        this.elementOrder = elementOrder;
    }

    private String graphToString() {
        String result = "";

        if (graph != null) {
            for (GraphElement element : graph.keySet()) {
                result += element.toString() + "\n";

                for (GraphConnection connection : graph.get(element)) {
                    result += "    " + connection.toString() + '\n';
                }
            }
        }


        return result;
    }

    @Override
    public String toString() {
        return graphToString();
    }
}
