package com.se4801.clearance.service;

import com.se4801.clearance.dto.request.CreateClearanceRequest;
import com.se4801.clearance.dto.response.ClearanceRequestResponse;
import com.se4801.clearance.exception.BusinessRuleException;
import com.se4801.clearance.model.ClearanceRequest;
import com.se4801.clearance.model.ClearanceRequestStatus;
import com.se4801.clearance.model.ClearanceStep;
import com.se4801.clearance.model.ClearanceStepStatus;
import com.se4801.clearance.model.ClearanceType;
import com.se4801.clearance.model.Office;
import com.se4801.clearance.model.Role;
import com.se4801.clearance.model.StudentProfile;
import com.se4801.clearance.model.User;
import com.se4801.clearance.repository.ClearanceRequestRepository;
import com.se4801.clearance.repository.ClearanceStepRepository;
import com.se4801.clearance.repository.OfficeRepository;
import com.se4801.clearance.repository.StudentProfileRepository;
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
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClearanceRequestServiceTest {

    @Mock
    private ClearanceRequestRepository clearanceRequestRepository;

    @Mock
    private ClearanceStepRepository clearanceStepRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private OfficeRepository officeRepository;

    @InjectMocks
    private ClearanceRequestService clearanceRequestService;

    @Test
    void studentCanCreateClearanceRequestAndStepsForActiveOffices() {
        StudentProfile profile = studentProfile();
        Office library = office(1L, "Library");
        Office registrar = office(2L, "Registrar");
        ClearanceRequest savedRequest = request(10L, profile);

        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(clearanceRequestRepository.existsByStudentProfileUserIdAndStatusIn(any(), anyCollection()))
                .thenReturn(false);
        when(officeRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(library, registrar));
        when(clearanceRequestRepository.save(any(ClearanceRequest.class))).thenReturn(savedRequest);
        when(clearanceStepRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(clearanceStepRepository.findByClearanceRequestIdOrderByOfficeIdAsc(10L)).thenReturn(List.of(
                step(101L, savedRequest, library, ClearanceStepStatus.PENDING),
                step(102L, savedRequest, registrar, ClearanceStepStatus.WAITING)
        ));

        ClearanceRequestResponse response = clearanceRequestService.createRequest(
                new CreateClearanceRequest(ClearanceType.GRADUATION, "Graduating"),
                principal(Role.STUDENT)
        );

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(ClearanceRequestStatus.IN_REVIEW);
        assertThat(response.steps()).hasSize(2);

        ArgumentCaptor<List<ClearanceStep>> stepsCaptor = ArgumentCaptor.forClass(List.class);
        verify(clearanceStepRepository).saveAll(stepsCaptor.capture());
        assertThat(stepsCaptor.getValue())
                .extracting(ClearanceStep::getStatus)
                .containsExactly(ClearanceStepStatus.PENDING, ClearanceStepStatus.WAITING);
    }

    @Test
    void duplicateActiveRequestIsRejected() {
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile()));
        when(clearanceRequestRepository.existsByStudentProfileUserIdAndStatusIn(any(), anyCollection()))
                .thenReturn(true);

        assertThatThrownBy(() -> clearanceRequestService.createRequest(
                new CreateClearanceRequest(ClearanceType.GRADUATION, null),
                principal(Role.STUDENT)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("You already have an active clearance request");
    }

    @Test
    void studentWithoutProfileIsRejected() {
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clearanceRequestService.createRequest(
                new CreateClearanceRequest(ClearanceType.GRADUATION, null),
                principal(Role.STUDENT)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Student profile is required to create a clearance request");
    }

    @Test
    void noActiveOfficesCausesBusinessError() {
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile()));
        when(clearanceRequestRepository.existsByStudentProfileUserIdAndStatusIn(any(), anyCollection()))
                .thenReturn(false);
        when(officeRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of());

        assertThatThrownBy(() -> clearanceRequestService.createRequest(
                new CreateClearanceRequest(ClearanceType.TRANSFER, null),
                principal(Role.STUDENT)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No active clearance offices are configured");
    }

    private CustomUserPrincipal principal(Role role) {
        return new CustomUserPrincipal(User.builder()
                .id(1L)
                .fullName("Student One")
                .email("student@test.com")
                .passwordHash("hash")
                .role(role)
                .active(true)
                .build());
    }

    private StudentProfile studentProfile() {
        User student = User.builder()
                .id(1L)
                .fullName("Student One")
                .email("student@test.com")
                .passwordHash("hash")
                .role(Role.STUDENT)
                .active(true)
                .build();
        return StudentProfile.builder()
                .id(3L)
                .studentId("ATE/001")
                .department("Software Engineering")
                .program("BSc")
                .yearOfStudy(4)
                .user(student)
                .build();
    }

    private ClearanceRequest request(Long id, StudentProfile profile) {
        return ClearanceRequest.builder()
                .id(id)
                .requestType(ClearanceType.GRADUATION)
                .status(ClearanceRequestStatus.IN_REVIEW)
                .reason("Graduating")
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
