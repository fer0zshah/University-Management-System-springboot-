package com.example.demo.controller;

import com.example.demo.dto.AdminRequest;
import com.example.demo.dto.AdminResponse;
import com.example.demo.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public List<AdminResponse> getAll() {
        return adminService.getAll();
    }

    @GetMapping("/{id}")
    public AdminResponse getById(@PathVariable Long id) {
        return adminService.getById(id);
    }

    @PostMapping
    public ResponseEntity<AdminResponse> create(@RequestBody AdminRequest request) {
        AdminResponse created = adminService.create(request);
        return ResponseEntity.created(URI.create("/api/admins/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public AdminResponse update(@PathVariable Long id, @RequestBody AdminRequest request) {
        return adminService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}