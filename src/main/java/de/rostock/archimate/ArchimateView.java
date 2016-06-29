package de.rostock.archimate;

import de.rostock.graph.Graph;

import java.util.HashMap;
import java.util.NoSuchElementException;


/**
 * View representation from XML
 */
public class ArchimateView {
    private String name;
    private String id;
    private String viewpoint;
    private HashMap<String, ArchimateGroup> groups = null;
    private HashMap<String, ArchimateObject> objects = null;
    private HashMap<String, ArchimateConnection> connections = null;
    private Graph graph;


    public ArchimateView(String name, String id, String viewpoint) {
        this.name = name;
        this.id = id;
        this.viewpoint = viewpoint;

        groups = new HashMap<>();
        objects = new HashMap<>();
        connections = new HashMap<>();

        graph = new Graph();
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
                "\nid='" + id + '\'' +
                "\nviewpoint='" + viewpoint + '\'' +
                ",\ngroups=" + groupsRepresentation() +
                ",\nobjects=" + objectsRepresentation() +
                ",\nconnections=" + connectionsRepresentation() + '\n' +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getViewpoint() {
        return viewpoint;
    }

    public HashMap<String, ArchimateConnection> getConnections() {
        return connections;
    }

    public HashMap<String, ArchimateObject> getObjects() {
        return objects;
    }

    public ArchimateObject getObject(String id) {
        if (objects.containsKey(id)) {
            return objects.get(id);
        } else {
            throw new NoSuchElementException();
        }
    }

    public Graph getGraph() {
        return graph;
    }
}