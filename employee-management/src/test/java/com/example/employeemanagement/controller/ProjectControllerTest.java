package com.example.employeemanagement.controller;

import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.model.Project;
import com.example.employeemanagement.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(ProjectController.class)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    private Project project1;
    private Project project2;

    @BeforeEach
    void setUp() {
        project1 = new Project(1L, "Alpha Project", null);
        project2 = new Project(2L, "Beta Project", null);
    }

    @Test
    void createProject_validInput_shouldReturnCreated() throws Exception {
        when(projectService.createProject(any(Project.class))).thenReturn(project1);

        mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(project1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Alpha Project")));

        verify(projectService, times(1)).createProject(any(Project.class));
    }
    
    @Test
    void createProject_invalidInput_shouldReturnBadRequest() throws Exception {
        Project invalidProject = new Project(null, "P", null); // Name too short

        mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProject)))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).createProject(any(Project.class));
    }

    @Test
    void getProjectById_whenFound_shouldReturnProject() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(Optional.of(project1));

        mockMvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Alpha Project")));

        verify(projectService, times(1)).getProjectById(1L);
    }

    @Test
    void getProjectById_whenNotFound_shouldReturnNotFound() throws Exception {
        when(projectService.getProjectById(3L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/projects/3"))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).getProjectById(3L);
    }

    @Test
    void getAllProjects_shouldReturnListOfProjects() throws Exception {
        List<Project> projects = Arrays.asList(project1, project2);
        when(projectService.getAllProjects()).thenReturn(projects);

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Alpha Project")))
                .andExpect(jsonPath("$[1].name", is("Beta Project")));

        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    void updateProject_whenFound_shouldReturnUpdatedProject() throws Exception {
        Project updatedDetails = new Project(null, "Alpha Project Updated", null);
        Project returnedProject = new Project(1L, "Alpha Project Updated", null);

        when(projectService.updateProject(eq(1L), any(Project.class))).thenReturn(returnedProject);

        mockMvc.perform(put("/api/v1/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Alpha Project Updated")));

        verify(projectService, times(1)).updateProject(eq(1L), any(Project.class));
    }

    @Test
    void updateProject_whenNotFound_shouldReturnNotFound() throws Exception {
        Project updatedDetails = new Project(null, "NonExistent Project", null);
        when(projectService.updateProject(eq(3L), any(Project.class)))
                .thenThrow(new ResourceNotFoundException("Project not found with id: 3"));

        mockMvc.perform(put("/api/v1/projects/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).updateProject(eq(3L), any(Project.class));
    }
    
    @Test
    void updateProject_invalidInput_shouldReturnBadRequest() throws Exception {
        Project invalidProject = new Project(null, "P", null); // Name too short

        mockMvc.perform(put("/api/v1/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProject)))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).updateProject(anyLong(), any(Project.class));
    }

    @Test
    void deleteProject_whenFound_shouldReturnNoContent() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(Optional.of(project1));
        doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/v1/projects/1"))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).getProjectById(1L);
        verify(projectService, times(1)).deleteProject(1L);
    }

    @Test
    void deleteProject_whenNotFound_shouldReturnNotFound() throws Exception {
        when(projectService.getProjectById(3L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/projects/3"))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).getProjectById(3L);
        verify(projectService, never()).deleteProject(3L);
    }
}
