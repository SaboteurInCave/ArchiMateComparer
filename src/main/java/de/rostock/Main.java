package de.rostock;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 2)
            throw new Exception("There should be two input arguments.");
        Model oldModel = new Model(args[0]);
        Model newModel = new Model(args[1]);

        List<ArchimateView> newViews = newModel.getViews();
        List<ArchimateView> oldViews = oldModel.getViews();

        newViews.forEach(newView -> {
            ArchimateView oldView = oldViews.stream().filter(view -> Objects.equals(newView.getName(), view.getName())).findFirst().get();

            new ViewCompare(oldView, newView).compare();
        });


        /*
        File xmlDocument = model.createXmlDocument(model.getRoot().clone(), args[2]);
        if (xmlDocument != null)
            System.out.println("File created.");
        */
    }
}
