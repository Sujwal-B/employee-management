package com.example.employeemanagement.service;

import com.example.employeemanagement.exception.DuplicateEmailException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.model.Department;
import com.example.employeemanagement.model.Employee;
import com.example.employeemanagement.model.Project;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee1;
    private Employee employee2;
    private Department department1;
    private Project project1;
    private Employee manager;


    @BeforeEach
    void setUp() {
        department1 = new Department(1L, "HR", new HashSet<>());
        project1 = new Project(1L, "HR Project", new HashSet<>());
        manager = new Employee(10L, "Manager Person", "Manager", 90000.0, LocalDate.of(1970, 1, 1), "manager@example.com", "111", LocalDate.of(2000,1,1), "Addr", department1, null, new HashSet<>());


        employee1 = new Employee(1L, "John Doe", "Developer", 60000.0, LocalDate.of(1990, 1, 1),
                "john.doe@example.com", "1234567890", LocalDate.of(2022, 1, 1), "123 Main St",
                department1, manager, Collections.singleton(project1));

        employee2 = new Employee(2L, "Jane Smith", "Analyst", 70000.0, LocalDate.of(1992, 2, 2),
                "jane.smith@example.com", "0987654321", LocalDate.of(2021, 2, 2), "456 Oak St",
                department1, null, new HashSet<>());
    }

    @Test
    void createEmployee_success() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department1));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(manager)); // For manager
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee1);

        Employee created = employeeService.createEmployee(employee1);

        assertNotNull(created);
        assertEquals("john.doe@example.com", created.getEmail());
        verify(employeeRepository).save(employee1);
    }

    @Test
    void createEmployee_duplicateEmail_shouldThrowDuplicateEmailException() {
        when(employeeRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(employee2)); // Different employee with same email

        DuplicateEmailException exception = assertThrows(DuplicateEmailException.class, () -> {
            employeeService.createEmployee(employee1);
        });
        assertEquals("Email already exists: john.doe@example.com", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void createEmployee_departmentNotFound_shouldThrowResourceNotFoundException() {
        employee1.setManager(null); // Simplify test
        employee1.setProjects(new HashSet<>()); // Simplify test
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.createEmployee(employee1);
        });
        assertEquals("Department not found with id: 1", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }
    
    @Test
    void createEmployee_nullDepartmentId_shouldThrowResourceNotFoundException() {
        employee1.setDepartment(new Department(null, "New Dept", null)); // Department with null ID
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.createEmployee(employee1);
        });
        assertEquals("Department ID must be provided and valid.", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }


    @Test
    void createEmployee_managerNotFound_shouldThrowResourceNotFoundException() {
        employee1.setProjects(new HashSet<>()); // Simplify test
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department1));
        when(employeeRepository.findById(10L)).thenReturn(Optional.empty()); // Manager not found

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.createEmployee(employee1);
        });
        assertEquals("Manager not found with id: 10", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }
    
    @Test
    void createEmployee_projectNotFound_shouldThrowResourceNotFoundException() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department1));
        if (employee1.getManager() != null && employee1.getManager().getId() != null) {
            when(employeeRepository.findById(employee1.getManager().getId())).thenReturn(Optional.of(manager));
        }
        // Assuming employee1 has a project with ID 1L that's not found
        when(projectRepository.findById(1L)).thenReturn(Optional.empty()); 

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.createEmployee(employee1);
        });
        assertEquals("Project not found with id: 1", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }


    @Test
    void getEmployeeById_whenFound_shouldReturnEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee1));
        Optional<Employee> found = employeeService.getEmployeeById(1L);
        assertTrue(found.isPresent());
        assertEquals("john.doe@example.com", found.get().getEmail());
    }

    @Test
    void getEmployeeById_whenNotFound_shouldReturnEmptyOptional() {
        when(employeeRepository.findById(3L)).thenReturn(Optional.empty());
        Optional<Employee> found = employeeService.getEmployeeById(3L);
        assertFalse(found.isPresent());
    }
    
    @Test
    void getAllEmployees_shouldReturnListOfEmployees() {
        List<Employee> employees = Arrays.asList(employee1, employee2);
        when(employeeRepository.findAll()).thenReturn(employees);
        List<Employee> foundEmployees = employeeService.getAllEmployees();
        assertNotNull(foundEmployees);
        assertEquals(2, foundEmployees.size());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void updateEmployee_success() {
        Employee existingEmployee = new Employee(1L, "Old Name", "Old Role", 50000.0, LocalDate.now().minusYears(30), "original.email@example.com", "123", LocalDate.now().minusYears(2), "Old Addr", department1, null, new HashSet<>());
        Employee detailsToUpdate = new Employee(null, "New Name", "New Role", 60000.0, LocalDate.now().minusYears(25), "new.email@example.com", "456", LocalDate.now().minusYears(1), "New Addr", department1, manager, Collections.singleton(project1));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.findByEmail("new.email@example.com")).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department1));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(manager));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee updated = employeeService.updateEmployee(1L, detailsToUpdate);

        assertNotNull(updated);
        assertEquals("New Name", updated.getName());
        assertEquals("new.email@example.com", updated.getEmail());
        assertEquals(manager, updated.getManager());
        assertTrue(updated.getProjects().contains(project1));
        verify(employeeRepository).save(existingEmployee);
    }
    
    @Test
    void updateEmployee_whenEmployeeNotFound_shouldThrowResourceNotFound() {
        Employee detailsToUpdate = new Employee(null, "New Name", "New Role", 60000.0, LocalDate.now().minusYears(25), "new.email@example.com", "456", LocalDate.now().minusYears(1), "New Addr", department1, null, new HashSet<>());
        when(employeeRepository.findById(3L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(3L, detailsToUpdate);
        });
        assertEquals("Employee not found with id: 3", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void updateEmployee_duplicateEmail_shouldThrowDuplicateEmailException() {
        Employee existingEmployee = new Employee(1L, "Old Name", "Old Role", 50000.0, LocalDate.now().minusYears(30), "original.email@example.com", "123", LocalDate.now().minusYears(2), "Old Addr", department1, null, new HashSet<>());
        Employee detailsToUpdate = new Employee(null, "New Name", "New Role", 60000.0, LocalDate.now().minusYears(25), "jane.smith@example.com", "456", LocalDate.now().minusYears(1), "New Addr", department1, null, new HashSet<>());
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.findByEmail("jane.smith@example.com")).thenReturn(Optional.of(employee2)); // employee2 has this email and id 2L

        DuplicateEmailException exception = assertThrows(DuplicateEmailException.class, () -> {
            employeeService.updateEmployee(1L, detailsToUpdate);
        });
        assertEquals("Email already exists: jane.smith@example.com", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }
    
    @Test
    void updateEmployee_departmentNotFound_shouldThrowResourceNotFoundException() {
        Employee existingEmployee = new Employee(1L, "Old Name", "Old Role", 50000.0, LocalDate.now().minusYears(30), "original.email@example.com", "123", LocalDate.now().minusYears(2), "Old Addr", department1, null, new HashSet<>());
        Department departmentNotFound = new Department(99L, "NonExistent", null);
        Employee detailsToUpdate = new Employee(null, "New Name", "New Role", 60000.0, LocalDate.now().minusYears(25), "original.email@example.com", "456", LocalDate.now().minusYears(1), "New Addr", departmentNotFound, null, new HashSet<>());

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(1L, detailsToUpdate);
        });
        assertEquals("Department not found with id: 99", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }


    @Test
    void deleteEmployee_whenFound_shouldDeleteEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee1));
        doNothing().when(employeeRepository).delete(employee1);
        employeeService.deleteEmployee(1L);
        verify(employeeRepository).delete(employee1);
    }

    @Test
    void deleteEmployee_whenNotFound_shouldThrowResourceNotFoundException() {
        when(employeeRepository.findById(3L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(3L);
        });
        assertEquals("Employee not found with id: 3", exception.getMessage());
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    void getEmployeeByEmail_whenFound_shouldReturnEmployee() {
        when(employeeRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(employee1));
        Optional<Employee> found = employeeService.getEmployeeByEmail("john.doe@example.com");
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
    }

    @Test
    void getEmployeeByEmail_whenNotFound_shouldReturnEmptyOptional() {
        when(employeeRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        Optional<Employee> found = employeeService.getEmployeeByEmail("nonexistent@example.com");
        assertFalse(found.isPresent());
    }
}
