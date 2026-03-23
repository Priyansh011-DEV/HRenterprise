package HR.enterprise.dto;

import HR.enterprise.entity.Role;
import lombok.Data;

@Data
public class EmployeeCreateRequest {

    private String name;
    private String email;
    private String department;
    private Double salary;

    private String username;
    private String password;
    private Role role;
}
