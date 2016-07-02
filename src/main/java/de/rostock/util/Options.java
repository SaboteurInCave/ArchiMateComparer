package de.rostock.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/*
    View mapping : newView(unique) -> oldView(not unique)
 */

public class Options {
    private String optionsPath;

    private Properties properties;

    public Options(String optionsPath) {
        this.optionsPath = optionsPath;
    }

    public void load() throws IOException, JsonSyntaxException {
        String jsonContent = new String(Files.readAllBytes(Paths.get(optionsPath)));

        Gson gson = new Gson();
        properties = gson.fromJson(jsonContent, Properties.class);

        // Transform list of view mapping to hashMap
        properties.getViewMappingList().forEach(viewMappingEntry -> {
            properties.setViewMapping(viewMappingEntry.getNewView(), viewMappingEntry.getOldView());
        });
    }

    public String getOldModelPath() {
        return properties.getOldModel();
    }

    public String getNewModelPath() {
        return properties.getNewModel();
    }

    public String getDiffModelPath() {
        return properties.getDiffModel();
    }

    public HashMap<String, String> getViewMapping() {
        return properties.getViewMapping();
    }
}
