package com.se4801.clearance.service;

import com.se4801.clearance.dto.response.OfficeResponse;
import com.se4801.clearance.repository.OfficeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOfficeService {

    private final OfficeRepository officeRepository;

    @Transactional(readOnly = true)
    public List<OfficeResponse> getActiveOffices() {
        return officeRepository.findByActiveTrueOrderByIdAsc()
                .stream()
                .map(office -> new OfficeResponse(
                        office.getId(),
                        office.getOfficeName(),
                        office.getDescription(),
                        office.isActive()
                ))
                .toList();
    }
}
