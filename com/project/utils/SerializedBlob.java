package com.project.utils;

import java.util.List;
import java.util.Map;

public interface SerializedBlob {
    public SerializedBlob extendMap(Map<String, SerializedBlob> m);

    public Factory.Type type();

    public Map<String, SerializedBlob> map();

    public List<SerializedBlob> array();

    public Object primitive();

    public String string();

    public Integer intValue();

    public Boolean booleanValue();

    public Double doubleValue();

    public String toSerialized();

    public interface Factory {
        public enum Type {
            OBJECT, ARRAY, PRIMITIVE
        }

        public SerializedBlob fromMap(Map<String, SerializedBlob> m);

        public SerializedBlob array(List<SerializedBlob> l);

        public SerializedBlob string(String s);

        public SerializedBlob intValue(int i);

        public SerializedBlob doubleValue(double d);

        public SerializedBlob booleanValue(boolean d);

        public SerializedBlob nullValue();

        public SerializedBlob primitive(Object o);

        public SerializedBlob parse(String serialized);

    }
}
