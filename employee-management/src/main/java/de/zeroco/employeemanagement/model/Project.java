package de.zeroco.employeemanagement.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a project within the organization.")
@JsonIgnoreProperties({"employees"}) // To avoid circular dependency in serialization if employees are fetched
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the project.", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Schema(description = "Name of the project.", example = "New Website Development", required = true)
    private String name;

    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    @Schema(description = "Set of employees assigned to this project.", accessMode = Schema.AccessMode.READ_ONLY)
    private Set<Employee> employees = new java.util.HashSet<>();
}
