package HR.enterprise.dto;

import lombok.Data;

@Data
public class EmployeeCreateRequest {

    private String name;
    private String email;
    private String department;
    private Double salary;

    private String username;
    private String password;
}
