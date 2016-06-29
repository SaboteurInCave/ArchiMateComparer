package de.rostock.util;

class Properties {
    private String oldModel;
    private String newModel;
    private String diffModel;

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

    @Override
    public String toString() {
        return "Properties{" +
                "oldModel='" + oldModel + '\'' +
                ", newModel='" + newModel + '\'' +
                ", diffModel='" + diffModel + '\'' +
                '}';
    }
}
