package org.springframework.samples.petclinic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.samples.petclinic.rest.dto.StringDto;

@Mapper
public interface StringMapper {
    @Mapping(source = "string", target = "data")
    StringDto toStringDto(String string);
}
