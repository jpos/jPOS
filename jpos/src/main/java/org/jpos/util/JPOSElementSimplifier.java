package org.jpos.util;

import org.jdom2.Element;
import org.jolokia.converter.json.simplifier.SimplifierExtractor;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QFactory;

import javax.management.Attribute;
import javax.management.AttributeList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.jpos.q2.Q2.getQ2;

public class JPOSElementSimplifier extends SimplifierExtractor<Element> {
    /**
     * Construct the simplifier for Property elements
     */
    public JPOSElementSimplifier() {
        super(Element.class);

        Object[][] pAttrs = {
                { "name", new NameAttributeExtractor() },
                { "objectName", new ObjectNameAttributeExtractor() },
                { "attr", new AttrAttributeExtractor() },
                { "property", new PropertyAttributeExtractor() },
                { "hasChildNodes", new ChildAttributeExtractor() }
        };
        addExtractors(pAttrs);
    }

    // ==================================================================================
    private static class ObjectNameAttributeExtractor implements AttributeExtractor<Element> {
        /** {@inheritDoc} */
        public Object extract(Element element)  { return element.getAttributeValue("class"); }
    }
    private static class AttrAttributeExtractor implements AttributeExtractor<Element> {
        /** {@inheritDoc} */
        public Object extract(Element element) {
            Map<String, Object> map = null;
            try {
                QFactory factory = getQ2().getFactory();
                AttributeList attributeList = factory.getAttributeList(element);
                for (Attribute key: attributeList.asList()) {
                    if (map == null)
                        map = new HashMap<>();
                    map.put(key.getName(), key.getValue());
                }
            } catch (ConfigurationException configurationException) {
                return null;
            }
            return map;
        }
    }
    private static class PropertyAttributeExtractor implements AttributeExtractor<Element> {
        /** {@inheritDoc} */
        public Object extract(Element element) {
            Map<String, Object> map = null;
            try {
                QFactory factory = getQ2().getFactory();
                Configuration simpleConfig = factory.getConfiguration(element);
                for (String key: simpleConfig.keySet()) {
                    if (map == null)
                        map = new HashMap<>();
                    map.put(key, simpleConfig.get(key));
                }
            } catch (ConfigurationException configurationException) {
                return null;
            }
            return map;
        }
    }
    private static class NameAttributeExtractor implements AttributeExtractor<Element> {
        /** {@inheritDoc} */
        public Object extract(Element element) { return element.getName(); }
    }
    private static class ChildAttributeExtractor implements AttributeExtractor<Element> {
        /** {@inheritDoc} */
        public Object extract(Element element) {
            List<Element> extracted = null;
            List<Element> children = element.getChildren();
            for(Element child: children){
                String key = child.getName();
                if (!Stream.of("property", "attr").anyMatch(key::equals)) {
                    if (extracted == null) {
                        extracted = new ArrayList<>();
                    }
                    extracted.add(child);
                }
            }
            return extracted;
        }
    }
}
