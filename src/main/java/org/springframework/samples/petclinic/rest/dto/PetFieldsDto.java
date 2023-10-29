package org.springframework.samples.petclinic.rest.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.annotation.Generated;

/**
 * Editable fields of a pet.
 */

@Schema(name = "PetFields", description = "Editable fields of a pet.")
@JsonTypeName("PetFields")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-10-28T16:45:57.517547+02:00[Europe/Amsterdam]")
public class PetFieldsDto {

  @JsonProperty("name")
  private String name;

  @JsonProperty("birthDate")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate birthDate;

  @JsonProperty("type")
  private PetTypeDto type;

  public PetFieldsDto name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of the pet.
   * @return name
  */
  @NotNull @Size(max = 30)
  @Schema(name = "name", example = "Leo", description = "The name of the pet.", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PetFieldsDto birthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
    return this;
  }

  /**
   * The date of birth of the pet.
   * @return birthDate
  */
  @NotNull @Valid
  @Schema(name = "birthDate", example = "Tue Sep 07 02:00:00 CEST 2010", description = "The date of birth of the pet.", requiredMode = Schema.RequiredMode.REQUIRED)
  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public PetFieldsDto type(PetTypeDto type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
  */
  @NotNull @Valid
  @Schema(name = "type", requiredMode = Schema.RequiredMode.REQUIRED)
  public PetTypeDto getType() {
    return type;
  }

  public void setType(PetTypeDto type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PetFieldsDto petFields = (PetFieldsDto) o;
    return Objects.equals(this.name, petFields.name) &&
        Objects.equals(this.birthDate, petFields.birthDate) &&
        Objects.equals(this.type, petFields.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, birthDate, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PetFieldsDto {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    birthDate: ").append(toIndentedString(birthDate)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

