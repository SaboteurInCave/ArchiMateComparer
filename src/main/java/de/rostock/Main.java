package de.rostock;

import de.rostock.model.Model;
import de.rostock.model.ModelDiff;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 3)
            throw new Exception("There should be 3 input arguments!");

        Model oldModel = new Model(args[0]);
        Model newModel = new Model(args[1]);

        new ModelDiff(oldModel, newModel, args[2]).diff();
    }
}
