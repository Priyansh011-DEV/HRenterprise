package HR.enterprise.service;

import HR.enterprise.dto.EmployeeCreateRequest;
import HR.enterprise.entity.Employee;
import HR.enterprise.entity.Role;
import HR.enterprise.entity.User;
import HR.enterprise.repository.EmployeeRepository;
import HR.enterprise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public Employee CreateEmployee(EmployeeCreateRequest request, String adminUsername) {

        // Step 1: get admin
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Step 2: role check
        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only ADMIN can create employees");
        }

        // Step 3: create User (EMPLOYEE role)
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
            User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.EMPLOYEE)
                .company(admin.getCompany())
                .build();

        userRepository.save(user);

        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        // Step 4: create Employee
        Employee employee = Employee.builder()
                .name(request.getName())
                .email(request.getEmail())
                .department(request.getDepartment())
                .salary(request.getSalary())
                .company(admin.getCompany())
                .user(user)
                .build();

        return employeeRepository.save(employee);
    }







    public List<Employee> getAllEmployees(String username) {

        // Step 1: get logged-in user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: role-based logic
        if (user.getRole() == Role.EMPLOYEE) {

            // Employee can only see their own data
            if (user.getEmployee() == null) {
                throw new RuntimeException("Employee profile not found");
            }

            return List.of(user.getEmployee());
        }

        // ADMIN / HR → see all employees of their company
        return employeeRepository.findByCompany(user.getCompany());
    }




    public Employee getMyProfile(String username) {

        // Step 1: get logged-in user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: check employee mapping
        if (user.getEmployee() == null) {
            throw new RuntimeException("Employee profile not found");
        }

        return user.getEmployee();
    }







    public void deleteEmployee(Long employeeId, String username) {

        // Step 1: get logged-in user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: role check
        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only ADMIN can delete employees");
        }

        // Step 3: fetch employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Step 4: company validation
        if (!employee.getCompany().getId().equals(user.getCompany().getId())) {
            throw new RuntimeException("You cannot delete employees from another company");
        }

        // Step 5: delete
        employeeRepository.delete(employee);
    }





    public Employee updateEmployee(Long employeeId, Employee updatedData, String username) {

        // Step 1: get logged-in user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: fetch employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Step 3: company validation (VERY IMPORTANT)
        if (!employee.getCompany().getId().equals(user.getCompany().getId())) {
            throw new RuntimeException("You cannot update employees from another company");
        }

        // Step 4: role-based check
        if (user.getRole() == Role.EMPLOYEE &&
                !employee.getId().equals(user.getEmployee().getId())) {
            throw new RuntimeException("Employees can only update their own profile");
        }

        // Step 5: update allowed fields only (no overwrite)
        if (updatedData.getName() != null) {
            employee.setName(updatedData.getName());
        }

        if (updatedData.getEmail() != null) {
            employee.setEmail(updatedData.getEmail());
        }

        if (updatedData.getDepartment() != null) {
            employee.setDepartment(updatedData.getDepartment());
        }

        if (updatedData.getSalary() != null) {
            employee.setSalary(updatedData.getSalary());
        }

        // Step 6: save
        return employeeRepository.save(employee);
    }
}
