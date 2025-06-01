package com.payiskoul.institution.program.dto;

import com.payiskoul.institution.organization.dto.InstitutionResponse;

import java.util.List;

public record PaginatedPrograms(
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<ProgramLevelResponse> programs
) {

}
