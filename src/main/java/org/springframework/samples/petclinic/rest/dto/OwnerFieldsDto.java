package org.springframework.samples.petclinic.rest.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.annotation.Generated;

/**
 * Editable fields of a pet owner.
 */

@Schema(name = "OwnerFields", description = "Editable fields of a pet owner.")
@JsonTypeName("OwnerFields")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-10-28T16:45:57.517547+02:00[Europe/Amsterdam]")
public class OwnerFieldsDto {

  @JsonProperty("firstName")
  private String firstName;

  @JsonProperty("lastName")
  private String lastName;

  @JsonProperty("address")
  private String address;

  @JsonProperty("city")
  private String city;

  @JsonProperty("telephone")
  private String telephone;

  @JsonProperty("id")
  private Integer id;

  public OwnerFieldsDto firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  /**
   * The first name of the pet owner.
   * @return firstName
  */
//  @NotNull @Pattern(regexp = "^[a-zA-Z]*$") @Size(min = 1, max = 30)
  @Schema(name = "firstName", example = "George", description = "The first name of the pet owner.", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public OwnerFieldsDto lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  /**
   * The last name of the pet owner.
   * @return lastName
  */
//  @NotNull @Pattern(regexp = "^[a-zA-Z]*$") @Size(min = 1, max = 30)
  @Schema(name = "lastName", example = "Franklin", description = "The last name of the pet owner.", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public OwnerFieldsDto address(String address) {
    this.address = address;
    return this;
  }

  /**
   * The postal address of the pet owner.
   * @return address
  */
//  @NotNull @Size(min = 1, max = 255)
  @Schema(name = "address", example = "110 W. Liberty St.", description = "The postal address of the pet owner.", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public OwnerFieldsDto city(String city) {
    this.city = city;
    return this;
  }

  /**
   * The city of the pet owner.
   * @return city
  */
//  @NotNull @Size(min = 1, max = 80)
  @Schema(name = "city", example = "Madison", description = "The city of the pet owner.", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public OwnerFieldsDto telephone(String telephone) {
    this.telephone = telephone;
    return this;
  }

  /**
   * The telephone number of the pet owner.
   * @return telephone
  */
//  @NotNull @Pattern(regexp = "^[0-9]*$") @Size(min = 1, max = 20)
  @Schema(name = "telephone", example = "6085551023", description = "The telephone number of the pet owner.", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getTelephone() {
    return telephone;
  }

  public void setTelephone(String telephone) {
    this.telephone = telephone;
  }

  public OwnerFieldsDto id(Integer id) {
    this.id = id;
    return this;
  }

  @Schema(name = "id", accessMode = Schema.AccessMode.READ_ONLY, example = "1", description = "The ID of the pet owner.", requiredMode = Schema.RequiredMode.REQUIRED)
  public Integer getId() {
      return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OwnerFieldsDto ownerFields = (OwnerFieldsDto) o;
    return Objects.equals(this.firstName, ownerFields.firstName) &&
        Objects.equals(this.lastName, ownerFields.lastName) &&
        Objects.equals(this.address, ownerFields.address) &&
        Objects.equals(this.city, ownerFields.city) &&
        Objects.equals(this.telephone, ownerFields.telephone) &&
        Objects.equals(this.id, ownerFields.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstName, lastName, address, city, telephone, id);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OwnerFieldsDto {\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    city: ").append(toIndentedString(city)).append("\n");
    sb.append("    telephone: ").append(toIndentedString(telephone)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
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

