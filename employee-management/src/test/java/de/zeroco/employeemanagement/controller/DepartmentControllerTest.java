package de.zeroco.employeemanagement.controller;

import de.zeroco.employeemanagement.exception.ResourceNotFoundException;
import de.zeroco.employeemanagement.model.Department;
import de.zeroco.employeemanagement.service.DepartmentService;
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

@WebMvcTest(DepartmentController.class)
public class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Department department1;
    private Department department2;

    @BeforeEach
    void setUp() {
        department1 = new Department(1L, "HR", null);
        department2 = new Department(2L, "Engineering", null);
    }

    @Test
    void createDepartment_validInput_shouldReturnCreated() throws Exception {
        when(departmentService.createDepartment(any(Department.class))).thenReturn(department1);

        mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(department1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("HR")));

        verify(departmentService, times(1)).createDepartment(any(Department.class));
    }

    @Test
    void createDepartment_invalidInput_shouldReturnBadRequest() throws Exception {
        Department invalidDepartment = new Department(null, "H", null); // Name too short

        mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDepartment)))
                .andExpect(status().isBadRequest()); // Assuming @Valid is handled by GlobalExceptionHandler

        verify(departmentService, never()).createDepartment(any(Department.class));
    }


    @Test
    void getDepartmentById_whenFound_shouldReturnDepartment() throws Exception {
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department1));

        mockMvc.perform(get("/api/v1/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("HR")));

        verify(departmentService, times(1)).getDepartmentById(1L);
    }

    @Test
    void getDepartmentById_whenNotFound_shouldReturnNotFound() throws Exception {
        when(departmentService.getDepartmentById(3L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/departments/3"))
                .andExpect(status().isNotFound()); // Handled by ResourceNotFoundException -> GlobalExceptionHandler

        verify(departmentService, times(1)).getDepartmentById(3L);
    }

    @Test
    void getAllDepartments_shouldReturnListOfDepartments() throws Exception {
        List<Department> departments = Arrays.asList(department1, department2);
        when(departmentService.getAllDepartments()).thenReturn(departments);

        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("HR")))
                .andExpect(jsonPath("$[1].name", is("Engineering")));

        verify(departmentService, times(1)).getAllDepartments();
    }

    @Test
    void updateDepartment_whenFound_shouldReturnUpdatedDepartment() throws Exception {
        Department updatedDetails = new Department(null, "Human Resources Updated", null);
        // Service's updateDepartment method should handle setting the ID correctly
        Department returnedDepartment = new Department(1L, "Human Resources Updated", null);


        when(departmentService.updateDepartment(eq(1L), any(Department.class))).thenReturn(returnedDepartment);

        mockMvc.perform(put("/api/v1/departments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Human Resources Updated")));

        verify(departmentService, times(1)).updateDepartment(eq(1L), any(Department.class));
    }

    @Test
    void updateDepartment_whenNotFound_shouldReturnNotFound() throws Exception {
        Department updatedDetails = new Department(null, "NonExistent Dept", null);
        when(departmentService.updateDepartment(eq(3L), any(Department.class)))
                .thenThrow(new ResourceNotFoundException("Department not found with id: 3"));

        mockMvc.perform(put("/api/v1/departments/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());

        verify(departmentService, times(1)).updateDepartment(eq(3L), any(Department.class));
    }
    
    @Test
    void updateDepartment_invalidInput_shouldReturnBadRequest() throws Exception {
        Department invalidDepartment = new Department(null, "U", null); // Name too short

        mockMvc.perform(put("/api/v1/departments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDepartment)))
                .andExpect(status().isBadRequest());

        verify(departmentService, never()).updateDepartment(anyLong(), any(Department.class));
    }


    @Test
    void deleteDepartment_whenFound_shouldReturnNoContent() throws Exception {
        // Mock getDepartmentById for the pre-check in controller
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department1)); 
        doNothing().when(departmentService).deleteDepartment(1L);

        mockMvc.perform(delete("/api/v1/departments/1"))
                .andExpect(status().isNoContent());

        verify(departmentService, times(1)).getDepartmentById(1L); // Controller pre-check
        verify(departmentService, times(1)).deleteDepartment(1L);
    }

    @Test
    void deleteDepartment_whenNotFound_shouldReturnNotFound() throws Exception {
        // Mock getDepartmentById for the pre-check in controller
        when(departmentService.getDepartmentById(3L))
                .thenReturn(Optional.empty());
                //.thenThrow(new ResourceNotFoundException("Department not found with id: 3"));

        mockMvc.perform(delete("/api/v1/departments/3"))
                .andExpect(status().isNotFound());

        verify(departmentService, times(1)).getDepartmentById(3L); // Controller pre-check
        verify(departmentService, never()).deleteDepartment(3L);
    }
}
