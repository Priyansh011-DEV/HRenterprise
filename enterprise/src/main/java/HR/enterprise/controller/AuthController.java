package HR.enterprise.controller;

import HR.enterprise.dto.LoginRequest;
import HR.enterprise.dto.RegisterRequest;
import HR.enterprise.entity.User;
import HR.enterprise.repository.UserRepository;
import HR.enterprise.service.AuthService;
import HR.enterprise.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final UserRepository userRepository;
    // remove SecurityConfig injection entirely

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 🔥 GET USER FROM DB
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔥 PASS USER (NOT STRING)
        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(token);
    }

    @GetMapping("/test")
    public String test() {
        return "Protected API working";
    }
}