package org.springframework.samples.petclinic.rest.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.annotation.Generated;

/**
 * The schema for all error responses.
 */

@Schema(name = "RestError", description = "The schema for all error responses.")
@JsonTypeName("RestError")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-10-28T16:45:57.517547+02:00[Europe/Amsterdam]")
public class RestErrorDto {

  @JsonProperty("status")
  private Integer status;

  @JsonProperty("error")
  private String error;

  @JsonProperty("path")
  private URI path;

  @JsonProperty("timestamp")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime timestamp;

  @JsonProperty("message")
  private String message;

  @JsonProperty("schemaValidationErrors")
  @Valid
  private List<ValidationMessageDto> schemaValidationErrors = new ArrayList<>();

  @JsonProperty("trace")
  private String trace;

  public RestErrorDto status(Integer status) {
    this.status = status;
    return this;
  }

  /**
   * The HTTP status code.
   * @return status
  */

  @Schema(name = "status", accessMode = Schema.AccessMode.READ_ONLY, example = "400", description = "The HTTP status code.", requiredMode = Schema.RequiredMode.REQUIRED)
  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public RestErrorDto error(String error) {
    this.error = error;
    return this;
  }

  /**
   * The short error message.
   * @return error
  */

  @Schema(name = "error", accessMode = Schema.AccessMode.READ_ONLY, example = "Bad Request", description = "The short error message.", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public RestErrorDto path(URI path) {
    this.path = path;
    return this;
  }

  /**
   * The path of the URL for this request.
   * @return path
  */
  @Valid
  @Schema(name = "path", accessMode = Schema.AccessMode.READ_ONLY, example = "/api/owners", description = "The path of the URL for this request.", requiredMode = Schema.RequiredMode.REQUIRED)
  public URI getPath() {
    return path;
  }

  public void setPath(URI path) {
    this.path = path;
  }

  public RestErrorDto timestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * The time the error occured.
   * @return timestamp
  */
  @Valid
  @Schema(name = "timestamp", accessMode = Schema.AccessMode.READ_ONLY, description = "The time the error occured.", requiredMode = Schema.RequiredMode.REQUIRED)
  public OffsetDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public RestErrorDto message(String message) {
    this.message = message;
    return this;
  }

  /**
   * The long error message.
   * @return message
  */

  @Schema(name = "message", accessMode = Schema.AccessMode.READ_ONLY, example = "Request failed schema validation", description = "The long error message.", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public RestErrorDto schemaValidationErrors(List<ValidationMessageDto> schemaValidationErrors) {
    this.schemaValidationErrors = schemaValidationErrors;
    return this;
  }

  public RestErrorDto addSchemaValidationErrorsItem(ValidationMessageDto schemaValidationErrorsItem) {
    if (this.schemaValidationErrors == null) {
      this.schemaValidationErrors = new ArrayList<>();
    }
    this.schemaValidationErrors.add(schemaValidationErrorsItem);
    return this;
  }

  /**
   * Validation errors against the OpenAPI schema.
   * @return schemaValidationErrors
  */
  @NotNull @Valid
  @Schema(name = "schemaValidationErrors", description = "Validation errors against the OpenAPI schema.", requiredMode = Schema.RequiredMode.REQUIRED)
  public List<ValidationMessageDto> getSchemaValidationErrors() {
    return schemaValidationErrors;
  }

  public void setSchemaValidationErrors(List<ValidationMessageDto> schemaValidationErrors) {
    this.schemaValidationErrors = schemaValidationErrors;
  }

  public RestErrorDto trace(String trace) {
    this.trace = trace;
    return this;
  }

  /**
   * The stacktrace for this error.
   * @return trace
  */

  @Schema(name = "trace", accessMode = Schema.AccessMode.READ_ONLY, example = "com.atlassian.oai.validator.springmvc.InvalidRequestException: ...", description = "The stacktrace for this error.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public String getTrace() {
    return trace;
  }

  public void setTrace(String trace) {
    this.trace = trace;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RestErrorDto restError = (RestErrorDto) o;
    return Objects.equals(this.status, restError.status) &&
        Objects.equals(this.error, restError.error) &&
        Objects.equals(this.path, restError.path) &&
        Objects.equals(this.timestamp, restError.timestamp) &&
        Objects.equals(this.message, restError.message) &&
        Objects.equals(this.schemaValidationErrors, restError.schemaValidationErrors) &&
        Objects.equals(this.trace, restError.trace);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, error, path, timestamp, message, schemaValidationErrors, trace);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RestErrorDto {\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    schemaValidationErrors: ").append(toIndentedString(schemaValidationErrors)).append("\n");
    sb.append("    trace: ").append(toIndentedString(trace)).append("\n");
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

