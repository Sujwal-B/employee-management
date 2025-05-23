package de.zeroco.employeemanagement.controller;

import de.zeroco.employeemanagement.model.Project;
import de.zeroco.employeemanagement.service.ProjectService;
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
@RequestMapping("/api/v1/projects")
@Tag(name = "Project Controller", description = "APIs for managing projects. CRUD operations require ADMIN role, GET operations require USER or ADMIN role.")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project", description = "Creates a new project. Requires ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "201", description = "Project created successfully", content = @Content(schema = @Schema(implementation = Project.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid"),
                   @ApiResponse(responseCode = "403", description = "Forbidden, user does not have ADMIN role")
               })
    public ResponseEntity<Project> createProject(@Valid @RequestBody Project project) {
        Project createdProject = projectService.createProject(project);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a project by ID", description = "Retrieves a specific project by its ID. Requires USER or ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Project found", content = @Content(schema = @Schema(implementation = Project.class))),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid"),
                   @ApiResponse(responseCode = "404", description = "Project not found")
               })
    public ResponseEntity<Project> getProjectById(@Parameter(description = "ID of the project to be retrieved") @PathVariable Long id) {
        Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return ResponseEntity.ok(project);
    }

    @GetMapping
    @Operation(summary = "Get all projects", description = "Retrieves a list of all projects. Requires USER or ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "List of projects retrieved", content = @Content(schema = @Schema(implementation = List.class))),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid")
               })
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing project", description = "Updates an existing project's details by ID. Requires ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Project updated successfully", content = @Content(schema = @Schema(implementation = Project.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid"),
                   @ApiResponse(responseCode = "403", description = "Forbidden, user does not have ADMIN role"),
                   @ApiResponse(responseCode = "404", description = "Project not found")
               })
    public ResponseEntity<Project> updateProject(
            @Parameter(description = "ID of the project to be updated") @PathVariable Long id,
            @Valid @RequestBody Project projectDetails) {
        Project updatedProject = projectService.updateProject(id, projectDetails);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project", description = "Deletes a project by its ID. Requires ADMIN role.",
               responses = {
                   @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
                   @ApiResponse(responseCode = "401", description = "Unauthorized, token missing or invalid"),
                   @ApiResponse(responseCode = "403", description = "Forbidden, user does not have ADMIN role"),
                   @ApiResponse(responseCode = "404", description = "Project not found")
               })
    public ResponseEntity<Void> deleteProject(@Parameter(description = "ID of the project to be deleted") @PathVariable Long id) {
        projectService.getProjectById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
