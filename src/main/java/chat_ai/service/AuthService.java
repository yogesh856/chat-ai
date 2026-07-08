package chat_ai.service;



import chat_ai.dto.response.AuthResponse;
import chat_ai.dto.request.LoginRequest;
import chat_ai.dto.request.RegisterRequest;
import chat_ai.entity.Role;
import chat_ai.entity.User;
import chat_ai.repository.UserRepository;
import chat_ai.security.JwtService;
import chat_ai.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {

        // 1. Check karo email already exist toh nahi karti
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // 2. Naya user banao — password HASH karke store karo
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.User);   // default role

        userRepository.save(user);

        // 3. Token generate karo aur return karo
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String token = jwtService.generateToken(userPrincipal);

        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {

        // 1. AuthenticationManager credentials verify karega
        //    (internally UserDetailsService + PasswordEncoder use hoga)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Yaha tak pohoch gaye matlab credentials sahi the
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Token generate karo
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String token = jwtService.generateToken(userPrincipal);

        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }
}