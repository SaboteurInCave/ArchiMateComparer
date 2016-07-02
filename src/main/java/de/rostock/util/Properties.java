package de.rostock.util;

import java.util.ArrayList;
import java.util.HashMap;

class Properties {
    private String oldModel;
    private String newModel;
    private String diffModel;
    private ArrayList<ViewMappingEntry> viewMappingList = new ArrayList<>();

    private HashMap<String, String> viewMapping = new HashMap<>();

    public String getOldModel() {
        return oldModel;
    }

    public void setOldModel(String oldModel) {
        this.oldModel = oldModel;
    }

    public String getNewModel() {
        return newModel;
    }

    public void setNewModel(String newModel) {
        this.newModel = newModel;
    }

    public String getDiffModel() {
        return diffModel;
    }

    public void setDiffModel(String diffModel) {
        this.diffModel = diffModel;
    }

    public ArrayList<ViewMappingEntry> getViewMappingList() {
        return viewMappingList;
    }

    public void setViewMappingList(ArrayList<ViewMappingEntry> viewMappingList) {
        this.viewMappingList = viewMappingList;
    }

    public HashMap<String, String> getViewMapping() {
        return viewMapping;
    }

    public void setViewMapping(final String newView, final String oldView) {
        viewMapping.put(newView, oldView);
    }

    @Override
    public String toString() {
        return "Properties{" +
                "oldModel='" + oldModel + '\'' +
                ", newModel='" + newModel + '\'' +
                ", diffModel='" + diffModel + '\'' +
                ", viewMapping=" + viewMapping +
                '}';
    }
}
