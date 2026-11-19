package com.parwath.sms.service;

import com.parwath.sms.dto.PageResponse;
import com.parwath.sms.dto.StudentRequest;
import com.parwath.sms.dto.StudentResponse;
import com.parwath.sms.entity.Student;
import com.parwath.sms.exception.DuplicateResourceException;
import com.parwath.sms.exception.ResourceNotFoundException;
import com.parwath.sms.mapper.StudentMapper;
import com.parwath.sms.repository.StudentRepository;
import com.parwath.sms.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student student;
    private StudentRequest studentRequest;
    private StudentResponse studentResponse;

    @BeforeEach
    void setUp() {
        student = Student.builder()
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

        studentRequest = StudentRequest.builder()
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
    @DisplayName("createStudent: success when rollNumber and email are unique")
    void createStudent_success() {
        when(studentRepository.existsByRollNumber("CS2026001")).thenReturn(false);
        when(studentRepository.existsByEmail("arjun.kumar@example.com")).thenReturn(false);
        when(studentMapper.toEntity(studentRequest)).thenReturn(student);
        when(studentRepository.save(student)).thenReturn(student);
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        StudentResponse created = studentService.createStudent(studentRequest);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getRollNumber()).isEqualTo("CS2026001");
        verify(studentRepository).save(student);
    }

    @Test
    @DisplayName("createStudent: throws DuplicateResourceException when rollNumber already exists")
    void createStudent_duplicateRollNumber_throwsException() {
        when(studentRepository.existsByRollNumber("CS2026001")).thenReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(studentRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CS2026001");

        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("createStudent: throws DuplicateResourceException when email already exists")
    void createStudent_duplicateEmail_throwsException() {
        when(studentRepository.existsByRollNumber("CS2026001")).thenReturn(false);
        when(studentRepository.existsByEmail("arjun.kumar@example.com")).thenReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(studentRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("arjun.kumar@example.com");

        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("getStudentById: success when student exists")
    void getStudentById_success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        StudentResponse result = studentService.getStudentById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Arjun");
    }

    @Test
    @DisplayName("getStudentById: throws ResourceNotFoundException when student does not exist")
    void getStudentById_notFound_throwsException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getAllStudents: returns paginated list of students")
    void getAllStudents_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> page = new PageImpl<>(List.of(student), pageable, 1);

        when(studentRepository.findAll(pageable)).thenReturn(page);
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        PageResponse<StudentResponse> response = studentService.getAllStudents(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("updateStudent: success when valid and no duplicate conflicts")
    void updateStudent_success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByRollNumberAndIdNot("CS2026001", 1L)).thenReturn(false);
        when(studentRepository.existsByEmailAndIdNot("arjun.kumar@example.com", 1L)).thenReturn(false);
        when(studentRepository.save(student)).thenReturn(student);
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        StudentResponse updated = studentService.updateStudent(1L, studentRequest);

        assertThat(updated).isNotNull();
        verify(studentMapper).updateEntity(student, studentRequest);
        verify(studentRepository).save(student);
    }

    @Test
    @DisplayName("updateStudent: throws ResourceNotFoundException when student does not exist")
    void updateStudent_notFound_throwsException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.updateStudent(99L, studentRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStudent: throws DuplicateResourceException when rollNumber belongs to another student")
    void updateStudent_duplicateRollNumber_throwsException() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByRollNumberAndIdNot("CS2026001", 1L)).thenReturn(true);

        assertThatThrownBy(() -> studentService.updateStudent(1L, studentRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CS2026001");

        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStudent: throws DuplicateResourceException when email belongs to another student")
    void updateStudent_duplicateEmail_throwsException() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByRollNumberAndIdNot("CS2026001", 1L)).thenReturn(false);
        when(studentRepository.existsByEmailAndIdNot("arjun.kumar@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> studentService.updateStudent(1L, studentRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("arjun.kumar@example.com");

        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteStudent: success when student exists")
    void deleteStudent_success() {
        when(studentRepository.existsById(1L)).thenReturn(true);

        studentService.deleteStudent(1L);

        verify(studentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteStudent: throws ResourceNotFoundException when student does not exist")
    void deleteStudent_notFound_throwsException() {
        when(studentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> studentService.deleteStudent(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(studentRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("searchStudents: returns matching results with filters")
    void searchStudents_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> page = new PageImpl<>(List.of(student), pageable, 1);

        when(studentRepository.searchStudents(eq("Arjun"), eq("Computer Science"), any(), eq(3), eq(pageable)))
                .thenReturn(page);
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        PageResponse<StudentResponse> response = studentService.searchStudents(
                "Arjun", "Computer Science", null, 3, pageable
        );

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().getRollNumber()).isEqualTo("CS2026001");
    }
}
