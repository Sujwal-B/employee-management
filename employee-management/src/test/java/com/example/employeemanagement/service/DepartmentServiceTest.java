package com.example.employeemanagement.service;

import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.model.Department;
import com.example.employeemanagement.repository.DepartmentRepository;
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
public class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department1;
    private Department department2;

    @BeforeEach
    void setUp() {
        department1 = new Department(1L, "HR", null);
        department2 = new Department(2L, "Engineering", null);
    }

    @Test
    void createDepartment_shouldReturnSavedDepartment() {
        when(departmentRepository.save(any(Department.class))).thenReturn(department1);
        Department savedDepartment = departmentService.createDepartment(department1);
        assertNotNull(savedDepartment);
        assertEquals("HR", savedDepartment.getName());
        verify(departmentRepository, times(1)).save(department1);
    }

    @Test
    void getDepartmentById_whenFound_shouldReturnDepartment() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department1));
        Optional<Department> foundDepartment = departmentService.getDepartmentById(1L);
        assertTrue(foundDepartment.isPresent());
        assertEquals("HR", foundDepartment.get().getName());
        verify(departmentRepository, times(1)).findById(1L);
    }

    @Test
    void getDepartmentById_whenNotFound_shouldReturnEmptyOptional() {
        when(departmentRepository.findById(3L)).thenReturn(Optional.empty());
        Optional<Department> foundDepartment = departmentService.getDepartmentById(3L);
        assertFalse(foundDepartment.isPresent());
        verify(departmentRepository, times(1)).findById(3L);
    }

    @Test
    void getAllDepartments_shouldReturnListOfDepartments() {
        List<Department> departments = Arrays.asList(department1, department2);
        when(departmentRepository.findAll()).thenReturn(departments);
        List<Department> foundDepartments = departmentService.getAllDepartments();
        assertNotNull(foundDepartments);
        assertEquals(2, foundDepartments.size());
        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    void updateDepartment_whenFound_shouldReturnUpdatedDepartment() {
        Department updatedDetails = new Department(null, "Human Resources Updated", null);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department1));
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Department updatedDepartment = departmentService.updateDepartment(1L, updatedDetails);

        assertNotNull(updatedDepartment);
        assertEquals("Human Resources Updated", updatedDepartment.getName());
        assertEquals(1L, updatedDepartment.getId());
        verify(departmentRepository, times(1)).findById(1L);
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void updateDepartment_whenNotFound_shouldThrowResourceNotFoundException() {
        Department updatedDetails = new Department(null, "NonExistent Department", null);
        when(departmentRepository.findById(3L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            departmentService.updateDepartment(3L, updatedDetails);
        });

        assertEquals("Department not found with id: 3", exception.getMessage());
        verify(departmentRepository, times(1)).findById(3L);
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void deleteDepartment_whenFound_shouldDeleteDepartment() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department1));
        doNothing().when(departmentRepository).delete(department1);

        departmentService.deleteDepartment(1L);

        verify(departmentRepository, times(1)).findById(1L);
        verify(departmentRepository, times(1)).delete(department1);
    }

    @Test
    void deleteDepartment_whenNotFound_shouldThrowResourceNotFoundException() {
        when(departmentRepository.findById(3L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            departmentService.deleteDepartment(3L);
        });

        assertEquals("Department not found with id: 3", exception.getMessage());
        verify(departmentRepository, times(1)).findById(3L);
        verify(departmentRepository, never()).delete(any(Department.class));
    }
}
