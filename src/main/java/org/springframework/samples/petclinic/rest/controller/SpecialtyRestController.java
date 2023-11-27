/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.SpecialtyMapper;
import org.springframework.samples.petclinic.mapper.StringMapper;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.rest.api.SpecialtiesApi;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api")
public class SpecialtyRestController implements SpecialtiesApi {

    @Value("${feature.unsafe}")
    private String unsafe;

    private final ClinicService clinicService;
    private final SpecialtyMapper specialtyMapper;
    private final StringMapper stringMapper;

    public SpecialtyRestController(ClinicService clinicService, SpecialtyMapper specialtyMapper,
                                   StringMapper stringMapper) {
        this.clinicService = clinicService;
        this.specialtyMapper = specialtyMapper;
        this.stringMapper = stringMapper;
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<List<SpecialtyDto>> listSpecialties() {
        List<SpecialtyDto> specialties = new ArrayList<>();
        specialties.addAll(specialtyMapper.toSpecialtyDtos(this.clinicService.findAllSpecialties()));
        if (specialties.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(specialties, HttpStatus.OK);
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<Object> getSpecialty(String specialtyId) {
        Object specialty;

        if (Objects.equals(unsafe, "vuln2")) {
            specialty = this.clinicService.vulnFindSpecialtyById(specialtyId);
        } else {
            specialty = this.clinicService.findSpecialtyById(Integer.parseInt(specialtyId));
        }
        if (specialty == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (specialty instanceof Specialty specialtyObject) {
            return new ResponseEntity<>(specialtyMapper.toSpecialtyDto(specialtyObject), HttpStatus.OK);
        } else {
            String specialtyString = (String) specialty;
            return new ResponseEntity<>(stringMapper.toStringDto(specialtyString), HttpStatus.OK);
        }

    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<SpecialtyDto> addSpecialty(SpecialtyDto specialtyDto) {
        HttpHeaders headers = new HttpHeaders();
        Specialty specialty = specialtyMapper.toSpecialty(specialtyDto);
        this.clinicService.saveSpecialty(specialty);
        headers.setLocation(UriComponentsBuilder.newInstance().path("/api/specialties/{id}").buildAndExpand(specialty.getId()).toUri());
        return new ResponseEntity<>(specialtyMapper.toSpecialtyDto(specialty), headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<SpecialtyDto> updateSpecialty(Integer specialtyId, SpecialtyDto specialtyDto) {
        Specialty currentSpecialty = this.clinicService.findSpecialtyById(specialtyId);
        if (currentSpecialty == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        currentSpecialty.setName(specialtyDto.getName());
        this.clinicService.saveSpecialty(currentSpecialty);
        return new ResponseEntity<>(specialtyMapper.toSpecialtyDto(currentSpecialty), HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Transactional
    @Override
    public ResponseEntity<SpecialtyDto> deleteSpecialty(Integer specialtyId) {
        Specialty specialty = this.clinicService.findSpecialtyById(specialtyId);
        if (specialty == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.clinicService.deleteSpecialty(specialty);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
