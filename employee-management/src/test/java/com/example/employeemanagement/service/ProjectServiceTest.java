package com.example.employeemanagement.service;

import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.model.Project;
import com.example.employeemanagement.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project project1;
    private Project project2;

    @BeforeEach
    void setUp() {
        project1 = new Project(1L, "Alpha Project", null);
        project2 = new Project(2L, "Beta Project", null);
    }

    @Test
    void createProject_shouldReturnSavedProject() {
        when(projectRepository.save(any(Project.class))).thenReturn(project1);
        Project savedProject = projectService.createProject(project1);
        assertNotNull(savedProject);
        assertEquals("Alpha Project", savedProject.getName());
        verify(projectRepository, times(1)).save(project1);
    }

    @Test
    void getProjectById_whenFound_shouldReturnProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
        Optional<Project> foundProject = projectService.getProjectById(1L);
        assertTrue(foundProject.isPresent());
        assertEquals("Alpha Project", foundProject.get().getName());
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    void getProjectById_whenNotFound_shouldReturnEmptyOptional() {
        when(projectRepository.findById(3L)).thenReturn(Optional.empty());
        Optional<Project> foundProject = projectService.getProjectById(3L);
        assertFalse(foundProject.isPresent());
        verify(projectRepository, times(1)).findById(3L);
    }

    @Test
    void getAllProjects_shouldReturnListOfProjects() {
        List<Project> projects = Arrays.asList(project1, project2);
        when(projectRepository.findAll()).thenReturn(projects);
        List<Project> foundProjects = projectService.getAllProjects();
        assertNotNull(foundProjects);
        assertEquals(2, foundProjects.size());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void updateProject_whenFound_shouldReturnUpdatedProject() {
        Project updatedDetails = new Project(null, "Alpha Project Updated", null);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project updatedProject = projectService.updateProject(1L, updatedDetails);

        assertNotNull(updatedProject);
        assertEquals("Alpha Project Updated", updatedProject.getName());
        assertEquals(1L, updatedProject.getId());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void updateProject_whenNotFound_shouldThrowResourceNotFoundException() {
        Project updatedDetails = new Project(null, "NonExistent Project", null);
        when(projectRepository.findById(3L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            projectService.updateProject(3L, updatedDetails);
        });

        assertEquals("Project not found with id: 3", exception.getMessage());
        verify(projectRepository, times(1)).findById(3L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void deleteProject_whenFound_shouldDeleteProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
        doNothing().when(projectRepository).delete(project1);

        projectService.deleteProject(1L);

        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).delete(project1);
    }

    @Test
    void deleteProject_whenNotFound_shouldThrowResourceNotFoundException() {
        when(projectRepository.findById(3L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            projectService.deleteProject(3L);
        });

        assertEquals("Project not found with id: 3", exception.getMessage());
        verify(projectRepository, times(1)).findById(3L);
        verify(projectRepository, never()).delete(any(Project.class));
    }
}
