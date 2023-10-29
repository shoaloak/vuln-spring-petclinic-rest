package org.springframework.samples.petclinic.mapper;

import org.mapstruct.Mapper;
import org.springframework.samples.petclinic.rest.dto.StringDto;

@Mapper
public interface StringMapper {
    StringDto toStringDto(String string);
}
