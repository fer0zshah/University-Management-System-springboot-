package com.example.demo.service;

import com.example.demo.dto.StudentRequest;
import com.example.demo.dto.StudentResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.Student;
import com.example.demo.repository.StudentRepository;
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
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    @Test
    void createStudentSavesEncodedPasswordAndReturnsResponse() {
        StudentRequest request = new StudentRequest(
                "student1", "secret", "Student One", "student1@kuet.ac.bd", "2007087", "CSE");

        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        StudentResponse response = studentService.create(request);

        assertEquals(1L, response.id());
        assertEquals("student1", response.username());
        assertEquals("2007087", response.rollNumber());
        assertEquals(Role.STUDENT, response.role());
        verify(passwordEncoder).encode("secret");
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void createStudentRejectsDuplicateRollNumber() {
        StudentRequest request = new StudentRequest(
                "student2", "secret", "Student Two", "student2@kuet.ac.bd", "2207087", "CSE");
        when(studentRepository.existsByRollNumber("2207087")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> studentService.create(request));

        assertEquals(409, exception.getStatusCode().value());
        verify(studentRepository, never()).save(any(Student.class));
    }
}
