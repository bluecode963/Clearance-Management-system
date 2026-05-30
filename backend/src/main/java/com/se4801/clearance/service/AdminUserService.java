package com.se4801.clearance.service;

import com.se4801.clearance.dto.request.AdminCreateUserRequest;
import com.se4801.clearance.dto.response.UserResponse;
import com.se4801.clearance.exception.BusinessRuleException;
import com.se4801.clearance.exception.ResourceNotFoundException;
import com.se4801.clearance.mapper.UserMapper;
import com.se4801.clearance.model.Office;
import com.se4801.clearance.model.Role;
import com.se4801.clearance.model.StudentProfile;
import com.se4801.clearance.model.User;
import com.se4801.clearance.repository.OfficeRepository;
import com.se4801.clearance.repository.StudentProfileRepository;
import com.se4801.clearance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final OfficeRepository officeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(AdminCreateUserRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException("Email is already registered");
        }

        validateRoleSpecificFields(request);

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .office(resolveOffice(request))
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        if (request.role() == Role.STUDENT) {
            StudentProfile profile = StudentProfile.builder()
                    .studentId(request.studentId().trim())
                    .department(request.department().trim())
                    .program(request.program().trim())
                    .yearOfStudy(request.yearOfStudy())
                    .user(savedUser)
                    .build();
            studentProfileRepository.save(profile);
        }

        return UserMapper.toResponse(savedUser);
    }

    private void validateRoleSpecificFields(AdminCreateUserRequest request) {
        if (request.role() == Role.STUDENT) {
            if (isBlank(request.studentId())) {
                throw new BusinessRuleException("Student ID is required for student users");
            }
            if (isBlank(request.department())) {
                throw new BusinessRuleException("Department is required for student users");
            }
            if (isBlank(request.program())) {
                throw new BusinessRuleException("Program is required for student users");
            }
            if (request.yearOfStudy() == null) {
                throw new BusinessRuleException("Year of study is required for student users");
            }
            if (studentProfileRepository.findByStudentId(request.studentId().trim()).isPresent()) {
                throw new BusinessRuleException("Student ID is already registered");
            }
        }

        if (request.role() == Role.OFFICE_STAFF && request.officeId() == null) {
            throw new BusinessRuleException("Office is required for office staff users");
        }
    }

    private Office resolveOffice(AdminCreateUserRequest request) {
        if (request.role() != Role.OFFICE_STAFF) {
            return null;
        }
        return officeRepository.findById(request.officeId())
                .orElseThrow(() -> new ResourceNotFoundException("Office not found"));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
