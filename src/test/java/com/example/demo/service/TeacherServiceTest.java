package com.example.demo.service;

import com.example.demo.dto.TeacherRequest;
import com.example.demo.dto.TeacherResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.Teacher;
import com.example.demo.repository.TeacherRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TeacherService teacherService;

    @Test
    void createTeacherSavesTeacherAndReturnsResponse() {
        TeacherRequest request = new TeacherRequest(
                "teacher1", "secret", "Teacher One", "teacher1@kuet.ac.bd", "CSE", "Professor");
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> {
            Teacher saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        TeacherResponse response = teacherService.create(request);

        assertEquals(2L, response.id());
        assertEquals("teacher1", response.username());
        assertEquals("Professor", response.designation());
        assertEquals(Role.TEACHER, response.role());
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void createTeacherRejectsMissingRequiredUsername() {
        TeacherRequest request = new TeacherRequest(
                " ", "secret", "Teacher Two", "teacher2@kuet.ac.bd", "EEE", "Lecturer");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> teacherService.create(request));

        assertEquals(400, exception.getStatusCode().value());
        verify(teacherRepository, never()).save(any(Teacher.class));
    }
}
