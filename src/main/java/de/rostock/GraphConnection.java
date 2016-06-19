package de.rostock;

public class GraphConnection {
    private GraphElement vertex;
    private String edge;

    public GraphConnection(GraphElement vertex, String edge) {
        this.vertex = vertex;
        this.edge = edge;
    }

    public GraphConnection(GraphConnection connection) {
        this.vertex = connection.getVertex();
        this.edge = connection.getEdge();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphConnection that = (GraphConnection) o;

        if (vertex != null ? !vertex.equals(that.vertex) : that.vertex != null) return false;
        return edge != null ? edge.equals(that.edge) : that.edge == null;

    }

    @Override
    public int hashCode() {
        int result = vertex != null ? vertex.hashCode() : 0;
        result = 31 * result + (edge != null ? edge.hashCode() : 0);
        return result;
    }
}
