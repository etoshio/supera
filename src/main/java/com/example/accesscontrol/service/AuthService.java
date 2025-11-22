package com.example.accesscontrol.service;

import com.example.accesscontrol.dto.LoginRequest;
import com.example.accesscontrol.dto.LoginResponse;
import com.example.accesscontrol.model.User;
import com.example.accesscontrol.repository.UserRepository;
import com.example.accesscontrol.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
            authenticationManager.authenticate(authToken);
        } catch (Exception e) {
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Usuário ou senha inválidos"));

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, "Bearer", 900);
    }
}
