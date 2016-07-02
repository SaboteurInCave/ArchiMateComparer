package de.rostock.graph;

public class GraphConnection {
    private GraphElement vertex;
    private String edgeType, edgeId;

    GraphConnection(GraphElement vertex, String edgeType, String edgeId) {
        this.vertex = vertex;
        this.edgeType = edgeType;
        this.edgeId = edgeId;
    }

    GraphConnection(GraphConnection connection) {
        this.vertex = connection.getVertex();
        this.edgeType = connection.getEdgeType();
        this.edgeId = connection.getEdgeId();
    }

    public GraphElement getVertex() {
        return vertex;
    }

    public String getEdgeType() {
        return edgeType;
    }

    public String getEdgeId() {
        return edgeId;
    }

    @Override
    public String toString() {
        return "vertex: " + vertex + " by (" + edgeId + ": " + edgeType + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphConnection that = (GraphConnection) o;

        if (vertex != null ? !vertex.equals(that.vertex) : that.vertex != null) return false;
        return edgeType != null ? edgeType.equals(that.edgeType) : that.edgeType == null;

    }

    @Override
    public int hashCode() {
        int result = vertex != null ? vertex.hashCode() : 0;
        result = 31 * result + (edgeType != null ? edgeType.hashCode() : 0);
        return result;
    }
}
