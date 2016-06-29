package de.rostock.archimate;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static de.rostock.model.Model.xsi;

public class ArchimateXMLElement {
   private HashMap<String, String> attributes;

    public ArchimateXMLElement(List<Attribute> xmlAttributes) {

        attributes = new HashMap<>();

        for (Attribute attribute : xmlAttributes) {
            attributes.put(attribute.getName(), attribute.getValue());
        }
    }

    /**
     * Get attribute value by name
     * @param name attribute name
     * @return attribute value if it exists
     * @throws NoSuchElementException
     */
    public String getValue(final String name) throws NoSuchElementException {
        if (attributes.containsKey(name)) {
            return attributes.get(name);
        } else {
            throw new NoSuchElementException();
        }
    }


    /**
     * Set attribute value by name
     * @param name name of attribute
     * @param value value of attribute
     * @throws NoSuchElementException if attributes doesn't contain name
     */
    public void setValue(final String name, final String value) throws NoSuchElementException {
        if (attributes.containsKey(name)) {
            attributes.put(name, value);
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Helper function for setting id
     * @param id id value of element
     * @throws NoSuchElementException
     */
    public void setId(final String id) throws NoSuchElementException {
        setValue("id", id);
    }

    /**
     * Helper function for getting id of element
     * @return id
     * @throws NoSuchElementException
     */
    public String getId() throws NoSuchElementException {
        return getValue("id");
    }

    /**
     * Helper function for getting type of element
     * @return type
     * @throws NoSuchElementException
     */
    public String getType() throws NoSuchElementException {
        return getValue("type");
    }

    /**
     * Helper function for getting name of element
     * @return name
     * @throws NoSuchElementException
     */
    public String getName() throws NoSuchElementException {
        return getValue("name");
    }

    /**
     * Helper function for getting source of element
     * @return source
     * @throws NoSuchElementException
     */
    public String getSource() throws NoSuchElementException {
        return getValue("source");
    }

    /**
     * Helper function for getting target of element
     * @return target
     * @throws NoSuchElementException
     */
    public String getTarget() throws NoSuchElementException {
        return getValue("target");
    }

    /**
     * Get all attributes names of element
     * @return set of names
     */
    public Set<String> getAttributesKeys() {
        return attributes.keySet();
    }

    public Element createElement(final ArchimateElementType type) {
        Element element = new Element("element");

        if (type.equals(ArchimateElementType.element)) {

            element.setAttribute("type", attributes.get("type"), xsi);
            element.setAttribute("id", attributes.get("id"));
            element.setAttribute("name", attributes.get("name"));

        } else if (type.equals(ArchimateElementType.relation)) {

            element.setAttribute("type", attributes.get("type"), xsi);
            element.setAttribute("id", attributes.get("id"));
            element.setAttribute("source", attributes.get("source"));
            element.setAttribute("target", attributes.get("target"));

        }
        else {
            attributes
                .keySet()
                .forEach(s -> {
                    boolean addNamespace = false;

                    if (s.equals("type")) {
                        addNamespace = true;
                    }

                    if (addNamespace) {
                        element.setAttribute(s, attributes.get(s), xsi);
                    } else {
                        element.setAttribute(s, attributes.get(s));
                    }
                });
        }


        return element;
    }

    @Override
    public String toString() {
        String result = "";

        for (String key : attributes.keySet()) {
            result += String.format("   %s = %s\n", key, attributes.get(key));
        }

        return String.format("[\n%s]", result);
    }
}