package de.zeroco.employeemanagement.controller;

import de.zeroco.employeemanagement.exception.DuplicateEmailException;
import de.zeroco.employeemanagement.exception.ResourceNotFoundException;
import de.zeroco.employeemanagement.model.Department;
import de.zeroco.employeemanagement.model.Employee;
import de.zeroco.employeemanagement.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee1;
    private Employee employee2;
    private Department department1;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule()); // Important for LocalDate
        department1 = new Department(1L, "HR", new HashSet<>());
        employee1 = new Employee(1L, "John Doe", "Developer", 60000.0, LocalDate.of(1990, 1, 1),
                "john.doe@example.com", "1234567890", LocalDate.of(2022, 1, 1), "123 Main St",
                department1, null, new HashSet<>());
        employee2 = new Employee(2L, "Jane Smith", "Analyst", 70000.0, LocalDate.of(1992, 2, 2),
                "jane.smith@example.com", "0987654321", LocalDate.of(2021, 2, 2), "456 Oak St",
                department1, null, new HashSet<>());
    }

    @Test
    void createEmployee_validInput_shouldReturnCreated() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee1);

        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }
    
    @Test
    void createEmployee_invalidInput_nameBlank_shouldReturnBadRequest() throws Exception {
        Employee invalidEmployee = new Employee(null, "", "Developer", 60000.0, LocalDate.of(1990,1,1), "test@example.com", "123", LocalDate.now(), "Addr", department1, null, null);
        
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest()); // Validation triggered by @Valid

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    void createEmployee_duplicateEmail_shouldReturnConflict() throws Exception {
        when(employeeService.createEmployee(any(Employee.class)))
            .thenThrow(new DuplicateEmailException("Email already exists: " + employee1.getEmail()));

        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee1)))
                .andExpect(status().isConflict()); // DuplicateEmailException is mapped to 409

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }
    
    @Test
    void createEmployee_departmentNotFound_shouldReturnNotFound() throws Exception {
        when(employeeService.createEmployee(any(Employee.class)))
            .thenThrow(new ResourceNotFoundException("Department not found with id: " + department1.getId()));

        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee1)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }


    @Test
    void getEmployeeById_whenFound_shouldReturnEmployee() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(employee1));

        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    void getEmployeeById_whenNotFound_shouldReturnNotFound() throws Exception {
        when(employeeService.getEmployeeById(3L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/employees/3"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById(3L);
    }
    
    @Test
    void getAllEmployees_shouldReturnListOfEmployees() throws Exception {
        List<Employee> employees = Arrays.asList(employee1, employee2);
        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[1].name", is("Jane Smith")));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void updateEmployee_whenFound_shouldReturnUpdatedEmployee() throws Exception {
        Employee updatedDetails = new Employee(null, "Johnathan Doe", "Senior Developer", 65000.0, employee1.getDateOfBirth(),
                employee1.getEmail(), employee1.getPhoneNumber(), employee1.getHireDate(), "New Address", department1, null, null);
        
        Employee returnedEmployee = new Employee(1L, "Johnathan Doe", "Senior Developer", 65000.0, employee1.getDateOfBirth(),
                employee1.getEmail(), employee1.getPhoneNumber(), employee1.getHireDate(), "New Address", department1, null, null);


        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(returnedEmployee);

        mockMvc.perform(put("/api/v1/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Johnathan Doe")));

        verify(employeeService, times(1)).updateEmployee(eq(1L), any(Employee.class));
    }

    @Test
    void updateEmployee_whenNotFound_shouldReturnNotFound() throws Exception {
        Employee updatedDetails = new Employee(null, "NonExistent", "Role", 0.0, LocalDate.now(), "non@ex.com", "", LocalDate.now(), "", department1, null, null);
        when(employeeService.updateEmployee(eq(3L), any(Employee.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: 3"));

        mockMvc.perform(put("/api/v1/employees/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).updateEmployee(eq(3L), any(Employee.class));
    }

    @Test
    void deleteEmployee_whenFound_shouldReturnNoContent() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(employee1)); // For controller's pre-check
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).getEmployeeById(1L);
        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    void deleteEmployee_whenNotFound_shouldReturnNotFound() throws Exception {
        when(employeeService.getEmployeeById(3L)).thenReturn(Optional.empty()); // For controller's pre-check

        mockMvc.perform(delete("/api/v1/employees/3"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById(3L);
        verify(employeeService, never()).deleteEmployee(3L);
    }

    @Test
    void getEmployeeByEmail_whenFound_shouldReturnEmployee() throws Exception {
        when(employeeService.getEmployeeByEmail("john.doe@example.com")).thenReturn(Optional.of(employee1));

        mockMvc.perform(get("/api/v1/employees/email/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")));

        verify(employeeService, times(1)).getEmployeeByEmail("john.doe@example.com");
    }

    @Test
    void getEmployeeByEmail_whenNotFound_shouldReturnNotFound() throws Exception {
        when(employeeService.getEmployeeByEmail("non.existent@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/employees/email/non.existent@example.com"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeByEmail("non.existent@example.com");
    }
}
