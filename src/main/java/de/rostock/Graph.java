package de.rostock;

import java.util.ArrayList;
import java.util.HashMap;

public class Graph {
    private HashMap<GraphElement, ArrayList<GraphConnection>> graph;

    public Graph() {
        graph = new HashMap<>();
    }

    public void addEdge(GraphElement from, GraphElement to, String relationType) {
        GraphConnection newConnection = new GraphConnection(to, relationType);

        if (!graph.containsKey(from)) {
            graph.put(from, new ArrayList<>());
        }

        graph.get(from).add(newConnection);
    }

    private String graphToString() {
        String result = "";

        for (GraphElement element : graph.keySet()) {
            result += element.toString() + "\n";

            for (GraphConnection connection : graph.get(element)) {
                result += "    " + connection.toString() + '\n';
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return graphToString();
    }
}
