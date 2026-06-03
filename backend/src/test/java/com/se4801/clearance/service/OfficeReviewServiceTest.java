package com.se4801.clearance.service;

import com.se4801.clearance.dto.request.ReviewClearanceStepRequest;
import com.se4801.clearance.dto.request.ReviewDecision;
import com.se4801.clearance.exception.BusinessRuleException;
import com.se4801.clearance.exception.ResourceNotFoundException;
import com.se4801.clearance.model.ClearanceRequest;
import com.se4801.clearance.model.ClearanceRequestStatus;
import com.se4801.clearance.model.ClearanceStep;
import com.se4801.clearance.model.ClearanceStepStatus;
import com.se4801.clearance.model.ClearanceType;
import com.se4801.clearance.model.Office;
import com.se4801.clearance.model.Role;
import com.se4801.clearance.model.StudentProfile;
import com.se4801.clearance.model.User;
import com.se4801.clearance.repository.ApprovalLogRepository;
import com.se4801.clearance.repository.ClearanceRequestRepository;
import com.se4801.clearance.repository.ClearanceStepRepository;
import com.se4801.clearance.repository.UserRepository;
import com.se4801.clearance.security.CustomUserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfficeReviewServiceTest {

    @Mock
    private ClearanceStepRepository clearanceStepRepository;

    @Mock
    private ClearanceRequestRepository clearanceRequestRepository;

    @Mock
    private ApprovalLogRepository approvalLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OfficeReviewService officeReviewService;

    @Test
    void officeStaffCanApproveOwnOfficeStep() {
        Office library = office(1L, "Library");
        User staff = officeStaff(library);
        ClearanceRequest request = clearanceRequest();
        ClearanceStep step = step(10L, request, library, ClearanceStepStatus.PENDING);
        ClearanceStep registrarStep = step(11L, request, office(2L, "Registrar"), ClearanceStepStatus.WAITING);

        when(userRepository.findWithOfficeById(2L)).thenReturn(Optional.of(staff));
        when(clearanceStepRepository.findByIdAndOfficeId(10L, 1L)).thenReturn(Optional.of(step));
        when(clearanceStepRepository.save(any(ClearanceStep.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clearanceStepRepository.findByClearanceRequestIdOrderByOfficeIdAsc(99L))
                .thenReturn(List.of(step, registrarStep));

        officeReviewService.reviewStep(
                10L,
                new ReviewClearanceStepRequest(ReviewDecision.APPROVED, "Cleared"),
                principal(staff)
        );

        assertThat(step.getStatus()).isEqualTo(ClearanceStepStatus.APPROVED);
        assertThat(step.getComment()).isEqualTo("Cleared");
        assertThat(step.getReviewedBy()).isEqualTo(staff);
        assertThat(request.getStatus()).isEqualTo(ClearanceRequestStatus.READY_FOR_REGISTRAR);
        verify(approvalLogRepository).save(any());
        verify(clearanceRequestRepository).save(request);
    }

    @Test
    void rejectRequiresComment() {
        Office library = office(1L, "Library");
        User staff = officeStaff(library);
        ClearanceStep step = step(10L, clearanceRequest(), library, ClearanceStepStatus.PENDING);

        when(userRepository.findWithOfficeById(2L)).thenReturn(Optional.of(staff));
        when(clearanceStepRepository.findByIdAndOfficeId(10L, 1L)).thenReturn(Optional.of(step));

        assertThatThrownBy(() -> officeReviewService.reviewStep(
                10L,
                new ReviewClearanceStepRequest(ReviewDecision.REJECTED, " "),
                principal(staff)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Comment is required when rejecting a clearance step");
    }

    @Test
    void staffCannotReviewAnotherOfficeStep() {
        Office library = office(1L, "Library");
        User staff = officeStaff(library);

        when(userRepository.findWithOfficeById(2L)).thenReturn(Optional.of(staff));
        when(clearanceStepRepository.findByIdAndOfficeId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> officeReviewService.reviewStep(
                10L,
                new ReviewClearanceStepRequest(ReviewDecision.APPROVED, "Cleared"),
                principal(staff)
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Clearance step not found for your office");
    }

    @Test
    void alreadyReviewedStepCannotBeReviewedAgain() {
        Office library = office(1L, "Library");
        User staff = officeStaff(library);
        ClearanceStep step = step(10L, clearanceRequest(), library, ClearanceStepStatus.APPROVED);

        when(userRepository.findWithOfficeById(2L)).thenReturn(Optional.of(staff));
        when(clearanceStepRepository.findByIdAndOfficeId(10L, 1L)).thenReturn(Optional.of(step));

        assertThatThrownBy(() -> officeReviewService.reviewStep(
                10L,
                new ReviewClearanceStepRequest(ReviewDecision.APPROVED, "Again"),
                principal(staff)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("This clearance step has already been reviewed");
    }

    @Test
    void rejectedOfficeStepMakesParentRequestRejected() {
        Office library = office(1L, "Library");
        User staff = officeStaff(library);
        ClearanceRequest request = clearanceRequest();
        ClearanceStep step = step(10L, request, library, ClearanceStepStatus.PENDING);
        ClearanceStep registrarStep = step(11L, request, office(2L, "Registrar"), ClearanceStepStatus.WAITING);

        when(userRepository.findWithOfficeById(2L)).thenReturn(Optional.of(staff));
        when(clearanceStepRepository.findByIdAndOfficeId(10L, 1L)).thenReturn(Optional.of(step));
        when(clearanceStepRepository.save(any(ClearanceStep.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clearanceStepRepository.findByClearanceRequestIdOrderByOfficeIdAsc(99L))
                .thenReturn(List.of(step, registrarStep));

        officeReviewService.reviewStep(
                10L,
                new ReviewClearanceStepRequest(ReviewDecision.REJECTED, "Penalty unpaid"),
                principal(staff)
        );

        assertThat(step.getStatus()).isEqualTo(ClearanceStepStatus.REJECTED);
        assertThat(request.getStatus()).isEqualTo(ClearanceRequestStatus.REJECTED);

        ArgumentCaptor<ClearanceRequest> requestCaptor = ArgumentCaptor.forClass(ClearanceRequest.class);
        verify(clearanceRequestRepository).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getStatus()).isEqualTo(ClearanceRequestStatus.REJECTED);
    }

    private CustomUserPrincipal principal(User user) {
        return new CustomUserPrincipal(user);
    }

    private User officeStaff(Office office) {
        return User.builder()
                .id(2L)
                .fullName("Library Staff")
                .email("library.staff@test.com")
                .passwordHash("hash")
                .role(Role.OFFICE_STAFF)
                .office(office)
                .active(true)
                .build();
    }

    private ClearanceRequest clearanceRequest() {
        User student = User.builder()
                .id(1L)
                .fullName("Student One")
                .email("student@test.com")
                .passwordHash("hash")
                .role(Role.STUDENT)
                .active(true)
                .build();
        StudentProfile profile = StudentProfile.builder()
                .id(4L)
                .studentId("ATE/001")
                .department("Software Engineering")
                .program("BSc")
                .yearOfStudy(4)
                .user(student)
                .build();
        return ClearanceRequest.builder()
                .id(99L)
                .requestType(ClearanceType.GRADUATION)
                .status(ClearanceRequestStatus.IN_REVIEW)
                .studentProfile(profile)
                .build();
    }

    private Office office(Long id, String name) {
        return Office.builder()
                .id(id)
                .officeName(name)
                .active(true)
                .build();
    }

    private ClearanceStep step(Long id, ClearanceRequest request, Office office, ClearanceStepStatus status) {
        return ClearanceStep.builder()
                .id(id)
                .clearanceRequest(request)
                .office(office)
                .status(status)
                .build();
    }
}
