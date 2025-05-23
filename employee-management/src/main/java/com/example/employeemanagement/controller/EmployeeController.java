package com.example.employeemanagement.controller;

import com.example.employeemanagement.model.Employee;
import com.example.employeemanagement.service.EmployeeService;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.employeemanagement.model.Employee;
import com.example.employeemanagement.service.EmployeeService;
import com.example.employeemanagement.exception.ResourceNotFoundException;
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

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Controller", description = "APIs for managing employees. CRUD operations require ADMIN role, GET operations require USER or ADMIN role.")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    @Operation(summary = "Create a new employee", description = "Creates a new employee. Requires ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "201", description = "Employee created successfully", content = @Content(schema = @Schema(implementation = Employee.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid input, validation error, or email already exists"),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid"),
                   @ApiResponse(responseCode = "403", description = "Forbidden, user does not have ADMIN role"),
                   @ApiResponse(responseCode = "404", description = "Related entity (Department/Manager) not found")
               })
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        Employee createdEmployee = employeeService.createEmployee(employee);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an employee by ID", description = "Retrieves a specific employee by their ID. Requires USER or ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Employee found", content = @Content(schema = @Schema(implementation = Employee.class))),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid"),
                   @ApiResponse(responseCode = "404", description = "Employee not found")
               })
    public ResponseEntity<Employee> getEmployeeById(@Parameter(description = "ID of the employee to be retrieved") @PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return ResponseEntity.ok(employee);
    }

    @GetMapping
    @Operation(summary = "Get all employees", description = "Retrieves a list of all employees. Requires USER or ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "List of employees retrieved", content = @Content(schema = @Schema(implementation = List.class))),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid")
               })
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing employee", description = "Updates an existing employee's details by ID. Requires ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Employee updated successfully", content = @Content(schema = @Schema(implementation = Employee.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid"),
                   @ApiResponse(responseCode = "403", description = "Forbidden, user does not have ADMIN role"),
                   @ApiResponse(responseCode = "404", description = "Employee or related entity (Department/Manager) not found")
               })
    public ResponseEntity<Employee> updateEmployee(
            @Parameter(description = "ID of the employee to be updated") @PathVariable Long id,
            @Valid @RequestBody Employee employeeDetails) {
        Employee updatedEmployee = employeeService.updateEmployee(id, employeeDetails);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee", description = "Deletes an employee by their ID. Requires ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid"),
                   @ApiResponse(responseCode = "403", description = "Forbidden, user does not have ADMIN role"),
                   @ApiResponse(responseCode = "404", description = "Employee not found")
               })
    public ResponseEntity<Void> deleteEmployee(@Parameter(description = "ID of the employee to be deleted") @PathVariable Long id) {
        employeeService.getEmployeeById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get an employee by email", description = "Retrieves a specific employee by their email address. Requires USER or ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Employee found", content = @Content(schema = @Schema(implementation = Employee.class))),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid"),
                   @ApiResponse(responseCode = "404", description = "Employee not found with the given email")
               })
    public ResponseEntity<Employee> getEmployeeByEmail(@Parameter(description = "Email of the employee to be retrieved") @PathVariable String email) {
        Employee employee = employeeService.getEmployeeByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));
        return ResponseEntity.ok(employee);
    }
}
