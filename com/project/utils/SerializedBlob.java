package com.project.utils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SerializedBlob {
    public enum Type {
        OBJECT, ARRAY, PRIMITIVE
    }

    private final Type type;
    private Map<String, SerializedBlob> map;
    private final List<SerializedBlob> array;
    private final Object primitive;

    private SerializedBlob(Type t, Map<String, SerializedBlob> m, List<SerializedBlob> a, Object p) {
        this.type = t;
        this.map = m;
        this.array = a;
        this.primitive = p;
    }

    public static SerializedBlob fromMap(Map<String, SerializedBlob> m) {
        return new SerializedBlob(Type.OBJECT, m, null, null);
    }

    public SerializedBlob extendMap(Map<String, SerializedBlob> m) {
        Map<String, SerializedBlob> newMap = new HashMap<>();
        newMap.putAll(m);
        newMap.putAll(this.map);
        this.map = newMap;
        return this;
    }

    public static SerializedBlob array(List<SerializedBlob> l) {
        return new SerializedBlob(Type.ARRAY, null, l, null);
    }

    public static SerializedBlob string(String s) {
        return new SerializedBlob(Type.PRIMITIVE, null, null, s);
    }

    public static SerializedBlob intValue(int i) {
        return new SerializedBlob(Type.PRIMITIVE, null, null, i);
    }

    public static SerializedBlob doubleValue(double d) {
        return new SerializedBlob(Type.PRIMITIVE, null, null, d);
    }

    public static SerializedBlob booleanValue(boolean d) {
        return new SerializedBlob(Type.PRIMITIVE, null, null, d);
    }

    public static SerializedBlob nullValue() {
        return new SerializedBlob(Type.PRIMITIVE, null, null, null);
    }

    public static SerializedBlob primitive(Object o) {
        if (o != null && !(o instanceof Integer) && !(o instanceof Double) && !(o instanceof String))
            throw new IllegalArgumentException("Unsupported primitive: " + o.getClass());
        return new SerializedBlob(Type.PRIMITIVE, null, null, o);
    }

    public static SerializedBlob parse(String xml) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
            return from(doc.getDocumentElement());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse SerializedBlob", e);
        }
    }

    private static SerializedBlob from(Element el) {
        return switch (el.getTagName()) {
            case "object" -> {
                Map<String, SerializedBlob> m = new LinkedHashMap<>();
                for (Element entry : children(el))
                    m.put(entry.getAttribute("key"), from(children(entry).get(0)));
                yield fromMap(m);
            }
            case "array" -> {
                List<SerializedBlob> l = new ArrayList<>();
                for (Element c : children(el))
                    l.add(from(c));
                yield array(l);
            }
            case "primitive" -> {
                String t = el.getAttribute("type");
                String text = el.getTextContent();
                yield switch (t) {
                    case "null" -> nullValue();
                    case "int" -> intValue(Integer.parseInt(text));
                    case "double" -> doubleValue(Double.parseDouble(text));
                    case "boolean" -> booleanValue(text.equals("true"));
                    case "string" -> string(text);
                    default -> throw new IllegalArgumentException("Unknown primitive type: " + t);
                };
            }
            default -> throw new IllegalArgumentException("Unknown tag: " + el.getTagName());
        };
    }

    private static List<Element> children(Element el) {
        List<Element> out = new ArrayList<>();
        NodeList ns = el.getChildNodes();
        for (int i = 0; i < ns.getLength(); i++)
            if (ns.item(i) instanceof Element e)
                out.add(e);
        return out;
    }

    public Type type() {
        return type;
    }

    public Map<String, SerializedBlob> map() {
        if (type != Type.OBJECT)
            throw new IllegalStateException("Not an object");
        return map;
    }

    public List<SerializedBlob> array() {
        if (type != Type.ARRAY)
            throw new IllegalStateException("Not an array");
        return array;
    }

    public Object primitive() {
        if (type != Type.PRIMITIVE)
            throw new IllegalStateException("Not a primitive");
        return primitive;
    }

    public String string() {
        Object v = primitive();
        if (v != null && !(v instanceof String))
            throw new IllegalStateException("Not a string");
        return (String) v;
    }

    public Integer intValue() {
        Object v = primitive();
        if (v != null && !(v instanceof Integer))
            throw new IllegalStateException("Not an int");
        return (Integer) v;
    }

    public Boolean booleanValue() {
        Object v = primitive();
        if (v != null && !(v instanceof Boolean))
            throw new IllegalStateException("Not an int");
        return (Boolean) v;
    }

    public Double doubleValue() {
        Object v = primitive();
        if (v != null && !(v instanceof Double))
            throw new IllegalStateException("Not a double");
        return (Double) v;
    }

    public String toXml() {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
            doc.appendChild(toEl(doc));
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter sw = new StringWriter();
            t.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize SerializedBlob", e);
        }
    }

    private Element toEl(Document doc) {
        switch (type) {
            case OBJECT -> {
                Element el = doc.createElement("object");
                for (Map.Entry<String, SerializedBlob> e : map.entrySet()) {
                    Element entry = doc.createElement("entry");
                    entry.setAttribute("key", e.getKey());
                    entry.appendChild(e.getValue().toEl(doc));
                    el.appendChild(entry);
                }
                return el;
            }
            case ARRAY -> {
                Element el = doc.createElement("array");
                for (SerializedBlob v : array)
                    el.appendChild(v.toEl(doc));
                return el;
            }
            case PRIMITIVE -> {
                Element el = doc.createElement("primitive");
                if (primitive == null) {
                    el.setAttribute("type", "null");
                } else if (primitive instanceof Integer i) {
                    el.setAttribute("type", "int");
                    el.setTextContent(i.toString());
                } else if (primitive instanceof Double d) {
                    el.setAttribute("type", "double");
                    el.setTextContent(d.toString());
                } else if (primitive instanceof String s) {
                    el.setAttribute("type", "string");
                    el.setTextContent(s);
                } else if (primitive instanceof Boolean b) {
                    el.setAttribute("type", "boolean");
                    el.setTextContent(b ? "true" : "false");
                } else {
                    throw new IllegalStateException("Unsupported primitive: " + primitive.getClass());
                }
                return el;
            }
        }
        throw new IllegalStateException();
    }
}