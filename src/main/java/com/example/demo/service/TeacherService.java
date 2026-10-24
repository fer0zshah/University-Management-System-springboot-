package com.example.demo.service;

import com.example.demo.dto.TeacherRequest;
import com.example.demo.dto.TeacherResponse;
import com.example.demo.entity.Teacher;
import com.example.demo.repository.TeacherRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<TeacherResponse> getAll() {
        return teacherRepository.findAll().stream().map(this::toResponse).toList();
    }

    public TeacherResponse getById(Long id) {
        return toResponse(find(id));
    }

    public TeacherResponse create(TeacherRequest req) {
        validate(req, true);
        if (userRepository.existsByUsername(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        Teacher teacher = new Teacher();
        teacher.setUsername(req.username());
        teacher.setPassword(passwordEncoder.encode(req.password()));
        teacher.setFullName(req.fullName());
        teacher.setEmail(req.email());
        teacher.setDepartment(req.department());
        teacher.setDesignation(req.designation());
        return toResponse(teacherRepository.save(teacher));
    }

    public TeacherResponse update(Long id, TeacherRequest req) {
        validate(req, false);
        Teacher teacher = find(id);

        if (!teacher.getUsername().equals(req.username())
                && userRepository.existsByUsername(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (!teacher.getEmail().equals(req.email())
                && userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        teacher.setUsername(req.username());
        teacher.setFullName(req.fullName());
        teacher.setEmail(req.email());
        teacher.setDepartment(req.department());
        teacher.setDesignation(req.designation());
        if (!isBlank(req.password())) {
            teacher.setPassword(passwordEncoder.encode(req.password()));
        }
        return toResponse(teacherRepository.save(teacher));
    }

    public void delete(Long id) {
        teacherRepository.delete(find(id));
    }

    private Teacher find(Long id) {
        return teacherRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found with id " + id));
    }

    private void validate(TeacherRequest req, boolean passwordRequired) {
        if (isBlank(req.username()) || isBlank(req.fullName()) || isBlank(req.email())
                || (passwordRequired && isBlank(req.password()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "username, fullName, email" + (passwordRequired ? ", password" : "") + " are required");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private TeacherResponse toResponse(Teacher t) {
        return new TeacherResponse(t.getId(), t.getUsername(), t.getFullName(),
                t.getEmail(), t.getRole(), t.getDepartment(), t.getDesignation());
    }
}