package de.rostock;

import java.util.HashMap;


/**
 * View representation from XML
 */
public class ArchimateView {
    private String name;
    private HashMap<String, ArchimateGroup> groups = null;
    private HashMap<String, ArchimateObject> objects = null;
    private HashMap<String, ArchimateConnection> connections = null;

    public ArchimateView(String name) {
        this.name = name;
        groups = new HashMap<>();
        objects = new HashMap<>();
        connections = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void addGroup(ArchimateGroup archimateGroup) {
        groups.put(archimateGroup.getId(), archimateGroup);
    }

    public void addObject(ArchimateObject archimateObject) {
        objects.put(archimateObject.getId(), archimateObject);
    }

    public void addConnection(ArchimateConnection archimateConnection) {
        connections.put(archimateConnection.getId(), archimateConnection);
    }

    private String groupsRepresentation() {
        String result = "";

        for (String key : groups.keySet()) {
            result += groups.get(key).toString() + ',';
        }

        return result;
    }

    private String objectsRepresentation() {
        String result = "";

        for (String key : objects.keySet()) {
            result += objects.get(key).toString() + ',';
        }

        return result;
    }

    private String connectionsRepresentation() {
        String result = "";

        for (String key : connections.keySet()) {
            result += connections.get(key).toString() + ',';
        }

        return result;
    }

    @Override
    public String toString() {

        return "ArchimateView{" +
                "\nname='" + name + '\'' +
                ",\ngroups=" + groupsRepresentation() +
                ",\nobjects=" + objectsRepresentation() +
                ",\nconnections=" + connectionsRepresentation() + '\n' +
                '}';
    }
}