package com.parwath.sms.repository;

import com.parwath.sms.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByRollNumber(String rollNumber);

    Optional<Student> findByEmail(String email);

    boolean existsByRollNumber(String rollNumber);

    boolean existsByEmail(String email);

    boolean existsByRollNumberAndIdNot(String rollNumber, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("""
        SELECT s FROM Student s
        WHERE (:name IS NULL OR :name = '' OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:department IS NULL OR :department = '' OR LOWER(s.department) = LOWER(:department))
          AND (:email IS NULL OR :email = '' OR LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%')))
          AND (:year IS NULL OR s.yearOfStudy = :year)
    """)
    Page<Student> searchStudents(
        @Param("name") String name,
        @Param("department") String department,
        @Param("email") String email,
        @Param("year") Integer year,
        Pageable pageable
    );
}
