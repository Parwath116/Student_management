package com.parwath.sms.service;

import com.parwath.sms.dto.PageResponse;
import com.parwath.sms.dto.StudentRequest;
import com.parwath.sms.dto.StudentResponse;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    StudentResponse createStudent(StudentRequest request);

    StudentResponse getStudentById(Long id);

    PageResponse<StudentResponse> getAllStudents(Pageable pageable);

    StudentResponse updateStudent(Long id, StudentRequest request);

    void deleteStudent(Long id);

    PageResponse<StudentResponse> searchStudents(String name, String department, String email, Integer year, Pageable pageable);
}
