package com.se4801.clearance.service;

import com.se4801.clearance.dto.request.RegistrarDecision;
import com.se4801.clearance.dto.request.RegistrarDecisionRequest;
import com.se4801.clearance.exception.BusinessRuleException;
import com.se4801.clearance.model.ApprovalLog;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarClearanceServiceTest {

    @Mock
    private ClearanceRequestRepository clearanceRequestRepository;

    @Mock
    private ClearanceStepRepository clearanceStepRepository;

    @Mock
    private ApprovalLogRepository approvalLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegistrarClearanceService registrarClearanceService;

    @Test
    void registrarCanApproveReadyRequest() {
        User registrar = registrar();
        ClearanceRequest request = clearanceRequest(ClearanceRequestStatus.READY_FOR_REGISTRAR);
        List<ClearanceStep> steps = approvedOfficeSteps(request);

        when(userRepository.findById(9L)).thenReturn(Optional.of(registrar));
        when(clearanceRequestRepository.findWithStudentProfileById(20L)).thenReturn(Optional.of(request));
        when(clearanceStepRepository.findByClearanceRequestIdOrderByOfficeIdAsc(20L))
                .thenReturn(steps)
                .thenReturn(steps)
                .thenReturn(steps);

        registrarClearanceService.decideFinalClearance(
                20L,
                new RegistrarDecisionRequest(RegistrarDecision.APPROVED, "Final clearance approved"),
                principal(registrar)
        );

        assertThat(request.getStatus()).isEqualTo(ClearanceRequestStatus.COMPLETED);
        assertThat(steps.get(1).getStatus()).isEqualTo(ClearanceStepStatus.APPROVED);
        verify(clearanceRequestRepository).save(request);

        ArgumentCaptor<ApprovalLog> logCaptor = ArgumentCaptor.forClass(ApprovalLog.class);
        verify(approvalLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getAction()).isEqualTo("REGISTRAR_APPROVED");
    }

    @Test
    void registrarCanRejectReadyRequestWithComment() {
        User registrar = registrar();
        ClearanceRequest request = clearanceRequest(ClearanceRequestStatus.READY_FOR_REGISTRAR);
        List<ClearanceStep> steps = approvedOfficeSteps(request);

        when(userRepository.findById(9L)).thenReturn(Optional.of(registrar));
        when(clearanceRequestRepository.findWithStudentProfileById(20L)).thenReturn(Optional.of(request));
        when(clearanceStepRepository.findByClearanceRequestIdOrderByOfficeIdAsc(20L))
                .thenReturn(steps)
                .thenReturn(steps)
                .thenReturn(steps);

        registrarClearanceService.decideFinalClearance(
                20L,
                new RegistrarDecisionRequest(RegistrarDecision.REJECTED, "Missing final form"),
                principal(registrar)
        );

        assertThat(request.getStatus()).isEqualTo(ClearanceRequestStatus.REJECTED);
        assertThat(steps.get(1).getStatus()).isEqualTo(ClearanceStepStatus.REJECTED);
        assertThat(steps.get(1).getComment()).isEqualTo("Missing final form");

        ArgumentCaptor<ApprovalLog> logCaptor = ArgumentCaptor.forClass(ApprovalLog.class);
        verify(approvalLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getAction()).isEqualTo("REGISTRAR_REJECTED");
        assertThat(logCaptor.getValue().getNote()).isEqualTo("Missing final form");
    }

    @Test
    void rejectionWithoutCommentFails() {
        User registrar = registrar();
        ClearanceRequest request = clearanceRequest(ClearanceRequestStatus.READY_FOR_REGISTRAR);

        when(userRepository.findById(9L)).thenReturn(Optional.of(registrar));
        when(clearanceRequestRepository.findWithStudentProfileById(20L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> registrarClearanceService.decideFinalClearance(
                20L,
                new RegistrarDecisionRequest(RegistrarDecision.REJECTED, " "),
                principal(registrar)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Comment is required when registrar rejects a clearance request");
    }

    @Test
    void registrarCannotDecideInReviewRequest() {
        User registrar = registrar();
        ClearanceRequest request = clearanceRequest(ClearanceRequestStatus.IN_REVIEW);

        when(userRepository.findById(9L)).thenReturn(Optional.of(registrar));
        when(clearanceRequestRepository.findWithStudentProfileById(20L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> registrarClearanceService.decideFinalClearance(
                20L,
                new RegistrarDecisionRequest(RegistrarDecision.APPROVED, "Approved"),
                principal(registrar)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Only requests ready for registrar review can receive a final decision");
    }

    @Test
    void registrarCannotDecideCompletedRequest() {
        User registrar = registrar();
        ClearanceRequest request = clearanceRequest(ClearanceRequestStatus.COMPLETED);

        when(userRepository.findById(9L)).thenReturn(Optional.of(registrar));
        when(clearanceRequestRepository.findWithStudentProfileById(20L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> registrarClearanceService.decideFinalClearance(
                20L,
                new RegistrarDecisionRequest(RegistrarDecision.APPROVED, "Approved again"),
                principal(registrar)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Only requests ready for registrar review can receive a final decision");
    }

    @Test
    void registrarCannotApproveIfOfficeStepsAreNotAllApproved() {
        User registrar = registrar();
        ClearanceRequest request = clearanceRequest(ClearanceRequestStatus.READY_FOR_REGISTRAR);
        List<ClearanceStep> steps = List.of(
                step(1L, request, office(1L, "Library"), ClearanceStepStatus.PENDING),
                step(2L, request, office(2L, "Registrar"), ClearanceStepStatus.WAITING)
        );

        when(userRepository.findById(9L)).thenReturn(Optional.of(registrar));
        when(clearanceRequestRepository.findWithStudentProfileById(20L)).thenReturn(Optional.of(request));
        when(clearanceStepRepository.findByClearanceRequestIdOrderByOfficeIdAsc(20L)).thenReturn(steps);

        assertThatThrownBy(() -> registrarClearanceService.decideFinalClearance(
                20L,
                new RegistrarDecisionRequest(RegistrarDecision.APPROVED, "Approved"),
                principal(registrar)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("All office steps must be approved before registrar final decision");
    }

    private CustomUserPrincipal principal(User user) {
        return new CustomUserPrincipal(user);
    }

    private User registrar() {
        return User.builder()
                .id(9L)
                .fullName("Registrar User")
                .email("registrar@test.com")
                .passwordHash("hash")
                .role(Role.REGISTRAR)
                .active(true)
                .build();
    }

    private ClearanceRequest clearanceRequest(ClearanceRequestStatus status) {
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
                .id(20L)
                .requestType(ClearanceType.GRADUATION)
                .status(status)
                .studentProfile(profile)
                .build();
    }

    private List<ClearanceStep> approvedOfficeSteps(ClearanceRequest request) {
        return List.of(
                step(1L, request, office(1L, "Library"), ClearanceStepStatus.APPROVED),
                step(2L, request, office(2L, "Registrar"), ClearanceStepStatus.WAITING)
        );
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
