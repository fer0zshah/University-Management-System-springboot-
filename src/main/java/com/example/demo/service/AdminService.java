package com.example.demo.service;

import com.example.demo.dto.AdminRequest;
import com.example.demo.dto.AdminResponse;
import com.example.demo.entity.Admin;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<AdminResponse> getAll() {
        return adminRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AdminResponse getById(Long id) {
        return toResponse(find(id));
    }

    public AdminResponse create(AdminRequest req) {
        validate(req, true);
        if (userRepository.existsByUsername(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        Admin admin = new Admin();
        admin.setUsername(req.username());
        admin.setPassword(passwordEncoder.encode(req.password()));
        admin.setFullName(req.fullName());
        admin.setEmail(req.email());
        admin.setOffice(req.office());
        return toResponse(adminRepository.save(admin));
    }

    public AdminResponse update(Long id, AdminRequest req) {
        validate(req, false);
        Admin admin = find(id);

        if (!admin.getUsername().equals(req.username())
                && userRepository.existsByUsername(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (!admin.getEmail().equals(req.email())
                && userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        admin.setUsername(req.username());
        admin.setFullName(req.fullName());
        admin.setEmail(req.email());
        admin.setOffice(req.office());
        if (!isBlank(req.password())) { // password is optional on update
            admin.setPassword(passwordEncoder.encode(req.password()));
        }
        return toResponse(adminRepository.save(admin));
    }

    public void delete(Long id) {
        adminRepository.delete(find(id));
    }

    private Admin find(Long id) {
        return adminRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found with id " + id));
    }

    private void validate(AdminRequest req, boolean passwordRequired) {
        if (isBlank(req.username()) || isBlank(req.fullName()) || isBlank(req.email())
                || (passwordRequired && isBlank(req.password()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "username, fullName, email" + (passwordRequired ? ", password" : "") + " are required");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private AdminResponse toResponse(Admin a) {
        return new AdminResponse(a.getId(), a.getUsername(), a.getFullName(),
                a.getEmail(), a.getRole(), a.getOffice());
    }
}