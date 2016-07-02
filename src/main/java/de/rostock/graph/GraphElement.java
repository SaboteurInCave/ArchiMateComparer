package de.rostock.graph;

public class GraphElement {
    private String name,type, viewChildId;

    public GraphElement(String name, String type, String viewChildId) {
        this.name = name;
        this.type = type;
        this.viewChildId = viewChildId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getViewChildId() {
        return viewChildId;
    }

    @Override
    public String toString() {
        return "(name: " + name + ", type: " + type + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphElement element = (GraphElement) o;

        if (name != null ? !name.equals(element.name) : element.name != null) return false;
        return type != null ? type.equals(element.type) : element.type == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
