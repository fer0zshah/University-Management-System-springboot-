package com.example.demo.service;

import com.example.demo.dto.StudentRequest;
import com.example.demo.dto.StudentResponse;
import com.example.demo.entity.Student;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<StudentResponse> getAll() {
        return studentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public StudentResponse getById(Long id) {
        return toResponse(find(id));
    }

    public StudentResponse create(StudentRequest req) {
        validate(req, true);
        if (userRepository.existsByUsername(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        if (!isBlank(req.rollNumber()) && studentRepository.existsByRollNumber(req.rollNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Roll number already exists");
        }

        Student student = new Student();
        student.setUsername(req.username());
        student.setPassword(passwordEncoder.encode(req.password()));
        student.setFullName(req.fullName());
        student.setEmail(req.email());
        student.setRollNumber(req.rollNumber());
        student.setDepartment(req.department());
        return toResponse(studentRepository.save(student));
    }

    public StudentResponse update(Long id, StudentRequest req) {
        validate(req, false);
        Student student = find(id);

        if (!student.getUsername().equals(req.username())
                && userRepository.existsByUsername(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (!student.getEmail().equals(req.email())
                && userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        if (!isBlank(req.rollNumber())
                && !req.rollNumber().equals(student.getRollNumber())
                && studentRepository.existsByRollNumber(req.rollNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Roll number already exists");
        }

        student.setUsername(req.username());
        student.setFullName(req.fullName());
        student.setEmail(req.email());
        student.setRollNumber(req.rollNumber());
        student.setDepartment(req.department());
        if (!isBlank(req.password())) {
            student.setPassword(passwordEncoder.encode(req.password()));
        }
        return toResponse(studentRepository.save(student));
    }

    public void delete(Long id) {
        studentRepository.delete(find(id));
    }

    private Student find(Long id) {
        return studentRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id " + id));
    }

    private void validate(StudentRequest req, boolean passwordRequired) {
        if (isBlank(req.username()) || isBlank(req.fullName()) || isBlank(req.email())
                || (passwordRequired && isBlank(req.password()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "username, fullName, email" + (passwordRequired ? ", password" : "") + " are required");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private StudentResponse toResponse(Student s) {
        return new StudentResponse(s.getId(), s.getUsername(), s.getFullName(),
                s.getEmail(), s.getRole(), s.getRollNumber(), s.getDepartment());
    }
}