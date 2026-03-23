package HR.enterprise.controller;

import HR.enterprise.dto.EmployeeCreateRequest;
import HR.enterprise.entity.Employee;
import HR.enterprise.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;





    @PostMapping
    public ResponseEntity<Employee> CReateEmployee(
            @RequestBody EmployeeCreateRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        Employee employee = employeeService.createEmployee(request, username);

        return ResponseEntity.ok(employee);
    }




    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees(Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(employeeService.getAllEmployees(username));
    }





    @GetMapping("/me")
    public ResponseEntity<Employee> getMyProfile(Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(employeeService.getMyProfile(username));
    }








    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long id,
            @RequestBody Employee employee,
            Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                employeeService.updateEmployee(id, employee, username)
        );
    }






    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();

        employeeService.deleteEmployee(id, username);

        return ResponseEntity.ok("Employee deleted successfully");
    }
}
