package de.rostock;

public class ArchimateConnection {
    private String id, source, target, relationship;

    public ArchimateConnection(String id, String source, String target, String relationship) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.relationship = relationship;
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getRelationship() {
        return relationship;
    }

    @Override
    public String toString() {
        return "ArchimateConnection{" +
                super.toString() +
                "id='" + id + '\'' +
                ", source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", relationship='" + relationship + '\'' +
                '}';
    }
}
