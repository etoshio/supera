package com.example.accesscontrol.config;

import com.example.accesscontrol.model.Department;
import com.example.accesscontrol.model.User;
import com.example.accesscontrol.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDataInitializerTest {

    @Test
    void deveCriarUsuariosQuandoBaseVazia() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(repo.count()).thenReturn(0L);
        when(encoder.encode("Password123!")).thenReturn("hash");

        UserDataInitializer init = new UserDataInitializer(repo, encoder);
        init.initUsers().run(new String[0]);

        verify(repo, times(4)).save(any(User.class));
        verify(encoder, times(4)).encode("Password123!");
    }

    @Test
    void naoDeveCriarUsuariosQuandoJaExiste() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(repo.count()).thenReturn(5L);

        UserDataInitializer init = new UserDataInitializer(repo, encoder);
        init.initUsers().run(new String[0]);

        verify(repo).count();
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(encoder);
    }
}
