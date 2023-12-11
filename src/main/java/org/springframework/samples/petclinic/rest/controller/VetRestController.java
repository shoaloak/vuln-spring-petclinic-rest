/*
 * Copyright 2016-2018 the original author or authors.
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
import org.springframework.samples.petclinic.mapper.VetMapper;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.rest.api.VetsApi;
import org.springframework.samples.petclinic.rest.dto.VetDto;
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
 * @author Axel Koolhaas
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api")
public class VetRestController implements VetsApi {

    @Value("${feature.unsafe}")
    private String unsafe;

    private final ClinicService clinicService;
    private final VetMapper vetMapper;
    private final SpecialtyMapper specialtyMapper;
    private final StringMapper stringMapper;

    public VetRestController(ClinicService clinicService, VetMapper vetMapper, SpecialtyMapper specialtyMapper,
                             StringMapper stringMapper) {
        this.clinicService = clinicService;
        this.vetMapper = vetMapper;
        this.specialtyMapper = specialtyMapper;
        this.stringMapper = stringMapper;
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<List<VetDto>> listVets() {
        List<VetDto> vets = new ArrayList<>();
        vets.addAll(vetMapper.toVetDtos(this.clinicService.findAllVets()));
        if (vets.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(vets, HttpStatus.OK);
    }

    /**
     * Get Vet
     *
     * @param vetId: Purposely vulnerable to SQL Injection attack
     * @return ResponseEntity<>
     */
    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<Object> getVet(String vetId)  {
        Object vet;

        if (Objects.equals(unsafe, "vuln1") || Objects.equals(unsafe, "all")) {
                vet = this.clinicService.vulnFindVetById(vetId);
        } else {
            vet = this.clinicService.findVetById(Integer.parseInt(vetId));
        }
        if (vet == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (vet instanceof Vet vetObject) {
            return new ResponseEntity<>(vetMapper.toVetDto(vetObject), HttpStatus.OK);
        } else {
            String vetString = (String) vet;
            return new ResponseEntity<>(stringMapper.toStringDto(vetString), HttpStatus.OK);
        }
    }

    /**
     * Add Vet
     * @param vetDto The vet (required), firstName purposely vulnerable to SQL Injection attack
     * @return ResponseEntity<>
     */
    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<VetDto> addVet(VetDto vetDto) {
        HttpHeaders headers = new HttpHeaders();
        Vet vet = vetMapper.toVet(vetDto);
        if (Objects.equals(unsafe, "vuln3") || Objects.equals(unsafe, "all")) {
            this.clinicService.vulnSaveVet(vet);
        } else {
            this.clinicService.saveVet(vet);
        }
        headers.setLocation(UriComponentsBuilder.newInstance().path("/api/vets/{id}").buildAndExpand(vet.getId()).toUri());
        return new ResponseEntity<>(vetMapper.toVetDto(vet), headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<VetDto> updateVet(String vetId,VetDto vetDto)  {
        Vet currentVet = this.clinicService.findVetById(Integer.parseInt(vetId));
        if (currentVet == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        currentVet.setFirstName(vetDto.getFirstName());
        currentVet.setLastName(vetDto.getLastName());
        currentVet.clearSpecialties();
        for (Specialty spec : specialtyMapper.toSpecialtys(vetDto.getSpecialties())) {
            currentVet.addSpecialty(spec);
        }
        this.clinicService.saveVet(currentVet);
        return new ResponseEntity<>(vetMapper.toVetDto(currentVet), HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Transactional
    @Override
    public ResponseEntity<VetDto> deleteVet(String vetId) {
        Vet vet = this.clinicService.findVetById(Integer.parseInt(vetId));
        if (vet == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.clinicService.deleteVet(vet);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
