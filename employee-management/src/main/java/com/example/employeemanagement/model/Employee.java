package com.example.employeemanagement.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents an employee in the organization.")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the employee.", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Schema(description = "Full name of the employee.", example = "John Doe", required = true)
    private String name;

    @NotBlank
    @Schema(description = "Role or position of the employee.", example = "Software Engineer", required = true)
    private String role;

    @NotNull
    @Min(0)
    @Schema(description = "Salary of the employee.", example = "60000.00", required = true)
    private Double salary;

    @NotNull
    @Past
    @Schema(description = "Date of birth of the employee.", example = "1990-01-15", required = true)
    private LocalDate dateOfBirth;

    @Column(unique = true)
    @NotBlank
    @Email
    @Schema(description = "Email address of the employee, must be unique.", example = "john.doe@example.com", required = true)
    private String email;

    @Schema(description = "Phone number of the employee.", example = "123-456-7890")
    private String phoneNumber;

    @NotNull
    @PastOrPresent
    @Schema(description = "Date when the employee was hired.", example = "2021-06-01", required = true)
    private LocalDate hireDate;

    @Schema(description = "Residential address of the employee.", example = "123 Main St, Anytown, USA")
    private String address;

    @NotNull // Enforced at service layer for creation/update
    @ManyToOne
    @JoinColumn(name = "department_id")
    @Schema(description = "Department to which the employee belongs. Must provide department ID for new/updated employees.", required = true)
    private Department department;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    @Schema(description = "Manager of the employee. Can be null. Provide manager's employee ID if applicable.")
    private Employee manager;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "employee_project",
            joinColumns = { @JoinColumn(name = "employee_id") },
            inverseJoinColumns = { @JoinColumn(name = "project_id") })
    @Schema(description = "Set of projects the employee is assigned to. Provide project IDs for new/updated assignments.")
    private Set<Project> projects = new java.util.HashSet<>();
}
