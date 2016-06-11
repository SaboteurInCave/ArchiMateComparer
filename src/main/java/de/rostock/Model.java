package de.rostock;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Model {
    private String path;

    Model(String path) {
        this.path = path;
    }


    Element parseModel() {
        SAXBuilder builder = new SAXBuilder();
        try {
            Document document = builder.build(new File(path));
            return document.getRootElement();
            /*System.out.println(document.getBaseURI());
            ArrayList<Element> newElements = new ArrayList<Element>();
            ArrayList<Element> allElements = new ArrayList<Element>();
            newElements.add(document.getRootElement());
            while (!newElements.isEmpty()) {
                ArrayList<Element> curElements = new ArrayList<Element>(newElements);
                allElements.addAll(newElements);
                newElements.clear();
                for (Element element : curElements) {
                    newElements.addAll(element.getChildren());
                }
            }
            for (Element element : allElements) {
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
        return null;
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
}
