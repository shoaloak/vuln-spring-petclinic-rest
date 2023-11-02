package org.springframework.samples.petclinic.model;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class PreparedStatementData {
    // name, type, value
    private final Map<String, Map.Entry<Class<?>, Object>> parametersWithTypes;

    public PreparedStatementData() {
        this.parametersWithTypes = new HashMap<>();
    }

    public PreparedStatementData(Class<?> type, String name, Object value) {
        this.parametersWithTypes = new HashMap<>();
        this.parametersWithTypes.put(name, new AbstractMap.SimpleEntry<>(type, value));
    }

    public void addParameter(Class<?> type, String name, Object value) {
        this.parametersWithTypes.put(name, new AbstractMap.SimpleEntry<Class<?>, Object>(type, value));
    }

    public Map<String, Map.Entry<Class<?>, Object>> getParametersWithTypes() {
        return this.parametersWithTypes;
    }
}

