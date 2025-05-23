package de.zeroco.employeemanagement.controller;

import de.zeroco.employeemanagement.model.Department;
import de.zeroco.employeemanagement.service.DepartmentService;
import de.zeroco.employeemanagement.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@Tag(name = "Department Controller", description = "APIs for managing departments. CRUD operations require ADMIN role, GET operations require USER or ADMIN role.")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PostMapping
    @Operation(summary = "Create a new department", description = "Creates a new department. Requires ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "201", description = "Department created successfully", content = @Content(schema = @Schema(implementation = Department.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid"),
                   @ApiResponse(responseCode = "403", description = "Forbidden, user does not have ADMIN role")
               })
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department) {
        Department createdDepartment = departmentService.createDepartment(department);
        return new ResponseEntity<>(createdDepartment, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a department by ID", description = "Retrieves a specific department by its ID. Requires USER or ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Department found", content = @Content(schema = @Schema(implementation = Department.class))),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid"),
                   @ApiResponse(responseCode = "404", description = "Department not found")
               })
    public ResponseEntity<Department> getDepartmentById(@Parameter(description = "ID of the department to be retrieved") @PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return ResponseEntity.ok(department);
    }

    @GetMapping
    @Operation(summary = "Get all departments", description = "Retrieves a list of all departments. Requires USER or ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "List of departments retrieved", content = @Content(schema = @Schema(implementation = List.class))),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid")
               })
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing department", description = "Updates an existing department's details by ID. Requires ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Department updated successfully", content = @Content(schema = @Schema(implementation = Department.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid"),
                   @ApiResponse(responseCode = "403", description = "Forbidden, user does not have ADMIN role"),
                   @ApiResponse(responseCode = "404", description = "Department not found")
               })
    public ResponseEntity<Department> updateDepartment(
            @Parameter(description = "ID of the department to be updated") @PathVariable Long id,
            @Valid @RequestBody Department departmentDetails) {
        Department updatedDepartment = departmentService.updateDepartment(id, departmentDetails);
        return ResponseEntity.ok(updatedDepartment);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a department", description = "Deletes a department by its ID. Requires ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "204", description = "Department deleted successfully"),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid"),
                   @ApiResponse(responseCode = "403", description = "Forbidden, user does not have ADMIN role"),
                   @ApiResponse(responseCode = "404", description = "Department not found")
               })
    public ResponseEntity<Void> deleteDepartment(@Parameter(description = "ID of the department to be deleted") @PathVariable Long id) {
        departmentService.getDepartmentById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
