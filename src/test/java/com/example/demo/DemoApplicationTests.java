package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
// These tests load the real Spring context, security chain, controllers,
// services and H2 database, so they are integration tests rather than unit tests.
class DemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void adminCanCreateTeacher() throws Exception {
		mockMvc.perform(post("/api/teachers")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "integration-teacher",
						  "password": "secret123",
						  "fullName": "Integration Teacher",
						  "email": "integration.teacher@kuet.ac.bd",
						  "department": "CSE",
						  "designation": "Lecturer"
						}
						"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.username").value("integration-teacher"))
				.andExpect(jsonPath("$.role").value("TEACHER"));
	}

	@Test
	@WithMockUser(username = "teacher", roles = "TEACHER")
	void teacherCannotCreateAnotherTeacher() throws Exception {
		mockMvc.perform(post("/api/teachers")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "forbidden-teacher",
						  "password": "secret123",
						  "fullName": "Forbidden Teacher",
						  "email": "forbidden.teacher@kuet.ac.bd",
						  "department": "EEE",
						  "designation": "Lecturer"
						}
						"""))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "student", roles = "STUDENT")
	void studentCannotCreateAnotherStudent() throws Exception {
		mockMvc.perform(post("/api/students")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "forbidden-student",
						  "password": "secret123",
						  "fullName": "Forbidden Student",
						  "email": "forbidden.student@kuet.ac.bd",
						  "rollNumber": "2107999",
						  "department": "CSE"
						}
						"""))
				.andExpect(status().isForbidden());
	}
}
