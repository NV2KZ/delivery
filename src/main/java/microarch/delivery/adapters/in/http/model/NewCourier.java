package microarch.delivery.adapters.in.http.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.annotation.Generated;

/**
 * NewCourier
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-11T00:26:20.398575+03:00[Europe/Moscow]", comments = "Generator version: 7.20.0")
public class NewCourier {

  private String name;

  public NewCourier() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public NewCourier(String name) {
    this.name = name;
  }

  public NewCourier name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Имя
   * @return name
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "name", description = "Имя", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NewCourier newCourier = (NewCourier) o;
    return Objects.equals(this.name, newCourier.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NewCourier {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(@Nullable Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

