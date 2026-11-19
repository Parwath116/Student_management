package com.parwath.sms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequest {

    @NotBlank(message = "Roll number is required")
    @Size(min = 2, max = 50, message = "Roll number must be between 2 and 50 characters")
    private String rollNumber;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    @NotBlank(message = "Department is required")
    @Size(max = 80, message = "Department must not exceed 80 characters")
    private String department;

    @NotNull(message = "Year of study is required")
    @Min(value = 1, message = "Year of study must be between 1 and 4")
    @Max(value = 4, message = "Year of study must be between 1 and 4")
    private Integer yearOfStudy;

    @Past(message = "Date of birth must be a past date")
    private LocalDate dateOfBirth;
}
