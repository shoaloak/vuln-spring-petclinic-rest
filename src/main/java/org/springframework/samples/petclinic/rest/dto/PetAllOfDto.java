package org.springframework.samples.petclinic.rest.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.annotation.Generated;

/**
 * PetAllOfDto
 */

@JsonTypeName("Pet_allOf")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-10-28T16:45:57.517547+02:00[Europe/Amsterdam]")
public class PetAllOfDto {

  @JsonProperty("id")
  private Integer id;

  @JsonProperty("ownerId")
  private Integer ownerId;

  @JsonProperty("visits")
  @Valid
  private List<VisitDto> visits = new ArrayList<>();

  public PetAllOfDto id(Integer id) {
    this.id = id;
    return this;
  }

  /**
   * The ID of the pet.
   * minimum: 0
   * @return id
  */
  @Min(0)
  @Schema(name = "id", accessMode = Schema.AccessMode.READ_ONLY, example = "1", description = "The ID of the pet.", requiredMode = Schema.RequiredMode.REQUIRED)
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PetAllOfDto ownerId(Integer ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  /**
   * The ID of the pet's owner.
   * minimum: 0
   * @return ownerId
  */
  @Min(0)
  @Schema(name = "ownerId", accessMode = Schema.AccessMode.READ_ONLY, example = "1", description = "The ID of the pet's owner.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public Integer getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(Integer ownerId) {
    this.ownerId = ownerId;
  }

  public PetAllOfDto visits(List<VisitDto> visits) {
    this.visits = visits;
    return this;
  }

  public PetAllOfDto addVisitsItem(VisitDto visitsItem) {
    if (this.visits == null) {
      this.visits = new ArrayList<>();
    }
    this.visits.add(visitsItem);
    return this;
  }

  /**
   * Vet visit bookings for this pet.
   * @return visits
  */
  @Valid
  @Schema(name = "visits", accessMode = Schema.AccessMode.READ_ONLY, description = "Vet visit bookings for this pet.", requiredMode = Schema.RequiredMode.REQUIRED)
  public List<VisitDto> getVisits() {
    return visits;
  }

  public void setVisits(List<VisitDto> visits) {
    this.visits = visits;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PetAllOfDto petAllOf = (PetAllOfDto) o;
    return Objects.equals(this.id, petAllOf.id) &&
        Objects.equals(this.ownerId, petAllOf.ownerId) &&
        Objects.equals(this.visits, petAllOf.visits);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, ownerId, visits);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PetAllOfDto {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    ownerId: ").append(toIndentedString(ownerId)).append("\n");
    sb.append("    visits: ").append(toIndentedString(visits)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

