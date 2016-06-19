package de.rostock;

import java.util.ArrayList;
import java.util.Set;

public class ViewCompare {
    private ArchimateView oldView, newView;
    private Graph added, removed;

    public ViewCompare(ArchimateView oldView, ArchimateView newView) {
        this.oldView = oldView;
        this.newView = newView;

        added = new Graph(newView.getGraph());
        removed = new Graph(oldView.getGraph());
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

    public void compare() {
        normalize(added, oldView.getGraph());
        normalize(removed, newView.getGraph());

        System.out.println("View: " + newView.getName());
        System.out.println("===ADDED===");
        System.out.println(added);

        System.out.println("===REMOVED===");
        System.out.println(removed);
    }
}
