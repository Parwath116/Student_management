package com.parwath.sms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parwath.sms.dto.PageResponse;
import com.parwath.sms.dto.StudentRequest;
import com.parwath.sms.dto.StudentResponse;
import com.parwath.sms.entity.Student;
import com.parwath.sms.exception.DuplicateResourceException;
import com.parwath.sms.exception.GlobalExceptionHandler;
import com.parwath.sms.exception.ResourceNotFoundException;
import com.parwath.sms.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
@Import(GlobalExceptionHandler.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    private StudentRequest validRequest;
    private StudentResponse studentResponse;

    @BeforeEach
    void setUp() {
        validRequest = StudentRequest.builder()
                .rollNumber("CS2026001")
                .firstName("Arjun")
                .lastName("Kumar")
                .email("arjun.kumar@example.com")
                .phone("9876543210")
                .department("Computer Science")
                .yearOfStudy(3)
                .dateOfBirth(LocalDate.of(2003, 5, 15))
                .build();

        studentResponse = StudentResponse.builder()
                .id(1L)
                .rollNumber("CS2026001")
                .firstName("Arjun")
                .lastName("Kumar")
                .email("arjun.kumar@example.com")
                .phone("9876543210")
                .department("Computer Science")
                .yearOfStudy(3)
                .dateOfBirth(LocalDate.of(2003, 5, 15))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/students: 201 Created on valid request")
    void createStudent_success_returns201() throws Exception {
        when(studentService.createStudent(any(StudentRequest.class))).thenReturn(studentResponse);

        mockMvc.perform(post("/api/v1/students")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.rollNumber", is("CS2026001")))
                .andExpect(jsonPath("$.firstName", is("Arjun")))
                .andExpect(jsonPath("$.email", is("arjun.kumar@example.com")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/students: 400 Bad Request with fieldErrors on invalid request")
    void createStudent_validationFails_returns400WithErrorResponse() throws Exception {
        StudentRequest invalidRequest = StudentRequest.builder()
                .rollNumber("") // blank
                .firstName("")  // blank
                .lastName("Kumar")
                .email("invalid-email") // invalid email
                .phone("123") // not 10 digits
                .department("") // blank
                .yearOfStudy(5) // > 4
                .dateOfBirth(LocalDate.now().plusDays(1)) // future
                .build();

        mockMvc.perform(post("/api/v1/students")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Validation failed for one or more fields")))
                .andExpect(jsonPath("$.path", is("/api/v1/students")))
                .andExpect(jsonPath("$.fieldErrors", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors", hasKey("rollNumber")))
                .andExpect(jsonPath("$.fieldErrors", hasKey("firstName")))
                .andExpect(jsonPath("$.fieldErrors", hasKey("email")))
                .andExpect(jsonPath("$.fieldErrors", hasKey("phone")))
                .andExpect(jsonPath("$.fieldErrors", hasKey("department")))
                .andExpect(jsonPath("$.fieldErrors", hasKey("yearOfStudy")))
                .andExpect(jsonPath("$.fieldErrors", hasKey("dateOfBirth")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /api/v1/students/{id}: 200 OK when found")
    void getStudentById_success_returns200() throws Exception {
        when(studentService.getStudentById(1L)).thenReturn(studentResponse);

        mockMvc.perform(get("/api/v1/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.rollNumber", is("CS2026001")))
                .andExpect(jsonPath("$.department", is("Computer Science")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /api/v1/students/{id}: 404 Not Found when missing")
    void getStudentById_notFound_returns404() throws Exception {
        when(studentService.getStudentById(999L))
                .thenThrow(new ResourceNotFoundException("Student with id 999 not found"));

        mockMvc.perform(get("/api/v1/students/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Student with id 999 not found")))
                .andExpect(jsonPath("$.path", is("/api/v1/students/999")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /api/v1/students: 200 OK with paginated response")
    void getAllStudents_returns200AndPageResponse() throws Exception {
        PageResponse<StudentResponse> pageResponse = PageResponse.<StudentResponse>builder()
                .content(List.of(studentResponse))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isFirst(true)
                .isLast(true)
                .build();

        when(studentService.getAllStudents(any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/students?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rollNumber", is("CS2026001")))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.pageNumber", is(0)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("PUT /api/v1/students/{id}: 200 OK on successful update")
    void updateStudent_success_returns200() throws Exception {
        when(studentService.updateStudent(eq(1L), any(StudentRequest.class))).thenReturn(studentResponse);

        mockMvc.perform(put("/api/v1/students/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.rollNumber", is("CS2026001")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("PUT /api/v1/students/{id}: 409 Conflict when duplicate resource exists")
    void updateStudent_duplicateConflict_returns409() throws Exception {
        when(studentService.updateStudent(eq(1L), any(StudentRequest.class)))
                .thenThrow(new DuplicateResourceException("Student with email 'arjun.kumar@example.com' already exists"));

        mockMvc.perform(put("/api/v1/students/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("Student with email 'arjun.kumar@example.com' already exists")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/students/{id}: 204 No Content on successful deletion")
    void deleteStudent_success_returns204() throws Exception {
        doNothing().when(studentService).deleteStudent(1L);

        mockMvc.perform(delete("/api/v1/students/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/students/{id}: 404 Not Found when student does not exist")
    void deleteStudent_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Student with id 999 not found"))
                .when(studentService).deleteStudent(999L);

        mockMvc.perform(delete("/api/v1/students/999")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Student with id 999 not found")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /api/v1/students/search: 200 OK with query parameters")
    void searchStudents_returns200() throws Exception {
        PageResponse<StudentResponse> pageResponse = PageResponse.<StudentResponse>builder()
                .content(List.of(studentResponse))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isFirst(true)
                .isLast(true)
                .build();

        when(studentService.searchStudents(eq("Arjun"), eq("Computer Science"), eq("arjun@example.com"), eq(3), any(Pageable.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/students/search")
                        .param("name", "Arjun")
                        .param("department", "Computer Science")
                        .param("email", "arjun@example.com")
                        .param("year", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName", is("Arjun")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Type mismatch returns 400 Bad Request with descriptive message")
    void typeMismatch_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/students/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Parameter 'id' should be of type 'Long'")));
    }

    @Test
    @DisplayName("Unauthenticated request to /api/v1/students returns 401 Unauthorized")
    void unauthenticatedRequest_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Unknown path returns 404 Not Found with ErrorResponse")
    void unknownPath_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/students/unknown/nonexistent/path"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("The requested resource was not found")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Unsupported HTTP method returns 405 Method Not Allowed with ErrorResponse")
    void methodNotAllowed_returns405() throws Exception {
        mockMvc.perform(post("/api/v1/students/1")
                        .with(csrf()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status", is(405)))
                .andExpect(jsonPath("$.error", is("Method Not Allowed")))
                .andExpect(jsonPath("$.message", containsString("Request method 'POST' is not supported")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Invalid sort property returns 400 Bad Request with ErrorResponse")
    void badSortParameter_returns400() throws Exception {
        when(studentService.getAllStudents(any(Pageable.class)))
                .thenThrow(new PropertyReferenceException("invalidField", ClassTypeInformation.from(Student.class), Collections.emptyList()));

        mockMvc.perform(get("/api/v1/students?sort=invalidField,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Invalid property 'invalidField' for entity 'Student'")));
    }
}
