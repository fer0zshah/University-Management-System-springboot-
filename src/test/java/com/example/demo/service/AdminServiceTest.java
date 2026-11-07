// এই test class-টি service package-এর অংশ।
package com.example.demo.service;

// AdminService থেকে পাওয়া admin তথ্য রাখার response DTO import করা হচ্ছে।
import com.example.demo.dto.AdminResponse;
// test-এর জন্য একটি Admin entity তৈরি করতে Admin class import করা হচ্ছে।
import com.example.demo.entity.Admin;
// admin-এর role যাচাই করার জন্য Role enum import করা হচ্ছে।
import com.example.demo.entity.Role;
// database operation নকল করার জন্য AdminRepository import করা হচ্ছে।
import com.example.demo.repository.AdminRepository;
// AdminService-এর আরেকটি dependency নকল করার জন্য UserRepository import করা হচ্ছে।
import com.example.demo.repository.UserRepository;
// কোনো method-কে test method হিসেবে চিহ্নিত করতে JUnit-এর Test annotation import করা হচ্ছে।
import org.junit.jupiter.api.Test;
// JUnit-এর সঙ্গে Mockito extension যুক্ত করতে ExtendWith import করা হচ্ছে।
import org.junit.jupiter.api.extension.ExtendWith;
// mock dependency-গুলো AdminService-এর মধ্যে inject করতে InjectMocks import করা হচ্ছে।
import org.mockito.InjectMocks;
// নকল dependency তৈরি করতে Mock annotation import করা হচ্ছে।
import org.mockito.Mock;
// প্রতিটি test-এর আগে Mockito চালু করার জন্য MockitoExtension import করা হচ্ছে।
import org.mockito.junit.jupiter.MockitoExtension;
// AdminService-এর password encoder dependency নকল করতে এই interface import করা হচ্ছে।
import org.springframework.security.crypto.password.PasswordEncoder;
// HTTP status-সহ service exception যাচাই করতে এই exception class import করা হচ্ছে।
import org.springframework.web.server.ResponseStatusException;

// repository result-এ value থাকতে বা না থাকতে পারে—তা প্রকাশ করতে Optional import করা হচ্ছে।
import java.util.Optional;

// expected এবং actual value সমান কি না যাচাই করতে assertEquals import করা হচ্ছে।
import static org.junit.jupiter.api.Assertions.assertEquals;
// নির্দিষ্ট exception ছোড়া হয়েছে কি না যাচাই করতে assertThrows import করা হচ্ছে।
import static org.junit.jupiter.api.Assertions.assertThrows;
// mock method call-এর result নির্ধারণ করতে Mockito-এর when import করা হচ্ছে।
import static org.mockito.Mockito.when;

// এই class-এর tests চালানোর সময় Mockito-কে সক্রিয় করা হচ্ছে।
@ExtendWith(MockitoExtension.class)
// AdminService-এর unit tests এই class-এর মধ্যে রাখা হয়েছে।
class AdminServiceTest {

    // আসল database ব্যবহার না করে AdminRepository-এর mock object তৈরি করা হচ্ছে।
    @Mock
    // এই mock repository থেকে test অনুযায়ী কৃত্রিম result ফেরত দেওয়া হবে।
    private AdminRepository adminRepository;

    // AdminService constructor-এর UserRepository dependency-এর mock তৈরি করা হচ্ছে।
    @Mock
    // এই test-গুলোতে আসল users table access করা হবে না।
    private UserRepository userRepository;

    // আসল BCrypt calculation এড়িয়ে PasswordEncoder-এর mock তৈরি করা হচ্ছে।
    @Mock
    // এই dependency এখানে সরাসরি ব্যবহার না হলেও AdminService তৈরি করতে প্রয়োজন।
    private PasswordEncoder passwordEncoder;

    // উপরের mock dependency-গুলো দিয়ে test করার জন্য AdminService object তৈরি করা হচ্ছে।
    @InjectMocks
    // test method-গুলো এই service object-এর public method call করবে।
    private AdminService adminService;

    // নিচের method-টি একটি JUnit test হিসেবে চালানো হবে।
    @Test
    // পরিচিত ID দিলে সঠিক ও নিরাপদ AdminResponse পাওয়া যায় কি না এই test যাচাই করে।
    void getByIdReturnsAdminWithoutExposingPassword() {
        // repository থেকে পাওয়া admin record অনুকরণ করতে নতুন Admin object তৈরি করা হচ্ছে।
        Admin admin = new Admin();
        // test admin-এর database ID 3 নির্ধারণ করা হচ্ছে।
        admin.setId(3L);
        // test admin-এর username নির্ধারণ করা হচ্ছে।
        admin.setUsername("admin1");
        // database-এর মতো encoded password রাখা হচ্ছে; response-এ এটি প্রকাশ করা হবে না।
        admin.setPassword("encoded-secret");
        // test admin-এর পূর্ণ নাম নির্ধারণ করা হচ্ছে।
        admin.setFullName("Admin One");
        // test admin-এর email address নির্ধারণ করা হচ্ছে।
        admin.setEmail("admin1@kuet.ac.bd");
        // test admin-এর office নির্ধারণ করা হচ্ছে।
        admin.setOffice("Academic Building");
        // findById(3) call হলে mock repository-কে উপরের admin ফেরত দিতে বলা হচ্ছে।
        when(adminRepository.findById(3L)).thenReturn(Optional.of(admin));

        // বাস্তবে যে method test করা হচ্ছে সেটি call করে response সংরক্ষণ করা হচ্ছে।
        AdminResponse response = adminService.getById(3L);

        // response-এ প্রত্যাশিত admin ID এসেছে কি না যাচাই করা হচ্ছে।
        assertEquals(3L, response.id());
        // response-এ প্রত্যাশিত username এসেছে কি না যাচাই করা হচ্ছে।
        assertEquals("admin1", response.username());
        // Admin constructor থেকে role স্বয়ংক্রিয়ভাবে ADMIN হয়েছে কি না যাচাই করা হচ্ছে।
        assertEquals(Role.ADMIN, response.role());
        // response-এ প্রত্যাশিত office এসেছে কি না যাচাই করা হচ্ছে।
        assertEquals("Academic Building", response.office());
        // AdminResponse-এ password field-ই নেই, তাই encoded password client-এর কাছে প্রকাশ পায় না।
    }

    // নিচের method-টিও একটি JUnit test হিসেবে চালানো হবে।
    @Test
    // অজানা ID দিলে service সঠিক 404 exception দেয় কি না এই test যাচাই করে।
    void getByIdReturnsNotFoundForUnknownAdmin() {
        // ID 99 database-এ নেই বোঝাতে mock repository empty Optional ফেরত দেবে।
        when(adminRepository.findById(99L)).thenReturn(Optional.empty());

        // getById(99) call করলে ResponseStatusException ছোড়া হবে বলে প্রত্যাশা করা হচ্ছে।
        ResponseStatusException exception = assertThrows(
                // যে ধরনের exception প্রত্যাশিত এবং যে service call-টি exception ছোড়ার কথা তা দেওয়া হচ্ছে।
                ResponseStatusException.class, () -> adminService.getById(99L));

        // exception-এর HTTP status code 404 Not Found কি না যাচাই করা হচ্ছে।
        assertEquals(404, exception.getStatusCode().value());
        // exception-এর error message সঠিক কি না যাচাই করা হচ্ছে।
        assertEquals("Admin not found with id 99", exception.getReason());
        // দ্বিতীয় test method এখানে শেষ হচ্ছে।
    }
    // AdminServiceTest class এখানে শেষ হচ্ছে।
}
