package com.parwath.sms.service.impl;

import com.parwath.sms.dto.PageResponse;
import com.parwath.sms.dto.StudentRequest;
import com.parwath.sms.dto.StudentResponse;
import com.parwath.sms.entity.Student;
import com.parwath.sms.exception.DuplicateResourceException;
import com.parwath.sms.exception.ResourceNotFoundException;
import com.parwath.sms.mapper.StudentMapper;
import com.parwath.sms.repository.StudentRepository;
import com.parwath.sms.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        String rollNumber = request.getRollNumber().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (studentRepository.existsByRollNumber(rollNumber)) {
            throw new DuplicateResourceException("Student with roll number '" + rollNumber + "' already exists");
        }

        if (studentRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Student with email '" + email + "' already exists");
        }

        Student student = studentMapper.toEntity(request);
        Student savedStudent = studentRepository.save(student);
        return studentMapper.toResponse(savedStudent);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id " + id + " not found"));
        return studentMapper.toResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> getAllStudents(Pageable pageable) {
        Page<Student> studentPage = studentRepository.findAll(pageable);
        return PageResponse.from(studentPage.map(studentMapper::toResponse));
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id " + id + " not found"));

        String rollNumber = request.getRollNumber().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (studentRepository.existsByRollNumberAndIdNot(rollNumber, id)) {
            throw new DuplicateResourceException("Student with roll number '" + rollNumber + "' already exists");
        }

        if (studentRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateResourceException("Student with email '" + email + "' already exists");
        }

        studentMapper.updateEntity(existingStudent, request);
        Student updatedStudent = studentRepository.save(existingStudent);
        return studentMapper.toResponse(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student with id " + id + " not found");
        }
        studentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> searchStudents(String name, String department, String email, Integer year, Pageable pageable) {
        String trimmedName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String trimmedDepartment = (department != null && !department.trim().isEmpty()) ? department.trim() : null;
        String trimmedEmail = (email != null && !email.trim().isEmpty()) ? email.trim() : null;

        Page<Student> studentPage = studentRepository.searchStudents(
                trimmedName,
                trimmedDepartment,
                trimmedEmail,
                year,
                pageable
        );
        return PageResponse.from(studentPage.map(studentMapper::toResponse));
    }
}
