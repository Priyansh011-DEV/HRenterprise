package HR.enterprise.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String companyName;
    private String companyEmail;

    private String username;
    private String password;
}