package de.rostock;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 2)
            throw new Exception("There should be two input arguments.");
        Model model = new Model(args[0]);


        File xmlDocument = model.createXmlDocument(model.getRoot().clone(), args[1]);
        if (xmlDocument != null)
            System.out.println("File created.");
    }
}
