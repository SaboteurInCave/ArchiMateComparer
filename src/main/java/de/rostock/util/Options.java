package de.rostock.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
}
