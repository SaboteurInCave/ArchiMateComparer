package de.rostock;

import com.google.gson.JsonSyntaxException;
import de.rostock.model.Model;
import de.rostock.model.ModelDiff;
import de.rostock.util.Options;

import java.io.IOException;

public class Main {
    public static void main(String[] args)  {

        if (args.length < 1) {
            System.err.println("Can't find options file!");
            System.exit(-1);
        }

        Options options = new Options(args[0]);

        try {
            options.load();

            Model oldModel = new Model(options.getOldModelPath());
            Model newModel = new Model(options.getNewModelPath());

            new ModelDiff(oldModel, newModel, options.getDiffModelPath()).diff();

        } catch (IOException e) {
            System.err.println("Problems with IO: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.err.println("Problems with JSON syntax: " + e.getMessage());
        }

    }
}
