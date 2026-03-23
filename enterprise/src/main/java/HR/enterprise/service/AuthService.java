package HR.enterprise.service;

import HR.enterprise.dto.LoginRequest;
import HR.enterprise.dto.RegisterRequest;
import HR.enterprise.entity.Company;
import HR.enterprise.entity.Role;
import HR.enterprise.entity.User;
import HR.enterprise.repository.CompanyRepository;
import HR.enterprise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public String register(RegisterRequest request) {

        // Create Company
        Company company = Company.builder()
                .name(request.getCompanyName())
                .email(request.getCompanyEmail())
                .build();

        companyRepository.save(company);

        // Create User (Admin)
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .company(company)
                .build();

        userRepository.save(user);

        return "Registered successfully!";
    }
    public String login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return "Login successful!";
    }
}
