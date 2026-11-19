package com.parwath.sms.mapper;

import com.parwath.sms.dto.StudentRequest;
import com.parwath.sms.dto.StudentResponse;
import com.parwath.sms.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public Student toEntity(StudentRequest request) {
        if (request == null) {
            return null;
        }

        return Student.builder()
                .rollNumber(request.getRollNumber().trim())
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .department(request.getDepartment().trim())
                .yearOfStudy(request.getYearOfStudy())
                .dateOfBirth(request.getDateOfBirth())
                .build();
    }

    public StudentResponse toResponse(Student student) {
        if (student == null) {
            return null;
        }

        return StudentResponse.builder()
                .id(student.getId())
                .rollNumber(student.getRollNumber())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .department(student.getDepartment())
                .yearOfStudy(student.getYearOfStudy())
                .dateOfBirth(student.getDateOfBirth())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }

    public void updateEntity(Student student, StudentRequest request) {
        if (student == null || request == null) {
            return;
        }

        student.setRollNumber(request.getRollNumber().trim());
        student.setFirstName(request.getFirstName().trim());
        student.setLastName(request.getLastName().trim());
        student.setEmail(request.getEmail().trim().toLowerCase());
        student.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        student.setDepartment(request.getDepartment().trim());
        student.setYearOfStudy(request.getYearOfStudy());
        student.setDateOfBirth(request.getDateOfBirth());
    }
}
