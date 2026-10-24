package com.example.demo.controller;

import com.example.demo.dto.TeacherRequest;
import com.example.demo.dto.TeacherResponse;
import com.example.demo.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public List<TeacherResponse> getAll() {
        return teacherService.getAll();
    }

    @GetMapping("/{id}")
    public TeacherResponse getById(@PathVariable Long id) {
        return teacherService.getById(id);
    }

    @PostMapping
    public ResponseEntity<TeacherResponse> create(@RequestBody TeacherRequest request) {
        TeacherResponse created = teacherService.create(request);
        return ResponseEntity.created(URI.create("/api/teachers/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public TeacherResponse update(@PathVariable Long id, @RequestBody TeacherRequest request) {
        return teacherService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}