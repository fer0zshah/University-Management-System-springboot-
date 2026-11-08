
package com.example.demo.service;

import com.example.demo.dto.AdminResponse;
import com.example.demo.entity.Admin;
import com.example.demo.entity.Role;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getByIdReturnsAdminWithoutExposingPassword() {
        Admin admin = new Admin();
        admin.setId(3L);
        admin.setUsername("admin1");
        admin.setPassword("encoded-secret");
        admin.setFullName("Admin One");
        admin.setEmail("admin1@kuet.ac.bd");
        admin.setOffice("Academic Building");

        when(adminRepository.findById(3L)).thenReturn(Optional.of(admin));

        AdminResponse response = adminService.getById(3L);

        assertEquals(3L, response.id());
        assertEquals("admin1", response.username());
        assertEquals(Role.ADMIN, response.role());
        assertEquals("Academic Building", response.office());
    }

    @Test
    void getByIdReturnsNotFoundForUnknownAdmin() {
        when(adminRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> adminService.getById(99L));

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Admin not found with id 99", exception.getReason());
    }
}
