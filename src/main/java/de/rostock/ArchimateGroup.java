package de.rostock;

public class ArchimateGroup extends DiagramObject {
    private String name, fillColor;

    public ArchimateGroup(String id, ObjectBounds objectBounds, String parent, String name, String fillColor) {
        super(id, objectBounds, parent);
        this.name = name;
        this.fillColor = fillColor;
    }

    public String getName() {
        return name;
    }

    public String getFillColor() {
        return fillColor;
    }

    @Override
    public String toString() {
        return "ArchimateGroup{" +
                super.toString() +
                "id='" + getId() + '\'' +
                ", name='" + name + '\'' +
                ", fillColor='" + fillColor + '\'' +
                ", bounds=" + getObjectBounds() +
                '}';
    }
}
