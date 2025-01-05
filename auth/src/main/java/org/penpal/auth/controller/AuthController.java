package org.penpal.auth.controller;

import lombok.RequiredArgsConstructor;
import org.penpal.auth.dto.ResetPasswordPayload;
import org.penpal.auth.dto.StudentFilter;
import org.penpal.auth.dto.TeacherFilter;
import org.penpal.auth.model.*;
import org.penpal.auth.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping(path ="/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> login(@RequestBody Auth request) {
        return authService.login(request);
    }

    @PutMapping(path ="/forgot-password/{userEmail}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> forgotPassword(
            @PathVariable("userEmail") String userEmail,
            @RequestParam("userRole") String userRole) {
        return authService.forgotPassword(userEmail, userRole);
    }

    @PutMapping(path ="/change-password/{userEmail}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> changePassword(
            @PathVariable("userEmail") String userEmail,
            @RequestBody ResetPasswordPayload resetPasswordPayload) {
        return authService.changePassword(userEmail, resetPasswordPayload);
    }

    @GetMapping(path ="/validateToken")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> validateToken(@RequestBody String token) {
        return authService.validateToken(token);
    }

    @PostMapping(path = "/students")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> registerStudent(@RequestBody Student student) {
        return authService.registerStudent(student);
    }

    @GetMapping(path = "/students")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getStudents() {
        return authService.getStudents();
    }

    @GetMapping(path = "/students/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getStudent(@PathVariable("email") String email) {
        return authService.getStudent(email);
    }

    @GetMapping(path = "/students/without-penpals")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getStudentsWithoutPenpals() {
        return authService.getStudentsWithoutPenpal();
    }

    @PutMapping(path = "/students/upload-penpal-matching")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> uploadPenpalMatching(@RequestParam("file") MultipartFile file) throws IOException {
        return authService.uploadPenpalMatching(file);
    }

    @PutMapping(path = "/students/update-penpal")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateStudentPenpal(
            @RequestPart("studentEmail") String studentEmail,
            @RequestPart("penpalEmail") String penpalEmail,
            @RequestPart("penpalGroup") String penpalGroup) {
        return authService.updateStudentPenpal(studentEmail, penpalEmail, penpalGroup);
    }

    @PostMapping(path = "/students/filter")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> filterStudents(StudentFilter filter) {
        return authService.filterStudents(filter);
    }

    @PatchMapping(path = "/students/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateStudent(@PathVariable("email") String email, @RequestBody Student student) {
        return authService.updateStudent(student, email);
    }

    @GetMapping(path = "/students/{email}/profile-photo")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getStudentProfilePhoto(@PathVariable("email") String email) {
        return authService.getStudentProfilePhoto(email);
    }

    @PatchMapping(path = "/students/{email}/profile-photo")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateStudentProfilePhoto(
            @PathVariable("email") String email, @RequestParam("file") MultipartFile file) throws IOException {
        return authService.updateStudentProfilePhoto(email, file);
    }

    @PutMapping(path = "/students/{email}/account-status")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateStudentAccountStatus(
            @PathVariable("email") String email, @RequestParam("status") String status) {
        return authService.updateStudentAccountStatus(email, status);
    }

    @PatchMapping(path = "/students/{email}/delete-account")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> sendStudentAccountDeleteRequest(@PathVariable("email") String email) {
        return authService.sendStudentAccountDeleteRequest(email);
    }

    @DeleteMapping(path = "/students/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteStudent(@PathVariable("email") String email) {
        return authService.deleteStudent(email);
    }

    @PostMapping(path = "/teachers")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> registerTeacher(@RequestBody Teacher teacher) {
        return authService.registerTeacher(teacher);
    }

    @GetMapping(path = "/teachers")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getTeachers() {
        return authService.getTeachers();
    }

    @PostMapping(path = "/teachers/filter")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> filterTeachers(@RequestBody TeacherFilter filter) {
        return authService.filterTeachers(filter);
    }

    @GetMapping(path = "/teachers/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getTeacher(@PathVariable("email") String email) {
        return authService.getTeacher(email);
    }

    @PatchMapping(path = "/teachers/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateTeacher(@RequestBody Teacher teacher, @PathVariable("email") String email) {
        return authService.updateTeacher(teacher, email);
    }

    @DeleteMapping(path = "/teachers/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteTeacher(@PathVariable("email") String email) {
        return authService.deleteTeacher(email);
    }

    @GetMapping(path = "/teachers/structure")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getTeacherStructure(@RequestParam(value = "userRole", required = false) String userRole) {
        return authService.getDistrictHierarchy(userRole);
    }

    @PostMapping(path = "/translators")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> registerTranslator(@RequestBody Translator translator) {
        return authService.registerTranslator(translator);
    }

    @GetMapping(path = "/translators")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getTranslators() {
        return authService.getTranslators();
    }

    @GetMapping(path = "/translators/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getTranslator(@PathVariable("email") String email) {
        return authService.getTranslator(email);
    }

    @PatchMapping(path = "/translators/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateTranslator(@PathVariable("email") String email, @RequestBody Translator translator) {
        return authService.updateTranslator(translator, email);
    }

    @DeleteMapping(path = "/translators/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteTranslator(@PathVariable("email") String email) {
        return authService.deleteTranslator(email);
    }

    @PostMapping(path = "/researchers")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> registerResearcher(@RequestBody Researcher researcher) {
        return authService.registerResearcher(researcher);
    }

    @GetMapping(path = "/researchers")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getResearchers() {
        return authService.getResearchers();
    }

    @GetMapping(path = "/researchers/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getResearcher(@PathVariable("email") String email) {
        return authService.getResearcher(email);
    }

    @PatchMapping(path = "/researchers/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateResearcher(@PathVariable("email") String email, @RequestBody Researcher researcher) {
        return authService.updateResearcher(researcher, email);
    }

    @DeleteMapping(path = "/researchers/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteResearcher(@PathVariable("email") String email) {
        return authService.deleteResearcher(email);
    }
}
