package org.penpal.auth.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.ss.usermodel.*;
import org.penpal.auth.dto.*;
import org.penpal.auth.model.*;
import org.penpal.auth.repository.*;
import org.penpal.auth.tokens.JwtGenerator;
import org.penpal.auth.tokens.TempPasswordGenerator;
import org.penpal.auth.validators.JwtValidator;
import org.penpal.email.service.dto.EmailSendingPayload;
import org.penpal.email.service.service.EmailSendingService;
import org.penpal.shared.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static java.rmi.server.LogStream.log;
import static org.penpal.shared.Constants.EMAIL_TAKEN;
import static org.penpal.shared.Constants.INVALID_EMAIL;

@Service
public class AuthService {
    @Value("${invitation.service.hosted.url}")
    private String INVITATION_SERVICE_HOSTED_URL;
    private final EmailValidator emailValidator;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private JwtGenerator jwtGenerator;
    @Autowired
    private JwtValidator jwtValidator;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private TranslatorRepository translatorRepository;
    @Autowired
    private ResearcherRepository researcherRepository;
    @Autowired
    private EmailSendingService emailSendingService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SchoolRepository schoolRepository;
    @Value("${reset.pwd.url}")
    private String RESET_PWD_HOSTED_URL;

    public AuthService(EmailValidator emailValidator) {
        this.emailValidator = emailValidator;
    }

    public ResponseEntity<?> login(Auth request) {
        boolean isValidEmail = emailValidator.test(request.getUserEmail());
        if (!isValidEmail) {
            return new ResponseEntity<>("Invalid email format!", HttpStatus.BAD_REQUEST);
        }
        if (request.getUserRole().equals(UserRole.STUDENT.toString())) {
            Optional<Student> student = studentRepository.findByUserEmail(request.getUserEmail());
            if (student.isPresent() && student.get().getAccountStatus() == AccountStatus.ACTIVE &&
                    student.get().getPassword() != null &&
                    new BCryptPasswordEncoder().matches(request.getUserPassword(), student.get().getPassword())) {
                String userRole = student.get().getUserRole().toString();
                student.get().setResetToken(null);
                studentRepository.save(student.get());
                var token = jwtGenerator.generateToken(request.getUserEmail(), userRole);
                return new ResponseEntity<>("{\"token\":\"" + token + "\",\"userRole\":\"" + userRole + "\"}", HttpStatus.OK);
            }
            return new ResponseEntity<>("{\"token\":\"" + "Invalid credentials or account is not active" + "\",\"userRole\":\"" + UserRole.STUDENT + "\"  }", HttpStatus.NOT_FOUND);
        } else if (request.getUserRole().equals(UserRole.TEACHER.toString())) {
            Optional<Teacher> teacher = teacherRepository.findByUserEmail(request.getUserEmail());
            if (teacher.isPresent() && teacher.get().getUserPassword() != null && new BCryptPasswordEncoder().matches(request.getUserPassword(), teacher.get().getUserPassword())) {
                String userRole = teacher.get().getUserRole().toString();
                teacher.get().setResetToken(null);
                teacherRepository.save(teacher.get());
                var token = jwtGenerator.generateToken(request.getUserEmail(), userRole);
                return new ResponseEntity<>("{\"token\":\"" + token + "\",\"userRole\":\"" + userRole + "\"}", HttpStatus.OK);
            }
            return new ResponseEntity<>("{\"token\":\"" + "Invalid Credentials" + "\",\"userRole\":\"" + null + "\"  }", HttpStatus.NOT_FOUND);
        } else if (request.getUserRole().equals(UserRole.TRANSLATOR.toString())) {
            Optional<Translator> translator = translatorRepository.findByUserEmail(request.getUserEmail());
            if (translator.isPresent() && translator.get().getUserPassword() != null && new BCryptPasswordEncoder().matches(request.getUserPassword(), translator.get().getUserPassword())) {
                String userRole = translator.get().getUserRole().toString();
                translator.get().setResetToken(null);
                translatorRepository.save(translator.get());
                var token = jwtGenerator.generateToken(request.getUserEmail(), userRole);
                return new ResponseEntity<>("{\"token\":\"" + token + "\",\"userRole\":\"" + userRole + "\"}", HttpStatus.OK);
            }
            return new ResponseEntity<>("{\"token\":\"" + "Invalid Credentials" + "\",\"userRole\":\"" + null + "\"  }", HttpStatus.NOT_FOUND);
        } else if (request.getUserRole().equals(UserRole.RESEARCHER.toString())) {
            Optional<Researcher> researcher = researcherRepository.findByUserEmail(request.getUserEmail());
            if (researcher.isPresent() && researcher.get().getUserPassword() != null && new BCryptPasswordEncoder().matches(request.getUserPassword(), researcher.get().getUserPassword())) {
                String userRole = researcher.get().getUserRole().toString();
                researcher.get().setResetToken(null);
                researcherRepository.save(researcher.get());
                var token = jwtGenerator.generateToken(request.getUserEmail(), userRole);
                return new ResponseEntity<>("{\"token\":\"" + token + "\",\"userRole\":\"" + userRole + "\"}", HttpStatus.OK);
            }
            return new ResponseEntity<>("{\"token\":\"" + "Invalid Credentials" + "\",\"userRole\":\"" + null + "\"  }", HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>("{\"token\":\"Invalid userRole\":\"" + null + "\"  }" + null + "\"  }", HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<?> validateToken(String email, String userRole) {
        if (userRole.equals(UserRole.STUDENT.toString())) {
            Optional<Student> student = studentRepository.findByUserEmail(email);
            if (student.isPresent()) {
                if(student.get().getResetToken() != null) {
                    boolean validate = jwtValidator.validateToken(student.get().getResetToken());
                    if(!validate) {
                        return new ResponseEntity<>("You are not authorized to do this. Please do the forgot password process again!", HttpStatus.UNAUTHORIZED);
                    } else {
                        student.get().setResetToken(null);
                        studentRepository.save(student.get());
                        return new ResponseEntity<>("Token is validated", HttpStatus.OK);
                    }
                }
                return new ResponseEntity<>("You have not requested a password change", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Student not found with the provided email!", HttpStatus.NOT_FOUND);
            }
        } else if (userRole.equals(UserRole.TEACHER.toString())) {
            Optional<Teacher> teacher = teacherRepository.findByUserEmail(email);
            if (teacher.isPresent()) {
                if(teacher.get().getResetToken() != null) {
                    boolean validate = jwtValidator.validateToken(teacher.get().getResetToken());
                    if(!validate) {
                        return new ResponseEntity<>("You are not authorized to do this. Please do the forgot password process again!", HttpStatus.UNAUTHORIZED);
                    } else {
                        teacher.get().setResetToken(null);
                        teacherRepository.save(teacher.get());
                        return new ResponseEntity<>("Token is validated", HttpStatus.OK);
                    }
                }
                return new ResponseEntity<>("You have not requested a password change", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Teacher not found with the provided email!", HttpStatus.NOT_FOUND);
            }
        } else if (userRole.equals(UserRole.TRANSLATOR.toString())) {
            Optional<Translator> translator = translatorRepository.findByUserEmail(email);
            if (translator.isPresent()) {
                if(translator.get().getResetToken() != null) {
                    boolean validate = jwtValidator.validateToken(translator.get().getResetToken());
                    if(!validate) {
                        return new ResponseEntity<>("You are not authorized to do this. Please do the forgot password process again!", HttpStatus.UNAUTHORIZED);
                    } else {
                        translator.get().setResetToken(null);
                        translatorRepository.save(translator.get());
                        return new ResponseEntity<>("Token is validated", HttpStatus.OK);
                    }
                }
                return new ResponseEntity<>("You have not requested a password change", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Translator not found with the provided email!", HttpStatus.NOT_FOUND);
            }
        } else if (userRole.equals(UserRole.RESEARCHER.toString())) {
            Optional<Researcher> researcher = researcherRepository.findByUserEmail(email);
            if (researcher.isPresent()) {
                if(researcher.get().getResetToken() != null) {
                    boolean validate = jwtValidator.validateToken(researcher.get().getResetToken());
                    if(!validate) {
                        return new ResponseEntity<>("You are not authorized to do this. Please do the forgot password process again!", HttpStatus.UNAUTHORIZED);
                    } else {
                        researcher.get().setResetToken(null);
                        researcherRepository.save(researcher.get());
                        return new ResponseEntity<>("Token is validated", HttpStatus.OK);
                    }
                }
                return new ResponseEntity<>("You have not requested a password change", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Researcher not found with the provided email!", HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>("Invalid user role!", HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> forgotPassword(String email, String userRole) {
        String resetToken = jwtGenerator.generateToken(email, userRole);
        EmailSendingPayload emailSendingPayload = new EmailSendingPayload();
        emailSendingPayload.setTo(email);
        emailSendingPayload.setSubject(EmailTemplates.RESET_PASSWORD_SUBJECT);
        emailSendingPayload.setBody(String.format(EmailTemplates.RESET_PASSWORD_BODY, RESET_PWD_HOSTED_URL, email));
        if (userRole.equals(UserRole.STUDENT.toString())) {
            Optional<Student> student = studentRepository.findByUserEmail(email);
            if (student.isPresent()) {
                student.get().setResetToken(resetToken);
                studentRepository.save(student.get());
                emailSendingService.sendEmail(emailSendingPayload);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Student not found with the provided email!", HttpStatus.NOT_FOUND);
            }
        } else if (userRole.equals(UserRole.TEACHER.toString())) {
            Optional<Teacher> teacher = teacherRepository.findByUserEmail(email);
            if (teacher.isPresent()) {
                teacher.get().setResetToken(resetToken);
                teacherRepository.save(teacher.get());
                emailSendingService.sendEmail(emailSendingPayload);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Teacher not found with the provided email!", HttpStatus.NOT_FOUND);
            }
        } else if (userRole.equals(UserRole.TRANSLATOR.toString())) {
            Optional<Translator> translator = translatorRepository.findByUserEmail(email);
            if (translator.isPresent()) {
                translator.get().setResetToken(resetToken);
                translatorRepository.save(translator.get());
                emailSendingService.sendEmail(emailSendingPayload);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Translator not found with the provided email!", HttpStatus.NOT_FOUND);
            }
        } else if (userRole.equals(UserRole.RESEARCHER.toString())) {
            Optional<Researcher> researcher = researcherRepository.findByUserEmail(email);
            if (researcher.isPresent()) {
                researcher.get().setResetToken(resetToken);
                researcherRepository.save(researcher.get());
                emailSendingService.sendEmail(emailSendingPayload);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Researcher not found with the provided email!", HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>("Invalid user role!", HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> changePassword(String email, ResetPasswordPayload resetPasswordPayload) {
        if (resetPasswordPayload.getUserRole().equals(UserRole.STUDENT.toString())) {
            Optional<Student> student = studentRepository.findByUserEmail(email);
            if (student.isPresent()) {
                student.get().setPassword(bCryptPasswordEncoder.encode(resetPasswordPayload.getNewPassword()));
                studentRepository.save(student.get());
                return new ResponseEntity<>("Password reset successful!", HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>("Student not found with the provided email!", HttpStatus.NOT_FOUND);
            }
        } else if (resetPasswordPayload.getUserRole().equals(UserRole.TEACHER.toString())) {
            Optional<Teacher> teacher = teacherRepository.findByUserEmail(email);
            if (teacher.isPresent()) {
                teacher.get().setUserPassword(bCryptPasswordEncoder.encode(resetPasswordPayload.getNewPassword()));
                teacherRepository.save(teacher.get());
                return new ResponseEntity<>("Password reset successful!", HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>("Teacher not found with the provided email!", HttpStatus.NOT_FOUND);
            }
        } else if (resetPasswordPayload.getUserRole().equals(UserRole.TRANSLATOR.toString())) {
            Optional<Translator> translator = translatorRepository.findByUserEmail(email);
            if (translator.isPresent()) {
                translator.get().setUserPassword(bCryptPasswordEncoder.encode(resetPasswordPayload.getNewPassword()));
                translatorRepository.save(translator.get());
                return new ResponseEntity<>("Password reset successful!", HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>("Translator not found with the provided email!", HttpStatus.NOT_FOUND);
            }
        } else if (resetPasswordPayload.getUserRole().equals(UserRole.RESEARCHER.toString())) {
            Optional<Researcher> researcher = researcherRepository.findByUserEmail(email);
            if (researcher.isPresent()) {
                researcher.get().setUserPassword(bCryptPasswordEncoder.encode(resetPasswordPayload.getNewPassword()));
                researcherRepository.save(researcher.get());
                return new ResponseEntity<>("Password reset successful!", HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>("Researcher not found with the provided email!", HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>("Invalid user role!", HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> registerStudent(Student student) {
        boolean isValidEmail = emailValidator.test(student.getUserEmail());
        if (!isValidEmail) {
            return new ResponseEntity<>(new APIResponse(INVALID_EMAIL), HttpStatus.BAD_REQUEST);
        }
        boolean isEmailTaken = studentRepository.findByUserEmail(student.getUserEmail()).isPresent();
        if (isEmailTaken) {
            return new ResponseEntity<>(new APIResponse(EMAIL_TAKEN), HttpStatus.CONFLICT);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

        Student newStudent = new Student();
        newStudent.setId(UUID.randomUUID().toString());
        newStudent.setUserEmail(student.getUserEmail());
        newStudent.setUserFullName(student.getUserFullName());
        newStudent.setSchool(student.getSchool());
        newStudent.setDistrict(student.getDistrict());
        newStudent.setGrade(student.getGrade());
        newStudent.setAssignedClass(student.getAssignedClass());
        newStudent.setTeacherName(student.getTeacherName());
        newStudent.setUserRole(UserRole.STUDENT);
        newStudent.setGender(student.getGender());
        newStudent.setDateOfBirth(student.getDateOfBirth());
        newStudent.setPreferredLanguage(student.getPreferredLanguage());
        newStudent.setPersonalInterests(student.getPersonalInterests());
        newStudent.setAccountDeleteStatus(AccountDeleteStatus.NONE);
        newStudent.setAccountStatus(AccountStatus.PENDING);

        String encodedPassword = bCryptPasswordEncoder.encode(student.getPassword());
        newStudent.setPassword(encodedPassword);
        try {
            studentRepository.save(newStudent);
            HttpEntity<Student> httpEntity = new HttpEntity<Student>(newStudent, httpHeaders);
            Map<String, Object> payload = new HashMap<>();
            payload.put("studentEmail", student.getUserEmail());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<Invitation[]> invitationResponseEntity = restTemplate.exchange(
                    INVITATION_SERVICE_HOSTED_URL+"api/invitations/filter", HttpMethod.POST, entity, Invitation[].class);
            if (Objects.requireNonNull(invitationResponseEntity.getBody()).length > 0) {
                HttpEntity<Map<String, Object>> entityUpdate = new HttpEntity<>(null, headers);
                ResponseEntity<Invitation> res = restTemplate.exchange(
                        INVITATION_SERVICE_HOSTED_URL+"api/invitations/" + Objects.requireNonNull(invitationResponseEntity.getBody())[0].getId() + "/status",
                        HttpMethod.PUT, entityUpdate, Invitation.class);
            }
            return new ResponseEntity<>(new APIResponse("Student registered successfully"), HttpStatus.OK);
        } catch (Exception e) {
            log(e.getMessage());
            return new ResponseEntity<>(new APIResponse("Please try again"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> updateStudentAccountStatus(String email, String status) {
        Optional<Student> existingStudent = studentRepository.findByUserEmail(email);
        if (existingStudent.isPresent()) {
            existingStudent.get().setAccountStatus(AccountStatus.valueOf(status));
            studentRepository.save(existingStudent.get());
            return new ResponseEntity<>(existingStudent.map((this::convertToStudentDTO)), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("Student not found!"), HttpStatus.NOT_FOUND);
    }

    private StudentResponse convertToStudentDTO(Student student) {
        StudentResponse studentResponse = new StudentResponse();
        studentResponse.setId(student.getId());
        studentResponse.setUserEmail(student.getUserEmail());
        studentResponse.setUserFullName(student.getUserFullName());
        studentResponse.setSchool(student.getSchool());
        studentResponse.setDistrict(student.getDistrict());
        studentResponse.setGrade(student.getGrade());
        studentResponse.setGender(student.getGender());
        studentResponse.setDateOfBirth(student.getDateOfBirth());
        studentResponse.setAssignedClass(student.getAssignedClass());
        studentResponse.setTeacherName(student.getTeacherName());
        studentResponse.setUserRole(UserRole.STUDENT);
        studentResponse.setPersonalInterests(student.getPersonalInterests());
        studentResponse.setAccountDeleteStatus(student.getAccountDeleteStatus());
        studentResponse.setPreferredLanguage(student.getPreferredLanguage());
        studentResponse.setAccountStatus(student.getAccountStatus());
        studentResponse.setPenpalEmail(student.getPenpalEmail());
        studentResponse.setPenpalGroup(student.getPenpalGroup());
        return studentResponse;
    }

    public ResponseEntity<?> getStudents() {
        List<Student> students = studentRepository.findAll();
        try {
            return new ResponseEntity<List<StudentResponse>>(
                    students.stream().map(this::convertToStudentDTO).collect(Collectors.toList()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> filterStudents(StudentFilter filter) {
        List<Student> students = studentRepository.findAll();
        List<Student> filteredStudents = students.stream().filter(student ->
                        (filter.getSchool() == null || student.getSchool().equals(filter.getSchool())) &&
                                (filter.getGender() == null || student.getGender().equals(filter.getGender())) &&
                                (filter.getTeacherName() == null || student.getTeacherName().equals(filter.getTeacherName())) &&
                                (filter.getGrade() == null || student.getGrade().equals(filter.getGrade())) &&
                                (filter.getDistrict() == null || student.getDistrict().equals(filter.getDistrict())) &&
                                (filter.getPreferredLanguage() == null || student.getPreferredLanguage().equals(filter.getPreferredLanguage())) &&
                                (filter.getAccountDeleteStatus() == null || student.getAccountDeleteStatus().equals(AccountDeleteStatus.valueOf(filter.getAccountDeleteStatus()))) &&
                                (filter.getPenpalGroup() == null || student.getPenpalGroup().equals(PenpalGroup.valueOf(filter.getPenpalGroup()))) &&
                                (filter.getAccountStatus() == null || student.getAccountStatus() == AccountStatus.valueOf(filter.getAccountStatus())))
                .toList();
        try {
            return new ResponseEntity<List<StudentResponse>>(
                    filteredStudents.stream().map(this::convertToStudentDTO).collect(Collectors.toList()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getStudent(String email) {
        Optional<Student> student = studentRepository.findByUserEmail(email);
        if (student.isPresent()) {
            student.get().setResetToken(null);
            studentRepository.save(student.get());
            return new ResponseEntity<>(student.map(this::convertToStudentDTO), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new APIResponse("No student found!"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getStudentsWithoutPenpal() {
        List<Student> students = studentRepository.findAll();
        List<Student> studentsWithoutPenpal = students.stream()
                .filter(student -> student.getPenpalEmail() == null)
                .collect(Collectors.toList());

        return new ResponseEntity<>(studentsWithoutPenpal, HttpStatus.OK);
    }

    public ResponseEntity<?> updateStudentPenpal(String studentEmail, String newPenpalEmail, String penpalGroup) {
        Optional<Student> student = studentRepository.findByUserEmail(studentEmail);
        Optional<Student> penpal = studentRepository.findByUserEmail(newPenpalEmail);
        if (student.isPresent() && penpal.isPresent()) {
            if (student.get().getAccountStatus() == AccountStatus.INACTIVE || student.get().getAccountStatus() == AccountStatus.PENDING)
                return new ResponseEntity<>(new APIResponse("Student " + student.get().getUserFullName() + " is " + student.get().getAccountStatus()), HttpStatus.BAD_REQUEST);

            if (penpal.get().getAccountStatus() == AccountStatus.INACTIVE || penpal.get().getAccountStatus() == AccountStatus.PENDING)
                return new ResponseEntity<>(new APIResponse("Student " + penpal.get().getUserFullName() + " is " + penpal.get().getAccountStatus()), HttpStatus.BAD_REQUEST);

            student.get().setPenpalEmail(newPenpalEmail);
            student.get().setPenpalGroup(PenpalGroup.valueOf(penpalGroup));

            penpal.get().setPenpalEmail(studentEmail);
            penpal.get().setPenpalGroup(PenpalGroup.valueOf(penpalGroup));

            studentRepository.save(student.get());
            studentRepository.save(penpal.get());

            return new ResponseEntity<>(new APIResponse("Penpal updated successfully!"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("One or both students not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> removeStudentPenpal(String studentEmail) {
        Optional<Student> existingStudent = studentRepository.findByUserEmail(studentEmail);
        if (existingStudent.isPresent()) {
            existingStudent.get().setPenpalEmail(null);
            existingStudent.get().setPenpalGroup(null);
            return new ResponseEntity<Student>(studentRepository.save(existingStudent.get()), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("Student not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> updateStudent(Student student, String email) {
        Optional<Student> existingStudent = studentRepository.findByUserEmail(email);

        if (existingStudent.isPresent()) {
            Student studentToUpdate = existingStudent.get();

            if (student.getUserFullName() != null) {
                studentToUpdate.setUserFullName(student.getUserFullName());
            }
            if (student.getPassword() != null) {
                studentToUpdate.setPassword(bCryptPasswordEncoder.encode(student.getPassword()));
            }
            if (student.getGrade() != null) {
                studentToUpdate.setGrade(student.getGrade());
            }
            if (student.getAssignedClass() != null) {
                studentToUpdate.setAssignedClass(student.getAssignedClass());
            }
            if (student.getGender() != null) {
                studentToUpdate.setGender(student.getGender());
            }
            if (student.getDateOfBirth() != null) {
                studentToUpdate.setDateOfBirth(student.getDateOfBirth());
            }
            if (student.getPreferredLanguage() != null) {
                studentToUpdate.setPreferredLanguage(student.getPreferredLanguage());
            }
            if (student.getPersonalInterests() != null) {
                studentToUpdate.setPersonalInterests(student.getPersonalInterests());
            }
            studentRepository.save(studentToUpdate);
            return new ResponseEntity<StudentResponse>(convertToStudentDTO(studentToUpdate), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("Student not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> getStudentProfilePhoto(String email) {
        Optional<Student> student = studentRepository.findByUserEmail(email);
        if (student.isPresent() && student.get().getProfilePicture() != null) {
            byte[] image = student.get().getProfilePicture();
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(image);
        } else {
            return new ResponseEntity<>(new APIResponse("Profile photo not found!"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> updateStudentProfilePhoto(String email, MultipartFile file) throws IOException {
        Optional<Student> existingStudent = studentRepository.findByUserEmail(email);
        if (existingStudent.isPresent()) {
            Student studentToUpdate = existingStudent.get();
            studentToUpdate.setProfilePicture((file.getBytes()));
            studentRepository.save(studentToUpdate);
            return new ResponseEntity<>(new APIResponse("Profile picture updated successfully"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new APIResponse("Student not found!"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> updateStudentAccountDeleteRequest(String email, String status) {
        Optional<Student> existingStudent = studentRepository.findByUserEmail(email);
        if (existingStudent.isPresent()) {
            existingStudent.get().setAccountDeleteStatus(AccountDeleteStatus.valueOf(status));
            studentRepository.save(existingStudent.get());
            return new ResponseEntity<>(new APIResponse("Student delete request updated successfully"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("Student not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> deleteStudent(String email) {
        Optional<Student> existingStudent = studentRepository.findByUserEmail(email);
        if (existingStudent.isPresent()) {
            existingStudent.get().setAccountDeleteStatus(AccountDeleteStatus.ACCEPTED);
            Optional<Student> studentWithPenpal = studentRepository.findByPenpalEmail(email);
            if (studentWithPenpal.isPresent()) {
                studentWithPenpal.get().setPenpalEmail(null);
                studentWithPenpal.get().setPenpalGroup(null);
                studentRepository.save(studentWithPenpal.get());
            }
            studentRepository.delete(existingStudent.get());
            return new ResponseEntity<>(new APIResponse("Student deleted"), HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(new APIResponse("Student not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> registerTeacher(Teacher teacher) {
        boolean isValidEmail = emailValidator.test(teacher.getUserEmail());
        if (!isValidEmail) {
            return new ResponseEntity<>(new APIResponse(INVALID_EMAIL), HttpStatus.BAD_REQUEST);
        }
        boolean isEmailTaken = teacherRepository.findByUserEmail(teacher.getUserEmail()).isPresent();
        if (isEmailTaken) {
            return new ResponseEntity<>(new APIResponse(EMAIL_TAKEN), HttpStatus.CONFLICT);
        }
        Teacher newTeacher = new Teacher();
        newTeacher.setId(UUID.randomUUID().toString());
        newTeacher.setUserEmail(teacher.getUserEmail());
        newTeacher.setUserPassword(teacher.getUserPassword());
        newTeacher.setUserFullName(teacher.getUserFullName());
        newTeacher.setDistrict(teacher.getDistrict());
        newTeacher.setSchool(teacher.getSchool());
        newTeacher.setUserRole(UserRole.TEACHER);
        newTeacher.setAssignedClass(teacher.getAssignedClass());
        if (!teacher.getContactNumber().matches("^\\+?[0-9]*$")) {
            return new ResponseEntity<>(new APIResponse("Invalid contact number format"), HttpStatus.BAD_REQUEST);
        }
        newTeacher.setContactNumber(teacher.getContactNumber());

        String encodedPassword = bCryptPasswordEncoder.encode(newTeacher.getUserPassword());
        newTeacher.setUserPassword(encodedPassword);
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Teacher> httpEntity = new HttpEntity<Teacher>(newTeacher, httpHeaders);
            return new ResponseEntity<Teacher>(teacherRepository.save(newTeacher), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private TeacherResponse convertToTeacherDTO(Teacher teacher) {
        TeacherResponse teacherResponse = new TeacherResponse();
        teacherResponse.setId(teacher.getId());
        teacherResponse.setUserEmail(teacher.getUserEmail());
        teacherResponse.setUserFullName(teacher.getUserFullName());
        teacherResponse.setDistrict(teacher.getDistrict());
        teacherResponse.setSchool(teacher.getSchool());
        teacherResponse.setAssignedClass(teacher.getAssignedClass());
        teacherResponse.setContactNumber(teacher.getContactNumber());
        teacherResponse.setUserRole(UserRole.TEACHER);
        return teacherResponse;
    }

    public ResponseEntity<?> getTeachers() {
        List<Teacher> teachers = teacherRepository.findAll();
        try {
            return new ResponseEntity<List<TeacherResponse>>(
                    teachers.stream().map(this::convertToTeacherDTO).collect(Collectors.toList()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getTeacher(String email) {
        Optional<Teacher> teacher = teacherRepository.findByUserEmail(email);
        if (teacher.isPresent()) {
            teacher.get().setResetToken(null);
            teacherRepository.save(teacher.get());
            return new ResponseEntity<>(teacher.map(this::convertToTeacherDTO), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new APIResponse("No teacher found!"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> filterTeachers(TeacherFilter filter) {
        List<Teacher> teachers = teacherRepository.findAll();
        List<Teacher> filteredTeachers = teachers.stream().filter(teacher ->
                        (filter.getDistrict() == null || teacher.getDistrict().equals(filter.getDistrict())) &&
                                (filter.getSchool() == null || teacher.getSchool().equals(filter.getSchool())) &&
                                (filter.getAssignedClass() == null || teacher.getAssignedClass().equals(filter.getAssignedClass())))
                .toList();
        try {
            return new ResponseEntity<List<TeacherResponse>>(
                    filteredTeachers.stream().map(this::convertToTeacherDTO).collect(Collectors.toList()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> updateTeacher(Teacher teacher, String email) {
        Optional<Teacher> existingStudent = teacherRepository.findByUserEmail(email);

        if (existingStudent.isPresent()) {
            Teacher teacherToUpdate = existingStudent.get();

            if (teacher.getUserFullName() != null) {
                teacherToUpdate.setUserFullName(teacher.getUserFullName());
            }
            if (teacher.getUserPassword() != null) {
                teacherToUpdate.setUserPassword(bCryptPasswordEncoder.encode(teacher.getUserPassword()));
            }
            if (teacher.getDistrict() != null) {
                teacherToUpdate.setDistrict(teacher.getDistrict());
            }
            if (teacher.getSchool() != null) {
                teacherToUpdate.setSchool(teacher.getSchool());
            }
            if (teacher.getAssignedClass() != null) {
                teacherToUpdate.setAssignedClass(teacher.getAssignedClass());
            }
            if (teacher.getContactNumber() != null) {
                if (!teacher.getContactNumber().matches("^\\+?[0-9]*$")) {
                    return new ResponseEntity<>(new APIResponse("Invalid contact number format"), HttpStatus.BAD_REQUEST);
                }
                teacherToUpdate.setContactNumber(teacher.getContactNumber());
            }
            return new ResponseEntity<Teacher>(teacherRepository.save(teacherToUpdate), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new APIResponse("Teacher not found!"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> deleteTeacher(String email) {
        Optional<Teacher> existingTeacher = teacherRepository.findByUserEmail(email);
        if (existingTeacher.isPresent()) {
            teacherRepository.delete(existingTeacher.get());
            return new ResponseEntity<>(new APIResponse("Teacher deleted"), HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(new APIResponse("Teacher not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> getDistrictHierarchy(String userRole) {
        List<String> districts = teacherRepository.findAll()
                .stream()
                .map(Teacher::getDistrict)
                .distinct()
                .toList();

        List<DistrictHierarchy> districtHierarchy = new ArrayList<>();

        for (String district : districts) {
            DistrictHierarchy districtData = new DistrictHierarchy();
            districtData.setDistrict(district);

            List<Teacher> teachers = teacherRepository.findByDistrict(district);

            Map<String, List<Teacher>> schoolTeacherMap = teachers.stream()
                    .collect(Collectors.groupingBy(Teacher::getSchool));

            for (Map.Entry<String, List<Teacher>> entry : schoolTeacherMap.entrySet()) {
                DistrictHierarchy.SchoolHierarchy schoolData = new DistrictHierarchy.SchoolHierarchy();
                schoolData.setSchool(entry.getKey());

                List<DistrictHierarchy.TeacherHierarchy> teacherHierarchies = new ArrayList<>();

                for (Teacher teacher : entry.getValue()) {
                    DistrictHierarchy.TeacherHierarchy teacherData = new DistrictHierarchy.TeacherHierarchy();
                    teacherData.setTeacherName(teacher.getUserFullName());
                    teacherData.setAssignedClass(teacher.getAssignedClass());

                    if (!"STUDENT".equalsIgnoreCase(userRole)) {
                        List<Student> students = studentRepository.findByAssignedClass(teacher.getAssignedClass());
                        teacherData.setStudents(students.stream()
                                .map(this::convertToStudentDTO)
                                .collect(Collectors.toList()));
                    }
                    teacherHierarchies.add(teacherData);
                }
                schoolData.setTeachers(teacherHierarchies);
                districtData.getSchools().add(schoolData);
            }

            districtHierarchy.add(districtData);
        }
        return new ResponseEntity<>(districtHierarchy, HttpStatus.OK);
    }

    public ResponseEntity<?> registerTranslator(Translator translator) {
        boolean isValidEmail = emailValidator.test(translator.getUserEmail());
        if (!isValidEmail) {
            return new ResponseEntity<>(new APIResponse(INVALID_EMAIL), HttpStatus.BAD_REQUEST);
        }
        boolean isEmailTaken = translatorRepository.findByUserEmail(translator.getUserEmail()).isPresent();
        if (isEmailTaken) {
            return new ResponseEntity<>(new APIResponse(EMAIL_TAKEN), HttpStatus.CONFLICT);
        }
        Translator newTranslator = new Translator();
        newTranslator.setId(UUID.randomUUID().toString());
        newTranslator.setUserEmail(translator.getUserEmail());
        newTranslator.setUserPassword(translator.getUserPassword());
        newTranslator.setUserFullName(translator.getUserFullName());
        newTranslator.setUserRole(UserRole.TRANSLATOR);
        if (!translator.getContactNumber().matches("^\\+?[0-9]*$")) {
            return new ResponseEntity<>(new APIResponse("Invalid contact number format"), HttpStatus.BAD_REQUEST);
        }
        newTranslator.setContactNumber(translator.getContactNumber());

        String encodedPassword = bCryptPasswordEncoder.encode(newTranslator.getUserPassword());
        newTranslator.setUserPassword(encodedPassword);
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Translator> httpEntity = new HttpEntity<Translator>(newTranslator, httpHeaders);
            return new ResponseEntity<Translator>(translatorRepository.save(newTranslator), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private TranslatorResponse convertToTranslatorDTO(Translator translator) {
        TranslatorResponse translatorResponse = new TranslatorResponse();
        translatorResponse.setId(translator.getId());
        translatorResponse.setUserEmail(translator.getUserEmail());
        translatorResponse.setUserFullName(translator.getUserFullName());
        translatorResponse.setContactNumber(translator.getContactNumber());
        translatorResponse.setUserRole(UserRole.TRANSLATOR);
        return translatorResponse;
    }

    public ResponseEntity<?> getTranslators() {
        List<Translator> translators = translatorRepository.findAll();
        try {
            return new ResponseEntity<List<TranslatorResponse>>(
                    translators.stream().map(this::convertToTranslatorDTO).collect(Collectors.toList()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getTranslator(String email) {
        Optional<Translator> translator = translatorRepository.findByUserEmail(email);
        if (translator.isPresent()) {
            translator.get().setResetToken(null);
            return new ResponseEntity<>(translator.map(this::convertToTranslatorDTO), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new APIResponse("No translator found!"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> updateTranslator(Translator translator, String email) {
        Optional<Translator> existingTranslator = translatorRepository.findByUserEmail(email);

        if (existingTranslator.isPresent()) {
            Translator translatorToUpdate = existingTranslator.get();

            if (translator.getUserFullName() != null) {
                translatorToUpdate.setUserFullName(translator.getUserFullName());
            }
            if (translator.getUserPassword() != null) {
                translatorToUpdate.setUserPassword(bCryptPasswordEncoder.encode(translator.getUserPassword()));
            }
            if (translator.getContactNumber() != null) {
                if (!translator.getContactNumber().matches("^\\+?[0-9]*$")) {
                    return new ResponseEntity<>(new APIResponse("Invalid contact number format"), HttpStatus.BAD_REQUEST);
                }
                translatorToUpdate.setContactNumber(translator.getContactNumber());
            }
            return new ResponseEntity<Translator>(translatorRepository.save(translatorToUpdate), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new APIResponse("Translator not found!"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> deleteTranslator(String email) {
        Optional<Translator> existingTeacher = translatorRepository.findByUserEmail(email);
        if (existingTeacher.isPresent()) {
            translatorRepository.delete(existingTeacher.get());
            return new ResponseEntity<>(new APIResponse("Translator deleted"), HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(new APIResponse("Translator not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> registerResearcher(Researcher researcher) {
        boolean isValidEmail = emailValidator.test(researcher.getUserEmail());
        if (!isValidEmail) {
            return new ResponseEntity<>(new APIResponse(INVALID_EMAIL), HttpStatus.BAD_REQUEST);
        }
        boolean isEmailTaken = researcherRepository.findByUserEmail(researcher.getUserEmail()).isPresent();
        if (isEmailTaken) {
            return new ResponseEntity<>(new APIResponse(EMAIL_TAKEN), HttpStatus.CONFLICT);
        }
        Researcher newResearcher = new Researcher();
        newResearcher.setId(UUID.randomUUID().toString());
        newResearcher.setUserEmail(researcher.getUserEmail());
        newResearcher.setUserPassword(researcher.getUserPassword());
        newResearcher.setUserFullName(researcher.getUserFullName());
        newResearcher.setUserRole(UserRole.RESEARCHER);
        if (!researcher.getContactNumber().matches("^\\+?[0-9]*$")) {
            return new ResponseEntity<>(new APIResponse("Invalid contact number format"), HttpStatus.BAD_REQUEST);
        }
        newResearcher.setContactNumber(researcher.getContactNumber());

        String encodedPassword = bCryptPasswordEncoder.encode(newResearcher.getUserPassword());
        newResearcher.setUserPassword(encodedPassword);
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Researcher> httpEntity = new HttpEntity<Researcher>(newResearcher, httpHeaders);
            return new ResponseEntity<Researcher>(researcherRepository.save(newResearcher), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResearcherResponse convertToResearchDTO(Researcher researcher) {
        ResearcherResponse researcherResponse = new ResearcherResponse();
        researcherResponse.setId(researcher.getId());
        researcherResponse.setUserEmail(researcher.getUserEmail());
        researcherResponse.setUserFullName(researcher.getUserFullName());
        researcherResponse.setContactNumber(researcher.getContactNumber());
        researcherResponse.setUserRole(UserRole.RESEARCHER);
        return researcherResponse;
    }

    public ResponseEntity<?> getResearchers() {
        List<Researcher> researchers = researcherRepository.findAll();
        try {
            return new ResponseEntity<List<ResearcherResponse>>(
                    researchers.stream().map(this::convertToResearchDTO).collect(Collectors.toList()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getResearcher(String email) {
        Optional<Researcher> researcher = researcherRepository.findByUserEmail(email);
        if (researcher.isPresent()) {
            researcher.get().setResetToken(null);
            return new ResponseEntity<>(researcher.map(this::convertToResearchDTO), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new APIResponse("No researcher found!"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> updateResearcher(Researcher researcher, String email) {
        Optional<Researcher> existingResearcher = researcherRepository.findByUserEmail(email);

        if (existingResearcher.isPresent()) {
            Researcher researcherToUpdate = existingResearcher.get();

            if (researcher.getUserFullName() != null) {
                researcherToUpdate.setUserFullName(researcher.getUserFullName());
            }
            if (researcher.getUserPassword() != null) {
                researcherToUpdate.setUserPassword(bCryptPasswordEncoder.encode(researcher.getUserPassword()));
            }
            if (researcher.getContactNumber() != null) {
                if (!researcher.getContactNumber().matches("^\\+?[0-9]*$")) {
                    return new ResponseEntity<>(new APIResponse("Invalid contact number format"), HttpStatus.BAD_REQUEST);
                }
                researcherToUpdate.setContactNumber(researcher.getContactNumber());
            }
            return new ResponseEntity<Researcher>(researcherRepository.save(researcherToUpdate), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new APIResponse("Researcher not found!"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> deleteResearcher(String email) {
        Optional<Researcher> existingResearcher = researcherRepository.findByUserEmail(email);
        if (existingResearcher.isPresent()) {
            researcherRepository.delete(existingResearcher.get());
            return new ResponseEntity<>(new APIResponse("Researcher deleted"), HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(new APIResponse("Researcher not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> uploadPenpalMatching(MultipartFile file) throws IOException {
        if (!file.getOriginalFilename().endsWith(".xlsx") && !file.getOriginalFilename().endsWith(".csv")) {
            return new ResponseEntity<>(new APIResponse("Invalid file type! Only .xlsx and .csv are supported."), HttpStatus.BAD_REQUEST);
        }

        List<String> errors = new ArrayList<>();
        boolean isExcel = file.getOriginalFilename().endsWith(".xlsx");

        List<List<String>> rows = isExcel ? readExcelFile(file) : readCsvFile(file);

        for (List<String> row : rows) {
            if (row.size() != 3) {
                errors.add("Invalid row format: " + row);
                continue;
            }

            String student1 = row.get(0);
            String student2 = row.get(1);
            String penpalGroup = row.get(2);

            try {
                ResponseEntity<?> response = updateStudentPenpal(student1, student2, penpalGroup);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    errors.add("Failed to update penpal for " + student1 + " and " + student2);
                }
            } catch (Exception e) {
                errors.add("Failed to update penpal for " + student1 + " and " + student2 + ": " + e.getMessage());
            }
        }

        if (errors.isEmpty()) {
            return new ResponseEntity<>(new APIResponse("Penpal matching updated successfully!"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("Some errors occurred: " + String.join(", ", errors)), HttpStatus.PARTIAL_CONTENT);
    }

    private List<List<String>> readExcelFile(MultipartFile file) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    rowData.add(cell.toString());
                }
                rows.add(rowData);
            }
        }
        return rows;
    }

    private List<List<String>> readCsvFile(MultipartFile file) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVReader csvReader = new CSVReader(reader)) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                rows.add(Arrays.asList(values));
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }

    public ResponseEntity<?> addSchool(String name, String district) {
        School school = new School();
        school.setName(name);
        school.setDistrict(district);
        schoolRepository.save(school);
        return new ResponseEntity<>(new APIResponse("School added successfully!"), HttpStatus.CREATED);
    }

    public ResponseEntity<?> getSchool(String id) {
        Optional<School> school = schoolRepository.findById(id);
        if (school.isPresent()) {
            return new ResponseEntity<>(school.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("School not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> updateSchool(String id, String updatedName) {
        Optional<School> school = schoolRepository.findById(id);
        if (school.isPresent()) {
            school.get().setName(updatedName);
            schoolRepository.save(school.get());
            return new ResponseEntity<>(new APIResponse("School updated successfully!"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("School not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> deleteSchool(String id) {
        Optional<School> school = schoolRepository.findById(id);
        if (school.isPresent()) {
            schoolRepository.delete(school.get());
            return new ResponseEntity<>(new APIResponse("School deleted successfully!"), HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(new APIResponse("School not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> getSchoolsByDistrict(String district) {
        Optional<List<School>> schools = schoolRepository.findByDistrict(district);
        if (schools.isPresent()) {
            return new ResponseEntity<>(schools.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("District not found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> getDistrictSchoolHierarchy() {
        List<School> schools = schoolRepository.findAll();

        Map<String, List<School>> districtSchoolMap = schools.stream()
                .collect(Collectors.groupingBy(School::getDistrict));

        List<DistrictSchoolHierarchy> districtHierarchy = districtSchoolMap.entrySet().stream()
                .map(entry -> {
                    String district = entry.getKey();
                    List<School> schoolList = entry.getValue();

                    DistrictSchoolHierarchy districtData = new DistrictSchoolHierarchy();
                    districtData.setDistrict(district);

                    List<DistrictSchoolHierarchy.SchoolData> schoolDataList = schoolList.stream()
                            .map(school -> {
                                DistrictSchoolHierarchy.SchoolData schoolData = new DistrictSchoolHierarchy.SchoolData();
                                schoolData.setSchoolName(school.getName());
                                return schoolData;
                            })
                            .collect(Collectors.toList());

                    districtData.setSchools(schoolDataList);
                    return districtData;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(districtHierarchy, HttpStatus.OK);
    }
}
