package com.example.accesscontrol.config;

import com.example.accesscontrol.model.Department;
import com.example.accesscontrol.model.User;
import com.example.accesscontrol.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class UserDataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initUsers() {
        return args -> {
            if (userRepository.count() > 0) return;

            createUser("ti.user@example.com", "User TI", Department.TI);
            createUser("fin.user@example.com", "User Financeiro", Department.FINANCEIRO);
            createUser("rh.user@example.com", "User RH", Department.RH);
            createUser("op.user@example.com", "User Operacoes", Department.OPERACOES);
        };
    }

    private void createUser(String email, String name, Department dept) {
        User u = User.builder()
                .email(email)
                .name(name)
                .department(dept)
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build();
        userRepository.save(u);
    }
}
