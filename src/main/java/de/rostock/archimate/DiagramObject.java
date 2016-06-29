package de.rostock.archimate;

public class DiagramObject {
    private String id;
    private ObjectBounds objectBounds;
    private String parent;

    DiagramObject(String id, ObjectBounds objectBounds, String parent) {
        this.id = id;
        this.objectBounds = objectBounds;
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    ObjectBounds getObjectBounds() {
        return objectBounds;
    }

    public String getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return "DiagramObject{" +
                "id='" + id + '\'' +
                ", objectBounds=" + objectBounds +
                ", parent='" + parent + '\'' +
                '}';
    }
}
