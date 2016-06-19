package de.rostock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;

public class Graph {
    private HashMap<GraphElement, ArrayList<GraphConnection>> graph;

    public Graph() {
        graph = new HashMap<>();
    }

    public Graph(Graph newGraph) {
        graph = new HashMap<>();

        HashMap<GraphElement, ArrayList<GraphConnection>> graphMap = newGraph.getGraph();

        graphMap.forEach((vertex, connections) -> {

            ArrayList<GraphConnection> newConnection = new ArrayList<>();

            connections.forEach(graphConnection -> {
                newConnection.add(new GraphConnection(graphConnection));
            });

            this.graph.put(vertex, newConnection);
        });
    }

    public HashMap<GraphElement, ArrayList<GraphConnection>> getGraph() {
        return graph;
    }

    public void addEdge(GraphElement from, GraphElement to, String relationType) {
        GraphConnection newConnection = new GraphConnection(to, relationType);

        if (!graph.containsKey(from)) {
            graph.put(from, new ArrayList<>());
        }

        graph.get(from).add(newConnection);
    }

    public void addEdge(GraphElement vertex, ArrayList<GraphConnection> connections) {
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
