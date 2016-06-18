package de.rostock;

import java.util.ArrayList;
import java.util.List;

public class ArchimateObject extends DiagramObject {

    private String textAlignment;
    private String archimateElement;
    private List<String> targetConnections;
    private List<String> sourceConnections;

    public ArchimateObject(String id, ObjectBounds objectBounds, String parent, String textAlignment, String archimateElement) {
        super(id, objectBounds, parent);
        this.textAlignment = textAlignment;
        this.archimateElement = archimateElement;

        targetConnections = new ArrayList<>();
        sourceConnections = new ArrayList<>();
    }

    public void addTargetConnection(String connection) {
        targetConnections.add(connection);
    }

    public void addSourceConnection(String connection) {
        sourceConnections.add(connection);
    }

    public String getArchimateElement() {
        return archimateElement;
    }

    @Override
    public String toString() {
        return "ArchimateObject{" +
                super.toString() +
                "textAlignment='" + textAlignment + '\'' +
                ", archimateElement='" + archimateElement + '\'' +
                ", targetConnections=" + targetConnections +
                ", sourceConnections=" + sourceConnections +
                '}';
    }
}
