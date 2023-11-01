package org.springframework.samples.petclinic.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StringDto {
    @JsonProperty("data")
    private String data;

    public StringDto data(String data) {
        this.data = data;
        return this;
    }
}
