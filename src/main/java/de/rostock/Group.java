package de.rostock;

public class Group extends DiagramObject {
    private String name, fillColor;

    public Group(String id, ObjectBounds objectBounds, String parent, String name, String fillColor) {
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
        return "Group{" +
                "id='" + getId() + '\'' +
                ", name='" + name + '\'' +
                ", fillColor='" + fillColor + '\'' +
                ", bounds=" + getObjectBounds() +
                '}';
    }
}
