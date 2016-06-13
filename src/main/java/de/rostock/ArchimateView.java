package de.rostock;

import java.util.HashMap;


/**
 * View representation from XML
 */
public class ArchimateView {
    private String name;
    private HashMap<String, Group> groups = null;

    public ArchimateView(String name) {
        this.name = name;
        groups = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void addGroup(Group group) {
        groups.put(group.getId(), group);
    }

    private String groupsRepresentation() {
        String result = "";

        for (String key : groups.keySet()) {
            result += groups.get(key).toString() + ',';
        }

        return result;
    }

    @Override
    public String toString() {
        return "ArchimateView{" +
                "name='" + name + '\'' +
                ", groups=" + groupsRepresentation() +
                '}';
    }
}