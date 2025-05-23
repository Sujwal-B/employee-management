package de.zeroco.employeemanagement.service;

import de.zeroco.employeemanagement.exception.DuplicateEmailException;
import de.zeroco.employeemanagement.exception.ResourceNotFoundException;
import de.zeroco.employeemanagement.model.Department;
import de.zeroco.employeemanagement.model.Employee;
import de.zeroco.employeemanagement.model.Project;
import de.zeroco.employeemanagement.repository.DepartmentRepository;
import de.zeroco.employeemanagement.repository.EmployeeRepository;
import de.zeroco.employeemanagement.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Transactional
    public Employee createEmployee(Employee employee) {
        // Validate Email uniqueness
        employeeRepository.findByEmail(employee.getEmail()).ifPresent(existingEmployee -> {
            throw new DuplicateEmailException("Email already exists: " + employee.getEmail());
        });

        // Validate Department
        if (employee.getDepartment() == null || employee.getDepartment().getId() == null) {
            throw new ResourceNotFoundException("Department ID must be provided and valid.");
        }
        Department department = departmentRepository.findById(employee.getDepartment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + employee.getDepartment().getId()));
        employee.setDepartment(department);


        // Validate Manager
        if (employee.getManager() != null && employee.getManager().getId() != null) {
            Employee manager = employeeRepository.findById(employee.getManager().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + employee.getManager().getId()));
            employee.setManager(manager);
        } else if (employee.getManager() != null && employee.getManager().getId() == null) {
            // If manager object is present but ID is null, it's an invalid state for creation
            employee.setManager(null); // Or throw an error if manager must be existing
        }


        // Validate and set Projects
        if (employee.getProjects() != null && !employee.getProjects().isEmpty()) {
            Set<Project> managedProjects = new HashSet<>();
            for (Project p : employee.getProjects()) {
                if (p.getId() != null) {
                    Project project = projectRepository.findById(p.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + p.getId()));
                    managedProjects.add(project);
                } else {
                    // Optionally handle creation of new projects if ID is null, or throw error
                    // For now, we'll assume projects must exist
                    throw new ResourceNotFoundException("Project ID must be provided for existing projects.");
                }
            }
            employee.setProjects(managedProjects);
        }

        return employeeRepository.save(employee);
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Validate Email uniqueness if email is being changed
        if (!employee.getEmail().equals(employeeDetails.getEmail())) {
            employeeRepository.findByEmail(employeeDetails.getEmail()).ifPresent(existingEmployee -> {
                throw new DuplicateEmailException("Email already exists: " + employeeDetails.getEmail());
            });
        }
        employee.setEmail(employeeDetails.getEmail());

        employee.setName(employeeDetails.getName());
        employee.setRole(employeeDetails.getRole());
        employee.setSalary(employeeDetails.getSalary());
        employee.setDateOfBirth(employeeDetails.getDateOfBirth());
        employee.setPhoneNumber(employeeDetails.getPhoneNumber());
        employee.setHireDate(employeeDetails.getHireDate());
        employee.setAddress(employeeDetails.getAddress());

        // Update Department
        if (employeeDetails.getDepartment() == null || employeeDetails.getDepartment().getId() == null) {
            throw new ResourceNotFoundException("Department ID must be provided and valid for update.");
        }
        Department department = departmentRepository.findById(employeeDetails.getDepartment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + employeeDetails.getDepartment().getId()));
        employee.setDepartment(department);


        // Update Manager
        if (employeeDetails.getManager() != null && employeeDetails.getManager().getId() != null) {
            Employee manager = employeeRepository.findById(employeeDetails.getManager().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + employeeDetails.getManager().getId()));
            employee.setManager(manager);
        } else {
            employee.setManager(null); // Allow unsetting manager
        }

        // Update Projects
        if (employeeDetails.getProjects() != null) {
            Set<Project> managedProjects = new HashSet<>();
            for (Project p : employeeDetails.getProjects()) {
                if (p.getId() != null) {
                    Project project = projectRepository.findById(p.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + p.getId()));
                    managedProjects.add(project);
                } else {
                     throw new ResourceNotFoundException("Project ID must be provided for existing projects when updating.");
                }
            }
            employee.setProjects(managedProjects);
        } else {
            employee.setProjects(new HashSet<>()); // Clear projects if null is passed
        }


        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        
        // Consider implications:
        // 1. If this employee is a manager to others, those employees' manager_id might need to be set to null.
        //    For now, we'll rely on database constraints (e.g., ON DELETE SET NULL) or handle this in a more advanced version.
        // 2. Employee-Project relationships in 'employee_project' table will be handled by JPA if CascadeType.REMOVE or orphanRemoval=true is on Project side,
        //    or manually if needed. Current setup @ManyToMany doesn't automatically remove project associations upon employee deletion unless projects are explicitly removed from employee.getProjects() then saved.
        //    The current @ManyToMany doesn't specify cascade remove, so join table entries will be removed.

        employeeRepository.delete(employee);
    }

    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }
}
