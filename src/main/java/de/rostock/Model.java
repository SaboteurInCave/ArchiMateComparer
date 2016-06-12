package de.rostock;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Model {
    private String path;
    private Element root = null;

    private HashMap<String, ArrayList<ArchimateElement>> folders;
    private HashMap<String, String> idFolder;

    Model(String path) {
        this.path = path;
        folders = new HashMap<String, ArrayList<ArchimateElement>>();
        idFolder = new HashMap<String, String>();

        parseModel();
    }

    private void iterateElements(Element folder, final String originalFolder) {

        for (Element element : folder.getChildren()) {

            final String name = element.getName();

            if (name.equals("folder")) {
                iterateElements(element, originalFolder);
            } else if (name.equals("element")) {
                if (!folders.containsKey(originalFolder)) {
                    folders.put(originalFolder, new ArrayList<ArchimateElement>());
                }

                ArchimateElement newElement = new ArchimateElement(element.getAttributes());
                folders.get(originalFolder).add(newElement);
                idFolder.put(newElement.getId(), originalFolder);
            }
        }

    }

    void parseModel() {
        SAXBuilder builder = new SAXBuilder();
        try {
            Document document = builder.build(new File(path));

            root = document.getRootElement();

            List<Element> fileFolders = root.getChildren("folder");

            for (Element folder : fileFolders) {
                iterateElements(folder, folder.getAttributeValue("name"));
            }

            for (String folder : folders.keySet()) {
                System.out.println(String.format("%s:", folder));

                for (ArchimateElement element : folders.get(folder)) {
                    System.out.println(element + ",");
                }
            }

            //    return document.getRootElement();


            /*System.out.println(document.getBaseURI());
            ArrayList<ArchimateElement> newElements = new ArrayList<ArchimateElement>();
            ArrayList<ArchimateElement> allElements = new ArrayList<ArchimateElement>();
            newElements.add(document.getRootElement());
            while (!newElements.isEmpty()) {
                ArrayList<ArchimateElement> curElements = new ArrayList<ArchimateElement>(newElements);
                allElements.addAll(newElements);
                newElements.clear();
                for (ArchimateElement element : curElements) {
                    newElements.addAll(element.getChildren());
                }
            }
            for (ArchimateElement element : allElements) {
                String elementInfo = element.getName() + "\n";
                List<Attribute> attributes = element.getAttributes();
                for (Attribute attribute : attributes) {
                    elementInfo += String.format("\t%s:\t%s\n", attribute.getName(), attribute.getValue());
                }
                System.out.println(elementInfo);
            }*/
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    File createXmlDocument(Element rootElement, String outputPath) {
        Document xmlDoc = new Document();
        xmlDoc.setRootElement(rootElement);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            File result = new File(outputPath);
            outputter.output(xmlDoc, new FileOutputStream(result));
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Element getRoot() {
        return root;
    }
}
