package de.rostock;

import org.jdom2.Attribute;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class ArchimateElement {
   private HashMap<String, String> attributes;

    public ArchimateElement(List<Attribute> xmlAttributes) {

        attributes = new HashMap<String, String>();

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
        return getValue("xsi:type");
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
    public Set<String> getNames() {
        return attributes.keySet();
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
