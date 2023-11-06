package org.springframework.samples.petclinic.model;

/**
 * @param type      the type of the parameter
 * @param name      the name of the parameter
 * @param value     the value of the parameter
 */
public record PreparedStatementParameter(Class<?> type, String name, Object value) {
}

