package de.rostock;

public class GraphConnection {
    private GraphElement vertex;
    private String edge;

    public GraphConnection(GraphElement vertex, String edge) {
        this.vertex = vertex;
        this.edge = edge;
    }

    public GraphElement getVertex() {
        return vertex;
    }

    public String getEdge() {
        return edge;
    }

    @Override
    public String toString() {
        return "vertex: " + vertex + " by " + edge;
    }
}
